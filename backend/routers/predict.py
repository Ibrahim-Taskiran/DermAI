# =============================================
# DermAI Backend - Tahmin (Predict) Router'ı
# =============================================
# Mobil uygulamadan gelen görüntüyü alır,
# AI modeline gönderir ve sonuçları JSON
# formatında geri döndürür.
#
# Android'deki DermAIApiService.kt:
#   @Multipart @POST("predict")
#   suspend fun analyzeImage(@Part image: MultipartBody.Part): AnalysisResponse
#
# Multipart field adı: "file"  (ApiAnalysisRepository.kt'den)
# =============================================

import logging
from fastapi import APIRouter, UploadFile, File, HTTPException, status

from schemas.response import AnalysisResponse, ErrorResponse
from services.model_service import model_service
from services.advice_service import get_advice_for_disease
from core.config import settings

# Loglama
logger = logging.getLogger(__name__)

# Bu router main.py'de /predict prefix'i ile bağlanır
router = APIRouter()


@router.post(
    "/predict",
    response_model=AnalysisResponse,
    summary="Cilt Görüntüsü Analizi",
    description=(
        "Mobil uygulamadan gelen cilt görüntüsünü EfficientNet-B0 modeline gönderir. "
        "Hastalık tahmini, olasılık skorları ve bakım önerilerini JSON olarak döndürür."
    ),
    responses={
        200: {"model": AnalysisResponse, "description": "Analiz başarıyla tamamlandı"},
        400: {"model": ErrorResponse, "description": "Geçersiz dosya formatı veya boyutu"},
        503: {"model": ErrorResponse, "description": "Model henüz yüklenmedi"},
        500: {"model": ErrorResponse, "description": "Sunucu hatası"},
    }
)
async def predict(
    file: UploadFile = File(
        ...,
        description="Analiz edilecek cilt görüntüsü (JPEG, PNG veya WEBP)"
    )
):
    """
    POST /predict

    Mobil uygulama bu endpoint'e multipart/form-data olarak
    'file' adıyla görüntü gönderir.

    İşlem adımları:
        1. Dosya formatı ve boyutu doğrulanır.
        2. Görüntü byte'ları ModelService'e aktarılır.
        3. EfficientNet-B0 top-3 tahmin döndürür.
        4. En yüksek tahmine göre bakım önerisi seçilir.
        5. Android formatında AnalysisResponse döndürülür.
    """

    # --- 1. Model Hazırlık Kontrolü ---
    if not model_service.is_loaded:
        logger.error("Tahmin isteği geldi ancak model yüklü değil.")
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail={
                "success": False,
                "error": "Model servisi kullanılamıyor",
                "detail": (
                    "AI modeli henüz yüklenmedi veya yükleme başarısız oldu. "
                    "Lütfen checkpoint dosyasının mevcut olduğunu doğrulayın."
                )
            }
        )

    # --- 2. Dosya Format Doğrulaması ---
    if file.content_type not in settings.ALLOWED_CONTENT_TYPES:
        logger.warning("Desteklenmeyen dosya türü: %s", file.content_type)
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={
                "success": False,
                "error": "Desteklenmeyen dosya formatı",
                "detail": (
                    f"Gönderilen format: {file.content_type}. "
                    f"Desteklenen formatlar: {', '.join(settings.ALLOWED_CONTENT_TYPES)}"
                )
            }
        )

    # --- 3. Dosya Boyutu Kontrolü ---
    image_bytes = await file.read()
    max_bytes = settings.MAX_FILE_SIZE_MB * 1024 * 1024   # MB -> Byte dönüşümü

    if len(image_bytes) > max_bytes:
        logger.warning(
            "Dosya boyutu aşıldı: %.2f MB (limit: %d MB)",
            len(image_bytes) / (1024 * 1024),
            settings.MAX_FILE_SIZE_MB
        )
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail={
                "success": False,
                "error": "Dosya boyutu çok büyük",
                "detail": f"Maksimum dosya boyutu {settings.MAX_FILE_SIZE_MB} MB'dır."
            }
        )

    # --- 4. AI Model Inference ---
    try:
        logger.info(
            "Tahmin başlatıldı | Dosya: %s | Boyut: %d bytes",
            file.filename,
            len(image_bytes)
        )

        # ModelService: görüntüyü işleyip top-3 Prediction listesi döndürür
        top3_predictions = model_service.predict(image_bytes)

    except RuntimeError as e:
        # Model yüklü değil (load() sırasında hata oluştuysa)
        logger.error("Model inference RuntimeError: %s", e)
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail={"success": False, "error": str(e)}
        )
    except Exception as e:
        # Beklenmeyen hatalar (bozuk görüntü, bellek yetersizliği vb.)
        logger.error("Tahmin sırasında beklenmeyen hata: %s", e, exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail={
                "success": False,
                "error": "Görüntü işlenirken hata oluştu",
                "detail": "Geçerli bir cilt görüntüsü gönderdiğinizden emin olun."
            }
        )

    # --- 5. Sonuçları Birleştir ve Yanıtı Oluştur ---
    # En yüksek olasılıklı tahmin birinci sıradadır (inference.py bunu garanti eder)
    top_prediction = top3_predictions[0]

    # En yüksek tahmine göre bakım önerisi seç
    advice = get_advice_for_disease(top_prediction.disease)

    logger.info(
        "Tahmin tamamlandı | Sonuç: %s (%.1f%%)",
        top_prediction.disease,
        top_prediction.probability * 100
    )

    # --- 6. Android'in Beklediği Formatta Yanıt Döndür ---
    return AnalysisResponse(
        success=True,
        top_prediction=top_prediction,
        top3_predictions=top3_predictions,
        advice=advice
    )


@router.get(
    "/health",
    summary="Sunucu ve Model Sağlık Kontrolü",
    description="Sunucunun ayakta olup olmadığını ve modelin yüklü olup olmadığını döndürür."
)
async def health_check():
    """
    GET /health

    Sunucunun çalışıp çalışmadığını ve modelin hazır olup olmadığını
    kontrol etmek için kullanılır. Mobil uygulama bağlantı testi için
    bu endpoint'i kullanabilir.
    """
    return {
        "status": "ok",
        "model_loaded": model_service.is_loaded,
        "message": (
            "Sunucu çalışıyor, model hazır."
            if model_service.is_loaded
            else "Sunucu çalışıyor ancak model henüz yüklenmedi."
        )
    }
