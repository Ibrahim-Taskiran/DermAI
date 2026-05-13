# DermAI Proje Dökümantasyonu 🩺

DermAI, derin öğrenme tekniklerini kullanarak cilt hastalıklarını analiz eden ve kullanıcıya bilgilendirici sonuçlar sunan bir mobil sağlık asistanı projesidir. Proje; **Yapay Zeka (AI)**, **Backend (FastAPI)** ve **Mobil Uygulama (Kotlin)** olmak üzere üç ana katmandan oluşmaktadır.

## 1. Teknik Mimari Özeti

*   **Mobil Uygulama:** Android platformu için Kotlin ve Jetpack Compose kullanılarak geliştirilmiştir. Kamera ve galeri entegrasyonu ile analiz yapılacak görüntüleri toplar.
*   **Backend Servisi:** FastAPI tabanlı bir Python sunucusudur. Gelen görüntüleri AI modeline iletir, sonuçları işler ve mobil uygulamaya JSON formatında döner.
*   **Yapay Zeka Modeli:** PyTorch kütüphanesi üzerine inşa edilmiş, **EfficientNet-B0** mimarisini temel alan bir sınıflandırma modelidir.

---

## 2. Yapay Zeka (AI) Modeli ve Dataset

Projenin kalbi olan model, 5 farklı hastalık grubu ve normal cilt dokusunu ayırt edebilecek şekilde eğitilmiştir.

### Model Mimarisi
*   **Backbone:** EfficientNet-B0 (Hafif ve mobil uyumlu yüksek doğruluklu mimari).
*   **Giriş Boyutu:** 224x224 piksel (RGB).
*   **Çıkış Katmanı:** 6 sınıf (Softmax aktivasyonu ile olasılık skorları üretir).

### Sınıflandırılan Hastalıklar
1.  **Akne ve Rosacea** (Acne and Rosacea)
2.  **Aktinik Keratoz ve Malign Lezyonlar** (Kanser riski taşıyan lezyonlar)
3.  **Egzama** (Atopic Dermatitis)
4.  **Pigmentasyon Bozuklukları** (Leke ve renk değişimleri)
5.  **Siğiller ve Viral Enfeksiyonlar** (Warts & Viral Infections)
6.  **Normal Cilt** (Herhangi bir hastalık bulgusu olmayan durum)

### Model Performansı ve Görseller
`dataset` klasöründe yer alan grafikler, modelin başarısını kanıtlamaktadır:
*   **`dataset_distribution.png`**: Eğitimde kullanılan verilerin sınıflara göre dağılımını gösterir.
*   **`model_performance_dashboard.png`**: Modelin eğitim sırasındaki doğruluk (accuracy) ve kayıp (loss) değerlerini, ayrıca Karmaşıklık Matrisi (Confusion Matrix) gibi kritik metrikleri içerir.

---

## 3. Backend (Sunucu) Yapısı

Backend, `backend/` dizini altında modernize edilmiş bir FastAPI yapısına sahiptir.

*   **Endpointler:**
    *   `POST /predict`: Mobil uygulamadan gelen görüntüyü alır, AI modelinden geçiri ve en yüksek olasılıklı 3 sonucu (Top-3) döner.
    *   `GET /health`: Sunucu ve modelin durumunu kontrol eder.
*   **Servis Yönetimi:** `model_service.py` dosyası, modelin belleğe yüklenmesini (Singleton Pattern) ve tahmin işlemlerinin verimli yapılmasını sağlar.

---

## 4. Mobil Uygulama Özellikleri

Kotlin ile geliştirilen uygulama, kullanıcı dostu bir arayüz ve akıcı bir iş akışı sunar:

*   **Görüntü Alma:** CameraX API ile doğrudan çekim veya galeriden seçim.
*   **Vücut Haritası (Body Map):** Hastalığın vücudun hangi bölgesinde olduğunu seçmek için interaktif 3D model desteği.
*   **Analiz Ekranı:** Modelden gelen sonuçları görselleştirir (Yüzdelik oranlar ile).
*   **Hastalık Veritabanı:** Analiz sonucunda teşhis edilen hastalık hakkında `docs/hastalik_veritabani.md` dosyasındaki verileri temel alarak bakım önerileri, risk seviyesi ve doktor uyarılarını sunar.

---

## 5. Proje Dosya Yapısı

```plaintext
DermAI/
├── ai-model/          # Model mimarisi, eğitim scriptleri ve .pth ağırlık dosyası
├── backend/           # FastAPI sunucusu, API router'ları ve model servisleri
├── dataset/           # Performans grafikleri ve veri dağılım raporları
├── docs/              # Hastalık veritabanı ve tıbbi içerik dökümanları
└── mobile-app/        # Android projesi (Kotlin/Compose)
```

## 6. Mevcut Durum Notu
Proje şu an fonksiyonel olarak tamamlanmıştır. Model 6 sınıfı (5 hastalık + 1 normal) başarıyla tanımakta, backend bu sonuçları mobil uygulamaya servis etmektedir.

---
*Bu döküman proje son durumu temel alınarak oluşturulmuştur.*
