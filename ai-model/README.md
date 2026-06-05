# DermAI: Derin Öğrenme Tabanlı Dermatolojik Tarama Sistemi

## Proje Başlığı ve Genel Bakış
**DermAI**, dermatolojik rahatsızlıkların tespitinde yüksek hassasiyetli klinik tanı desteği sağlamak üzere tasarlanmış, Derin Öğrenme (Deep Learning) tabanlı yenilikçi bir tarama aracıdır. Temel amacı, cilt lezyonlarını analiz ederek teşhis süreçlerinde doktorlara ve tıbbi uzmanlara güvenilir, hızlı ve yapay zeka destekli bir ikinci görüş (second opinion) sunmaktır.

## Sinir Ağı Mimarisi
- **EfficientNet-B0 Omurgası:** Modelin temel feature-extractor (özellik çıkarıcı) mimarisi olarak güçlü ve hafif yapısıyla **EfficientNet-B0** kullanılmıştır.
- **Bileşik Ölçeklendirme (Compound Scaling):** Bu mimarinin tercih edilme sebebi; ağın derinliğini, genişliğini ve giriş çözünürlüğünü matematiksel olarak dengeli bir şekilde ölçeklendirebilmesidir. Böylece VRAM (donanım kısıtlamaları) aşılmadan maksimum başarılı sonuç alınır.
- **Özelleştirilmiş Sınıflandırıcı Başlık (Classifier Head):** Sadece hedeflediğimiz uzmanlık sınıflarını tahmin etmek üzere ağın sonuna projemize özel dinamik bir sınıflandırma katmanı entegre edilmiştir.

## 12-Sınıflı Kapsamlı (Super-Expert) Stratejisi
- **Dinamik Veri Bölme ve Sınırlandırma (Capping):** Veri setindeki dengesizliği (class imbalance) kontrol altında tutmak için, eğitim ve doğrulama setleri oransal (80/20) olarak oluşturulur. Ancak aşırı öğrenmeyi (overfitting) önlemek amacıyla her bir sınıf için eğitim görselleri maksimum **1500**, doğrulama görselleri ise maksimum **500** ile sınırlandırılmıştır. "Diğer" (Other) gibi belirsiz gürültü sınıfları sistemden tamamen çıkarılarak tam 12 uzmanlık sınıfına odaklanılmıştır.
- **Optimizasyon ve Ağırlıklandırma:** Hızlı ve istikrarlı yakınsama (convergence) sağlamak için **OneCycleLR** öğrenme oranı zamanlayıcısı kullanılmıştır. Ayrıca sistem, geçmiş fazlardan kalma Sınıf Ağırlıkları (Class Weights) stratejisi gözetilerek evrimleştirilmiştir.

## Veri Seti Kategorileri (Hedeflenen Hastalık Sınıfları)
Sistem, gürültü sınıfları ("Other") veri havuzundan arındırıldıktan sonra tamamen klinik doğruluğu hedefleyen toplam **32,933** görselden oluşan **12 temel dermatolojik sınıf** üzerinde hizmet vermektedir. Güncel sistemde bulunan uzmanlık kategorileri aşağıdadır:
1. Melanocytic Nevi
2. Basal Cell Carcinoma
3. Acne or Rosacea
4. Melanoma
5. Normal (Sağlıklı Cilt)
6. Warts Molluscum and other Viral Infections
7. Benign Keratosis-like Lesions
8. Psoriasis pictures Lichen Planus and related diseases
9. Seborrheic Keratoses and other Benign Tumors
10. Tinea Ringworm Candidiasis and other Fungal Infections
11. Eczema
12. Atopic Dermatitis

## Veri Ön İşleme (Data Preprocessing)
- **Kısayol Öğrenme Engellemesi (Watermark Blackout):** Ham verilerin alt kısmında bulunan (örn: "© Dermnet.com") ticari filigranlar, modelin cilt dokusu yerine yazıyı ezberlemesini (shortcut learning) önlemek amacıyla piksel tensörü üzerinden özel bir algoritmayla kalıcı olarak maskelenerek (siyah blok kullanılarak) silinmiştir.
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
Modelin 12 sınıflı final yapısında, doğrulanmış veri seti üzerinde yapılan değerlendirmesi (Audit) sonucunda ulaşılan başarılar şöyledir:
- **Genel Doğruluk (Overall Accuracy):** `%95.44`
- **Makro F1-Skoru (Macro F1-Score):** `0.9508`
- **Ağırlıklı F1-Skoru (Weighted F1-Score):** `0.9546`
- **Kusursuz Teşhis Başarısı:** 'Melanoma', 'Normal', 'Tinea Ringworm Candidiasis' ve 'Melanocytic Nevi' sınıflarında model F1-Skoru olarak `%96 - %99` aralığına ulaşmaktadır.
- **Klinik Teşhis Zorluğu Analizi:** Beklendiği üzere, dermatoskop yardımı olmadan optik olarak ayrıştırılması dahi oldukça güç olan 'Basal Cell Carcinoma' ile 'Benign Keratosis-like Lesions' yapılarında sadece izafi bir karışıklık gözlemlenmiş; sistem klinik standartlarda rasyonel sınırlar içindeki görevini tescillemiştir.
