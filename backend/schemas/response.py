# =============================================
# DermAI Backend - API Yanıt Şemaları
# =============================================
# Android uygulamasındaki AnalysisResponse.kt
# ile birebir eşleşen Pydantic modelleri.
#
# Android modeli referansı:
#   data class Prediction(val disease: String, val probability: Double)
#   data class Advice(val care, recommendation, doctor_warning)
#   data class AnalysisResponse(success, top_prediction, top3_predictions, advice)
# =============================================

from pydantic import BaseModel, Field
from typing import List


class Prediction(BaseModel):
    """
    Tek bir hastalık tahmini.
    Android: data class Prediction(val disease: String, val probability: Double)
    """

    # Hastalığın İngilizce adı (AI modelinin sınıf adı)
    disease: str = Field(..., description="Tahmin edilen hastalığın adı")

    # Modelin bu sınıfa verdiği güven skoru (0.0 - 1.0 arası)
    probability: float = Field(..., ge=0.0, le=1.0, description="Tahmin güven skoru")

    class Config:
        json_schema_extra = {
            "example": {
                "disease": "Eczema (Atopic Dermatitis)",
                "probability": 0.87
            }
        }


class Advice(BaseModel):
    """
    Tespit edilen hastalığa özel bakım ve uyarı bilgileri.
    Android: data class Advice(val care, val recommendation, val doctor_warning)
    """

    # Günlük cilt bakım tavsiyeleri
    care: str = Field(..., description="Günlük bakım önerileri")

    # Kullanılabilecek ürün veya tedavi önerileri
    recommendation: str = Field(..., description="Tedavi ve ürün önerileri")

    # Doktora ne zaman başvurulması gerektiği
    doctor_warning: str = Field(..., description="Doktora başvurma uyarısı", alias="doctor_warning")

    class Config:
        # Android'in beklediği anahtar adını (snake_case) koru
        populate_by_name = True
        json_schema_extra = {
            "example": {
                "care": "Cildi düzenli olarak nemlendiricilerle koruyun.",
                "recommendation": "Steroid içermeyen kremler kullanabilirsiniz.",
                "doctor_warning": "Belirtiler kötüleşirse dermatolog ziyareti zorunludur."
            }
        }


class AnalysisResponse(BaseModel):
    """
    Mobil uygulamaya döndürülecek tam analiz yanıtı.
    Android: data class AnalysisResponse(success, top_prediction, top3_predictions, advice)

    Alan adları Android'deki @SerializedName anotasyonlarıyla eşleşir:
        @SerializedName("top_prediction")  -> top_prediction
        @SerializedName("top3_predictions") -> top3_predictions
    """

    # İşlemin başarılı olup olmadığı
    success: bool = Field(..., description="API isteğinin başarı durumu")

    # En yüksek olasılıklı tahmin
    top_prediction: Prediction = Field(..., description="En güçlü tahmin")

    # İlk 3 tahminin listesi (en yüksekten en düşüğe)
    top3_predictions: List[Prediction] = Field(..., description="İlk 3 tahmin listesi")

    # Tespit edilen hastalığa özel bakım bilgileri
    advice: Advice = Field(..., description="Bakım önerileri ve doktor uyarısı")

    class Config:
        json_schema_extra = {
            "example": {
                "success": True,
                "top_prediction": {
                    "disease": "Eczema (Atopic Dermatitis)",
                    "probability": 0.87
                },
                "top3_predictions": [
                    {"disease": "Eczema (Atopic Dermatitis)", "probability": 0.87},
                    {"disease": "Normal", "probability": 0.08},
                    {"disease": "Acne and Rosacea Photos", "probability": 0.05}
                ],
                "advice": {
                    "care": "Cildi düzenli olarak nemlendiricilerle koruyun.",
                    "recommendation": "Steroid içermeyen kremler kullanabilirsiniz.",
                    "doctor_warning": "Belirtiler kötüleşirse dermatolog ziyareti zorunludur."
                }
            }
        }


class ErrorResponse(BaseModel):
    """
    Hata durumlarında döndürülen yanıt şeması.
    """

    # İşlem başarısız olduğu için her zaman False
    success: bool = False

    # Hatanın kısa açıklaması
    error: str = Field(..., description="Hata mesajı")

    # Hata detayları (opsiyonel, geliştirme aşamasında faydalı)
    detail: str | None = Field(None, description="Ek hata detayı")

    class Config:
        json_schema_extra = {
            "example": {
                "success": False,
                "error": "Geçersiz dosya formatı",
                "detail": "Sadece JPEG, PNG ve WEBP formatları desteklenmektedir."
            }
        }
