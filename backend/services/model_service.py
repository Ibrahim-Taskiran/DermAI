# =============================================
# DermAI Backend - Model Servisi
# =============================================
# EfficientNet-B0 modelini belleğe yükler ve
# gelen görüntüler üzerinde inference çalıştırır.
#
# ai-model/export paketindeki:
#   - model.py      -> build_dermai_model()
#   - transforms.py -> get_val_transforms()
#   - inference.py  -> predict_image()
#   - config.py     -> EXPERT_CLASSES
# dosyaları buradan import edilir.
# =============================================

import sys
import logging
import tempfile
import os
from pathlib import Path
from typing import Optional

import torch
from PIL import Image

from core.config import settings
from schemas.response import Prediction

# Loglama ayarları
logger = logging.getLogger(__name__)

# =============================================
# ai-model klasörünü Python modül yoluna ekle
# Böylece ai-model/model.py, transforms.py vb.
# doğrudan import edilebilir.
# =============================================
_AI_MODEL_PATH = str(settings.AI_MODEL_DIR)
if _AI_MODEL_PATH not in sys.path:
    sys.path.insert(0, _AI_MODEL_PATH)

try:
    from model import build_dermai_model          # EfficientNet-B0 oluşturucu
    from transforms import get_val_transforms     # Görüntü ön işleme pipeline'ı
    from inference import predict_image           # Top-K tahmin fonksiyonu
    from config import EXPERT_CLASSES, IMAGE_SIZE

    _AI_MODEL_IMPORTS_OK = True
except ImportError as e:
    logger.error(
        "ai-model export modülleri yüklenemedi: %s\n"
        "ai-model/export klasörünün mevcut olduğundan ve "
        "model.py, transforms.py, inference.py, config.py dosyalarının "
        "içinde bulunduğundan emin olun.",
        e
    )
    _AI_MODEL_IMPORTS_OK = False


def _infer_num_classes(state_dict: dict) -> int:
    """Checkpoint'teki classifier katmanından çıktı sınıf sayısını okur."""
    for key, tensor in state_dict.items():
        if key.endswith("classifier.1.weight"):
            return int(tensor.shape[0])
    raise ValueError("state_dict içinde classifier.1.weight bulunamadı.")


