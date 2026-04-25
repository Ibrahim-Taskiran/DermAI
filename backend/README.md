# DermAI Backend Teknik Raporu

**Hazırlayan:** Kerem | **Tarih:** 25 Nisan 2026 | **Versiyon:** 1.0.0

---

## 1. Genel Bakış

DermAI backend'i, Android mobil uygulaması ile yapay zeka modelini birbirine bağlayan bir **REST API sunucusudur**. Kullanıcı telefonda bir cilt fotoğrafı seçtiğinde bu fotoğraf backend'e gönderilir, EfficientNet-B0 modeli tarafından analiz edilir ve sonuç JSON formatında telefona iletilir.

```
Android Uygulaması  ──►  FastAPI Backend  ──►  EfficientNet-B0 Modeli
      (Retrofit)               (Uvicorn)           (ai-model/)
         ◄──────────────────────────────────────────────────
                          JSON Yanıt
```

---

## 2. Kullanılan Teknolojiler

| Teknoloji | Sürüm | Görev |
|-----------|-------|-------|
| **Python** | 3.13 | Backend programlama dili |
| **FastAPI** | 0.111.0 | Web framework — API endpoint'leri |
| **Uvicorn** | 0.30.0 | ASGI sunucusu — HTTP isteklerini dinler |
| **PyTorch** | 2.x | AI modeli çalıştırma (inference) |
| **Pydantic** | 2.x | JSON veri doğrulama ve şema tanımlama |
| **python-multipart** | 0.0.9 | Fotoğraf yükleme (multipart/form-data) |
| **python-dotenv** | 1.0.1 | `.env` dosyasından ayar okuma |
| **torchinfo** | 1.8.0 | Model mimari özeti (ai-model tarafından kullanılır) |
| **Pillow** | 11.x | Görüntü işleme |

---

## 3. Klasör Yapısı ve Dosyaların Görevleri

```
backend/
│
├── main.py                  ← Uygulamanın başlangıç noktası
├── requirements.txt         ← Tüm Python bağımlılıkları
├── .env                     ← Gizli ayarlar (model yolu, port, cihaz)
│
├── core/
│   └── config.py            ← Merkezi ayar yönetimi
│
├── schemas/
│   └── response.py          ← API yanıt veri yapıları (Pydantic)
│
├── services/
│   ├── model_service.py     ← AI model yükleme ve tahmin servisi
│   └── advice_service.py    ← Hastalığa göre bakım önerisi üretme
│
└── routers/
    └── predict.py           ← HTTP endpoint tanımları (/predict, /health)
```

---

## 4. Dosyaların Detaylı Açıklaması

### 4.1 `main.py` — Uygulamanın Kalbi

Tüm sistemi ayağa kaldıran dosyadır. Üç temel işi vardır:

**a) Lifespan (Yaşam Döngüsü) Yönetimi**

Sunucu başladığında AI modeli otomatik olarak belleğe yüklenir. Bu sayede her istek geldiğinde model yeniden yüklenmez; bir kez yüklenir ve tüm isteklere hizmet verir.

```
Sunucu Başlar → model_service.load() çağrılır → Model RAM'e yüklenir → İstekler kabul edilir
Sunucu Kapanır → Kaynaklar serbest bırakılır
```

**b) CORS Middleware**

Farklı kaynaklardan (Android emülatör, Postman, farklı IP'ler) gelen isteklere izin verir. Geliştirme aşamasında tüm kaynaklara açıktır.

**c) Router Kaydı**

`predict.py` dosyasındaki endpoint'leri uygulamaya bağlar.

---

### 4.2 `core/config.py` — Merkezi Ayar Yönetimi

Tüm ayarlar tek bir yerden yönetilir. `.env` dosyasını okur ve `settings` nesnesi aracılığıyla diğer modüllere dağıtır.

| `.env` Değişkeni | Varsayılan | Açıklama |
|---|---|---|
| `HOST` | `0.0.0.0` | Sunucunun dinleyeceği IP (0.0.0.0 = tüm ağ arayüzleri) |
| `PORT` | `8000` | Sunucu portu |
| `MODEL_CHECKPOINT_PATH` | `../ai-model/checkpoints/...` | Eğitilmiş model dosyasının yolu |
| `MODEL_NUM_CLASSES` | `6` | Sınıf sayısı |
| `DEVICE` | `cpu` | `cpu` veya `cuda` |

---

### 4.3 `schemas/response.py` — Veri Yapıları

Android uygulamasının `AnalysisResponse.kt` dosyasıyla **birebir eşleşen** Python veri yapılarıdır. Pydantic kullanılarak otomatik doğrulama ve JSON dönüşümü sağlanır.

```
Python (Backend)              Kotlin (Android)
─────────────────────────     ──────────────────────────
Prediction                ←→  data class Prediction
  disease: str                  val disease: String
  probability: float            val probability: Double

Advice                    ←→  data class Advice
  care: str                     val care: String
  recommendation: str           val recommendation: String
  doctor_warning: str           val doctorWarning: String

AnalysisResponse          ←→  data class AnalysisResponse
  success: bool                 val success: Boolean
  top_prediction: Prediction    val topPrediction: Prediction
  top3_predictions: list        val top3Predictions: List<Prediction>
  advice: Advice                val advice: Advice
```

---

### 4.4 `services/model_service.py` — AI Model Köprüsü

Backend ile `ai-model/` klasörü arasındaki en kritik bağlantı burada kurulur.

**ai-model klasörüyle bağlantı:**

```python
sys.path.insert(0, "../ai-model")   # ai-model klasörü Python yoluna eklenir

from model import build_dermai_model       # EfficientNet-B0 mimarisini kurar
from transforms import get_val_transforms  # Görüntü ön işleme pipeline'ı
from inference import predict_image        # Tahmin fonksiyonu
from config import EXPERTS_CLASSES         # 6 hastalık sınıfı listesi
```

