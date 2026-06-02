# =============================================
# DermAI Backend - Uygulama Konfigürasyonu
# =============================================
# .env dosyasından ortam değişkenlerini okur ve
# tüm modüllerin kullanabileceği merkezi bir
# ayar nesnesi oluşturur.
# =============================================

import os
from pathlib import Path
from dotenv import load_dotenv

# Backend klasörünün bir üst dizinindeki .env dosyasını yükle
_BASE_DIR = Path(__file__).resolve().parent.parent
load_dotenv(_BASE_DIR / ".env")


class Settings:
    """
    Uygulama genelinde kullanılan tüm ayarları tutan sınıf.
    Değerler önce .env dosyasından, bulunamazsa varsayılan
    değerlerden okunur.
    """

    # --- Sunucu Ayarları ---
    HOST: str = os.getenv("HOST", "0.0.0.0")
    PORT: int = int(os.getenv("PORT", "8000"))

    # --- Yol Ayarları ---
    # Backend klasörünün kök dizini
    BASE_DIR: Path = _BASE_DIR

    # ai-model export paketinin yolu (import için sys.path'e eklenir)
    AI_MODEL_DIR: Path = _BASE_DIR.parent / "ai-model" / "export"

    # Model checkpoint dosyasının tam yolu
    # .env'deki yol backend/ dizinine göre relatif olarak yorumlanır
    MODEL_CHECKPOINT_PATH: Path = (
        _BASE_DIR / os.getenv("MODEL_CHECKPOINT_PATH", "../ai-model/export/dermai.pth")
    ).resolve()

    # --- Model Ayarları ---
    # 12 sınıflı yeni export modelini desteklemek için varsayılan değer güncellendi.
    MODEL_NUM_CLASSES: int = int(os.getenv("MODEL_NUM_CLASSES", "12"))

    # Tahmin cihazı: "cpu" veya "cuda"
    DEVICE: str = os.getenv("DEVICE", "cpu")

    # Desteklenen görüntü formatları (mobil uygulamadan gelen dosyaları filtreler)
    ALLOWED_CONTENT_TYPES: list = ["image/jpeg", "image/png", "image/webp"]

    # Maksimum yüklenebilir dosya boyutu (10 MB)
    MAX_FILE_SIZE_MB: int = 10


# Modüller bu nesneyi import ederek ayarlara erişir
settings = Settings()
