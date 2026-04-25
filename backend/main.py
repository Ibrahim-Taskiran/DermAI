# =============================================
# DermAI Backend - FastAPI Giriş Noktası
# =============================================
# Sunucuyu başlatmak için:
#   cd backend
#   uvicorn main:app --host 0.0.0.0 --port 8000 --reload
#
# API dokümantasyonu (sunucu çalışınca):
#   http://localhost:8000/docs     (Swagger UI)
#   http://localhost:8000/redoc    (ReDoc)
# =============================================

import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from core.config import settings
from routers.predict import router as predict_router
from services.model_service import model_service

# =============================================
# Loglama Konfigürasyonu
# =============================================
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(levelname)-8s | %(name)s | %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)
logger = logging.getLogger(__name__)


# =============================================
# Uygulama Yaşam Döngüsü (Lifespan)
# =============================================
# FastAPI başlarken model yüklenir,
# kapanırken kaynaklar serbest bırakılır.
@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Sunucu başlangıç ve kapatma işlemlerini yönetir.

    Başlangıç (startup):
        - EfficientNet-B0 modeli belleğe yüklenir.
        - Model yüklenemezse hata loglanır ama sunucu yine de başlar.
          /predict endpoint'i 503 döndürecektir.

    Kapatma (shutdown):
        - Gelecekte eklenmesi planlanan temizleme işlemleri buraya gelir.
    """
    # --- Sunucu Başlangıcı ---
    logger.info("=" * 50)
    logger.info("DermAI Backend başlatılıyor...")
    logger.info("Cihaz: %s | Checkpoint: %s", settings.DEVICE, settings.MODEL_CHECKPOINT_PATH)

    # AI modelini yükle
    model_service.load()

    if model_service.is_loaded:
        logger.info("Model hazır. Sunucu istekleri kabul ediyor.")
    else:
        logger.warning(
            "Model yüklenemedi. /predict endpoint'i çalışmayacak.\n"
            "Checkpoint dosyasının varlığını kontrol edin: %s",
            settings.MODEL_CHECKPOINT_PATH
        )

    logger.info("=" * 50)

    yield  # Sunucu burada çalışmaya devam eder

    # --- Sunucu Kapatılırken ---
    logger.info("DermAI Backend kapatılıyor...")


# =============================================
# FastAPI Uygulama Örneği
# =============================================
app = FastAPI(
    title="DermAI API",
    description=(
        "DermAI - Yapay Zeka Destekli Cilt Hastalığı Analiz API'si\n\n"
        "Mobil uygulamadan gelen cilt görüntülerini EfficientNet-B0 modeli ile analiz eder. "
        "Hastalık tahmini, olasılık skorları ve kişiselleştirilmiş bakım önerileri döndürür."
    ),
    version="1.0.0",
    lifespan=lifespan,
    docs_url="/docs",      # Swagger UI
    redoc_url="/redoc",    # ReDoc
)


# =============================================
# CORS Middleware
# =============================================
# Farklı kaynaklardan (Android emülatör, Postman vb.)
# gelen isteklere izin verir.
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],       # Geliştirme ortamı için tüm kaynaklar açık
    allow_credentials=True,
    allow_methods=["*"],       # GET, POST, OPTIONS vb. hepsine izin ver
    allow_headers=["*"],
)


# =============================================
# Router Kaydı
# =============================================
# predict.py içindeki tüm endpoint'leri uygulamaya bağla
app.include_router(predict_router, tags=["Analiz"])


# =============================================
# Kök Endpoint - Sunucu Durum Kontrolü
# =============================================
@app.get("/", tags=["Genel"], summary="API Bilgisi")
async def root():
    """
    GET /

    API'nin çalışıp çalışmadığını ve temel bilgileri döndürür.
    Mobil uygulama başlangıçta bu endpoint'i çağırabilir.
    """
    return {
        "app": "DermAI API",
        "version": "1.0.0",
        "status": "çalışıyor",
        "model_loaded": model_service.is_loaded,
        "endpoints": {
            "analiz": "POST /predict",
            "saglik": "GET /health",
            "swagger": "GET /docs"
        }
    }


# =============================================
# Doğrudan Çalıştırma (python main.py)
# =============================================
# Geliştirme ortamı için: uvicorn ile başlatılır.
# Üretim için: uvicorn main:app --host 0.0.0.0 --port 8000
if __name__ == "__main__":
    import uvicorn

    uvicorn.run(
        "main:app",
        host=settings.HOST,
        port=settings.PORT,
        reload=True,    # Kod değişikliklerinde otomatik yeniden başlatma
        log_level="info"
    )