**Model yükleme süreci:**

```
1. EfficientNet-B0 mimarisi oluşturulur (boş ağırlıklarla)
2. .pth checkpoint dosyası diskten okunur
3. Eğitilmiş ağırlıklar modele yüklenir
4. Model eval() moduna alınır (dropout/batchnorm pasif)
5. Validation transform pipeline'ı hazırlanır
```

**Tahmin süreci:**

```
Görüntü byte'ları gelir
    → Geçici .jpg dosyasına yazılır
    → predict_image() çağrılır (ai-model/inference.py)
    → Softmax ile 0-1 arası olasılıklar hesaplanır
    → Top-3 tahmin döndürülür
    → Geçici dosya silinir
```

**Singleton tasarım deseni:** Model tüm uygulama boyunca yalnızca **bir kez** yüklenir. Her istek için yeniden yüklenmez; bu hem hızı artırır hem de bellek tasarrufu sağlar.

---

### 4.5 `services/advice_service.py` — Bakım Önerileri

AI modelinin tahmin ettiği hastalık adına göre 3 kategoride Türkçe öneri üretir:

| Hastalık | care | recommendation | doctor_warning |
|---|---|---|---|
| Eczema (Atopic Dermatitis) | Nemlendirici önerileri | Steroid içermeyen kremler | Enfeksiyon belirtilerinde dermatolog |
| Normal | Rutin bakım | Yıllık kontrol | Ani değişimlerde doktor |
| Acne and Rosacea | Temizlik rutini | Salisilik asit ürünleri | Nodül/kist varsa dermatolog |
| Actinic Keratosis / Malignant | Güneşten korunma | SPF 50+ | ⚠️ ACİL — Dermatolog/onkolog |
| Pigmentation Disorders | Güneş koruması | Aydınlatıcı serumlar | Hızlı yayılımda dermatolog |
| Warts / Viral Infections | Bulaş önleme | Salisilik asit bantları | Sayı artışında dermatolog |

---

### 4.6 `routers/predict.py` — HTTP Endpoint'leri

#### `POST /predict` — Ana Analiz Endpoint'i

Android uygulamasının her analiz talebinde çağırdığı endpoint. İşlem sırası:

```
1. Model hazır mı? → Değilse 503 hata döndür
2. Dosya formatı geçerli mi? (JPEG/PNG/WEBP) → Değilse 400 hata döndür
3. Dosya boyutu 10 MB altında mı? → Değilse 400 hata döndür
4. model_service.predict() → Top-3 tahmin al
5. advice_service.get_advice_for_disease() → Bakım önerisi al
6. AnalysisResponse JSON → Android'e gönder
```

#### `GET /health` — Sağlık Kontrolü

```json
{
  "status": "ok",
  "model_loaded": true,
  "message": "Sunucu çalışıyor, model hazır."
}
```

#### `GET /` — API Bilgisi

```json
{
  "app": "DermAI API",
  "version": "1.0.0",
  "status": "çalışıyor",
  "model_loaded": true
}
```

---

## 5. Tam Veri Akışı

```
[Android Telefon]
    Kullanıcı fotoğraf seçer
    ApiAnalysisRepository.kt → ContentResolver ile fotoğraf byte'larını okur
    Retrofit → POST http://192.168.1.112:8000/predict
               multipart/form-data | field: "file"
                    ↓
[FastAPI Backend - routers/predict.py]
    Dosya format ve boyut kontrolü
    model_service.predict(image_bytes) çağrılır
                    ↓
[services/model_service.py]
    Byte'lar geçici dosyaya yazılır
    ai-model/inference.py → predict_image() çağrılır
                    ↓
[ai-model/inference.py]
    PIL ile görüntü açılır
    get_val_transforms() ile ön işleme (224x224, normalize)
    EfficientNet-B0 forward pass
    Softmax → olasılıklar
    Top-3 tahmin döndürülür
                    ↓
[services/advice_service.py]
    En yüksek tahmin → Türkçe bakım önerisi seçilir
                    ↓
[FastAPI Backend]
    AnalysisResponse JSON oluşturulur
    HTTP 200 → Android'e gönderilir
                    ↓
[Android Telefon]
    ResultScreen → Hastalık adı, olasılık, öneriler gösterilir
```

---

## 6. Android Tarafındaki Değişiklikler

Backend hazır hale gelince Android tarafında da 4 dosya güncellendi:

| Dosya | Değişiklik |
|---|---|
| `NetworkModule.kt` | `BASE_URL` → `http://192.168.1.112:8000/` |
| `AppModule.kt` | `MockAnalysisRepository` → `ApiAnalysisRepository` |
| `ApiAnalysisRepository.kt` | `File(path)` yerine `ContentResolver` kullanımı (Android 13+ uyumu) |
| `AndroidManifest.xml` | `usesCleartextTraffic="true"` (HTTP izni) |

---

## 7. Sunucuyu Başlatma

```powershell
cd C:\Users\kerem\DermAI\backend
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

Swagger UI (tarayıcıdan test): `http://localhost:8000/docs`

---

## 8. Gelecekte Yapılabilecek Geliştirmeler

| Konu | Açıklama |
|---|---|
| **HTTPS** | Üretim ortamında HTTP yerine HTTPS kullanılmalı |
| **GPU Desteği** | `.env`'de `DEVICE=cuda` yapılarak GPU'da çalıştırılabilir |
| **Rate Limiting** | Aşırı istek koruması eklenebilir |
| **Kimlik Doğrulama** | API anahtarı veya JWT token sistemi |
| **Bulut Deployment** | Render, Railway veya AWS'e taşınabilir |
