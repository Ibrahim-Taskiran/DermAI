# DermAI: Derin Öğrenme Tabanlı Dermatolojik Tarama Sistemi

## Proje Başlığı ve Genel Bakış
**DermAI**, dermatolojik rahatsızlıkların tespitinde yüksek hassasiyetli klinik tanı desteği sağlamak üzere tasarlanmış, Derin Öğrenme (Deep Learning) tabanlı yenilikçi bir tarama aracıdır. Temel amacı, cilt lezyonlarını analiz ederek teşhis süreçlerinde doktorlara ve tıbbi uzmanlara güvenilir, hızlı ve yapay zeka destekli bir ikinci görüş (second opinion) sunmaktır.

## Sinir Ağı Mimarisi
- **EfficientNet-B0 Omurgası:** Modelin temel feature-extractor (özellik çıkarıcı) mimarisi olarak güçlü ve hafif yapısıyla **EfficientNet-B0** kullanılmıştır.
- **Bileşik Ölçeklendirme (Compound Scaling):** Bu mimarinin tercih edilme sebebi; ağın derinliğini, genişliğini ve giriş çözünürlüğünü matematiksel olarak dengeli bir şekilde ölçeklendirebilmesidir. Böylece VRAM (donanım kısıtlamaları) aşılmadan maksimum başarılı sonuç alınır.
- **Özelleştirilmiş Sınıflandırıcı Başlık (Classifier Head):** Sadece hedeflediğimiz uzmanlık sınıflarını tahmin etmek üzere ağın sonuna projemize özel dinamik bir sınıflandırma katmanı entegre edilmiştir.

## "Dengeli Uzman" (Balanced Expert) Stratejisi
- **Veri Mühendisliği ve Sınırlandırma (Capping):** Veri setindeki aşırı sınıf dengesizliğini (class imbalance) önlemek amacıyla, modelin yanlılık (bias) geliştirmemesi için her sınıf maksimum **500 görsel** ile sınırlandırılmıştır. İstatistiksel olarak sağlığı zayıf olan (250 görselden az) sınıflar ise analizden tamamen çıkarılmıştır.
- **Optimizasyon ve Ağırlıklandırma:** Hızlı ve istikrarlı yakınsama (convergence) sağlamak için **OneCycleLR** öğrenme oranı zamanlayıcısı kullanılmıştır. Ayrıca sistem, geçmiş fazlardan kalma Sınıf Ağırlıkları (Class Weights) stratejisi gözetilerek evrimleştirilmiştir.

## Veri Ön İşleme (Data Preprocessing)
- **Akıllı Dolgu (Smart Padding / Letterboxing):** Dermatolojik verilerin tıbbi bütünlüğünü bozmamak esastır. Orijinal görüntü formunu (en-boy oranını) bozup cilt dokularını esnetmek yerine, görsellerin kenarlarına kare yapacak şekilde siyah bantlar ekleyen Letterboxing yöntemi kullanılmıştır.
- **Veri Artırma (Data Augmentation):** Modelin başarısını ve genelleme yeteneğini artırmak için PyTorch `v2` Transforms kullanılarak Rastgele Döndürme (Rotation), Çevirme (Flip) ve Renk Titremesi (Color Jitter) gibi agresif artırma teknikleri uygulanmıştır.

## Dosya Yapısı ve Görevleri
Sistem modüler bir yaklaşımla, tek gerçeklik kaynağı prensibine uygun olarak inşa edilmiştir. Çalışma alanındaki her bir dosyanın tanımı aşağıdadır:

| Dosya Adı | Açıklama ve Görev |
| :--- | :--- |
| `config.py` | Sınıf isimleri, dönüşüm haritaları ve hiperparametreler (IMAGE_SIZE, vs.) için **"Tek Gerçeklik Kaynağı"** (Single Source of Truth). |
| `train.py` | Modeli eğiten, kayıp fonksiyonlarını hesaplayan ve optimizasyon mantığını çalıştıran ana eğitim döngüsü. |
| `model.py` | EfficientNet-B0 mimarisinin tanımlandığı ve sistem modelini oluşturan betik. |
| `dataset.py` | 500 görselle sınırlandırma (capping) mantığını yürüten ve verileri yükleyen özel **PyTorch Dataset** sınıfı. |
| `transforms.py` | Veri artırma (augmentation) algoritmalarını ve Akıllı Dolgu (Smart Padding) işlem hattını tanımlar. |
| `predict.py` | Eğitilmiş modeli kullanarak tek bir görsel üzerinde analiz yapan, UI entegrasyonuna uygun JSON çıktısı üreten çıkarım (inference) betiği. |
| `audit_model.py` | Eğitim sonrası şampiyon modelin Karmaşıklık Matrislerini (Confusion Matrix) çıkaran ve F1-Skorlarını test eden değerlendirme paketi. |
| `database_report.py` | Veri setinin sınıflar arası dağılımını, veri sağlığını ve oranlarını analiz edip grafikler üreten analiz modülü. |

## Performans Metrikleri
Modelin final 6 sınıflı "Balanced Expert" fazında değerlendirilmesi sonucunda ulaşılan başarımlar şöyledir:
- **Genel Doğruluk (Final Accuracy):** `%84.03`
- **Makro F1-Skoru (Macro F1-Score):** `0.81`
- **Sağlıklı Doku Başarısı:** 'Normal' sınıfı testlerinde modelin teşhis etme doğruluğu `%97 - %100` aralığında kusursuza yakın bir seviye sergilemektedir.