class ModelService:
    """
    EfficientNet-B0 modelini belleğe yükleyen ve
    görüntü üzerinde tahmin yapan servis sınıfı.

    Sunucu başladığında tek bir örnek oluşturulur (singleton).
    Model bir kez yüklenir ve tüm istekler için yeniden kullanılır.
    """

    def __init__(self):
        # Model nesnesi (yüklenmeden önce None)
        self._model: Optional[torch.nn.Module] = None

        # Validation transform pipeline'ı
        self._transforms = None

        # Kullanılacak cihaz (cpu / cuda)
        self._device: str = settings.DEVICE

        # Sınıf isimleri (inference sonuçlarını etiketlemek için)
        self._class_names: list[str] = []

        # Model başarıyla yüklenip yüklenmediği
        self._is_loaded: bool = False

    def load(self) -> None:
        """
        Modeli ve transform pipeline'ını belleğe yükler.
        FastAPI uygulama başlarken lifespan içinde çağrılır.

        Hata durumunda uygulama çökmez; /predict endpoint'i
        503 Service Unavailable döndürür.
        """
        if not _AI_MODEL_IMPORTS_OK:
            logger.error("ai-model import hatası nedeniyle model yüklenemedi.")
            return

        checkpoint_path = settings.MODEL_CHECKPOINT_PATH

        # Checkpoint dosyası mevcut mu kontrol et
        if not checkpoint_path.exists():
            logger.warning(
                "Model checkpoint bulunamadı: %s\n"
                "Lütfen ai-model/train.py ile modeli eğitin ve "
                "checkpoint dosyasının bu konumda olduğundan emin olun.\n"
                "Backend mock modu olmadan çalışamayacak.",
                checkpoint_path
            )
            return

        try:
            logger.info("Model yükleniyor: %s", checkpoint_path)

            checkpoint = torch.load(
                checkpoint_path,
                map_location=self._device,
                weights_only=True,
            )

            if isinstance(checkpoint, dict):
                state_dict = checkpoint.get(
                    "model_state_dict",
                    checkpoint.get("state_dict", checkpoint),
                )
                saved_classes = checkpoint.get("class_names")
            else:
                state_dict = checkpoint
                saved_classes = None

            num_classes = _infer_num_classes(state_dict)
            expected_classes = len(EXPERT_CLASSES)
            if num_classes != expected_classes:
                raise ValueError(
                    f"Checkpoint {num_classes} sınıflı; DermAI export paketi {expected_classes} sınıf bekliyor. "
                    f"Doğru checkpoint dosyasını kullanın."
                )
            logger.info("Checkpoint sınıf sayısı: %d", num_classes)

            self._model = build_dermai_model(
                num_classes=expected_classes,
                pretrained=False,
            )
            self._model.load_state_dict(state_dict)
            self._model.to(self._device)
            self._model.eval()

            if saved_classes:
                self._class_names = list(saved_classes)
                if self._class_names != list(EXPERT_CLASSES):
                    raise ValueError(
                        "Checkpoint class_names eğitim sırasıyla uyuşmuyor. "
                        f"Beklenen: {list(EXPERT_CLASSES)}, "
                        f"checkpoint: {self._class_names}"
                    )
                logger.info("Sınıf isimleri checkpoint'ten alındı: %s", self._class_names)
            else:
                self._class_names = list(EXPERT_CLASSES)
                logger.info(
                    "Sınıf isimleri export config içinden alındı: %s",
                    self._class_names,
                )

            # Validation transform pipeline'ını hazırla
            self._transforms = get_val_transforms(target_size=IMAGE_SIZE)

            self._is_loaded = True
            logger.info(
                "Model başarıyla yüklendi. Cihaz: %s | Sınıf sayısı: %d",
                self._device, len(self._class_names)
            )

        except Exception as e:
            logger.error("Model yüklenirken hata oluştu: %s", e, exc_info=True)
            self._is_loaded = False

    @property
    def is_loaded(self) -> bool:
        """Modelin hazır olup olmadığını döndürür."""
        return self._is_loaded

    def predict(self, image_bytes: bytes) -> list[Prediction]:
        """
        Ham görüntü byte'larından top-3 tahmin listesi döndürür.

        Parametre:
            image_bytes (bytes): Mobil uygulamadan gelen multipart görüntü verisi.

        Döndürür:
            list[Prediction]: En yüksekten en düşüğe sıralı 3 tahmin.

        Hata:
            RuntimeError: Model yüklü değilse fırlatılır.
            ValueError: Görüntü okunamazsa fırlatılır.
        """
        if not self._is_loaded:
            raise RuntimeError(
                "Model henüz yüklenmedi veya yükleme başarısız oldu. "
                "Sunucu loglarını kontrol edin."
            )

        # Görüntü byte'larını geçici bir dosyaya yaz
        # predict_image() fonksiyonu dosya yolu (str) bekliyor
        with tempfile.NamedTemporaryFile(suffix=".jpg", delete=False) as tmp_file:
            tmp_file.write(image_bytes)
            tmp_path = tmp_file.name

        try:
            # AI model inference - ai-model/inference.py'deki predict_image() kullanılır
            result = predict_image(
                image_path=tmp_path,
                model=self._model,
                val_transforms=self._transforms,
                class_names=self._class_names,
                device=self._device
            )
        finally:
            # Geçici dosyayı temizle (hata olsa bile)
            os.unlink(tmp_path)

        # predict_image() çıktısı: {"predictions": [{"disease_id": ..., "probability": ...}]}
        # Android'in beklediği format: Prediction(disease=..., probability=...)
        predictions = [
            Prediction(
                disease=item["disease_id"],
                probability=round(item["probability"], 4)
            )
            for item in result["predictions"]
        ]

        return predictions


# Uygulama genelinde kullanılacak tek ModelService örneği (singleton)
model_service = ModelService()
