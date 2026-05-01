# DermAI Projesi: Sunum Hazırlık Rehberi

Bu belge, DermAI modeli sunumunuz için bir hazırlık rehberi olarak hazırlanmıştır. Sunum sırasında projenin adımlarını, karşılaşılan sorunları ve bu sorunları nasıl çözdüğünüzü anlatırken bu notlardan faydalanabilirsiniz.

## 1. Giriş ve Projenin Amacı (Giriş)
- **Proje Nedir?** DermAI, farklı cilt hastalıklarını (Akne, Egzama, Melanom vb.) fotoğraflar üzerinden otomatik olarak sınıflandırabilen derin öğrenme tabanlı bir yapay zeka modelidir.
- **Problem:** Cilt hastalıklarının teşhisi uzmanlık gerektirir. Bu model, teşhis sürecini hızlandırmak ve ön değerlendirme yapmak amacıyla geliştirildi.

## 2. Veri Seti ve Hazırlık Süreci (Veri ve Ön İşleme)
- **Veri Sınıfları ve Kısıtlama (Capping):** Başlangıçta 20 civarı sınıf olsa da, model "Dengeli Uzman" (Balanced Expert) stratejisiyle en kritik **6 uzman sınıfa** (Akne, Egzama, Melanom vb.) odaklandı. Sınıf dengesizliğini önlemek için her sınıf **maksimum 500 görsel** ile sınırlandırıldı (250'den az görseli olanlar çıkarıldı).
- **Veri Dağılımı (Train/Test Split):** `split_data.py` kullanılarak veriler eğitim (train) ve test (test) olarak ikiye ayrıldı. Bu sayede modelin daha önce hiç görmediği veriler üzerindeki performansını güvenilir bir şekilde ölçebildik.
- **Görsel Boyutu ve Ön İşleme:** Görüntüler modelin standart giriş boyutu olan **224x224 piksel** boyutuna getirildi. Tıbbi görüntülerin en-boy oranını bozmamak (esnetmemek) için **Akıllı Dolgu (Smart Padding / Letterboxing)** tekniği uygulandı.
- **Veri Çoğaltma (Data Augmentation):** Modelin başarısını artırmak için PyTorch v2 Transforms ile rastgele döndürme (rotation), çevirme (flip) ve renk titremesi (color jitter) gibi veri çoğaltma işlemleri yapıldı.

## 3. Karşılaşılan Zorluklar ve Çözümler (Problemler ve Çözümler)
Sunumun en etkili kısmı burasıdır. Karşılaştığınız zorlukları ve onlarla nasıl başa çıktığınızı şöyle anlatabilirsiniz:

- **Sorun 1: Sınıf Dengesizliği (Class Imbalance)**
  - *Durum:* Bazı cilt hastalıklarının görseli diğerlerinden daha azdı. Model, sayısı çok olan hastalıkları daha çok tahmin etme eğilimindeydi.
  - *Çözüm:* Veri çoğaltma (Data Augmentation) tekniklerini kullandım (döndürme, parlaklık değiştirme vb.) ve modelin azınlık sınıflarını daha iyi öğrenmesini sağladım.

- **Sorun 2: Aşırı Öğrenme (Overfitting)**
  - *Durum:* Model eğitim verilerini çok iyi ezberliyor ancak yeni verilere geldiğinde hata yapıyordu.
  - *Çözüm:* Checkpoint (Kontrol Noktası) sistemini kurdum (`dermai_checkpoint_epoch_X.pth` dosyaları). Her epoch (döngü) sonunda modeli kaydettim ve doğrulama performansı en yüksek olan, yani genelleme yeteneği en iyi olan modeli seçtim. Dropout ve uygun öğrenme oranları (learning rate) kullandım.

- **Sorun 3: Modelin Dışa Aktarılması ve Gerçek Dünya Kullanımı (Deployment)**
  - *Durum:* Eğitilen modelin başkaları tarafından kolayca kullanılabilir ve test edilebilir olması gerekiyordu.
  - *Çözüm:* Projenin dağıtımı için özel bir `export` klasörü oluşturdum. İçerisine sadece ağırlıkları (`dermai.pth`) ve tahmin için gerekli olan minimum dosyaları (`predict.py`, `inference.py`) koyarak modeli modüler ve taşınabilir hale getirdim.

## 4. Model Mimarisi ve Eğitim Süreci (Eğitim Modülü)
- **Kullanılan Mimari (Backbone):** Modelin omurgası olarak hafif ama özellik çıkarımında çok güçlü olan **EfficientNet-B0** mimarisi kullanıldı. Bu mimarinin "Bileşik Ölçeklendirme" (Compound Scaling) özelliği sayesinde ağın derinliği ve genişliği donanım (VRAM) aşılmadan matematiksel olarak dengelendi.
- **Özelleştirilmiş Başlık (Classifier Head):** Sadece bizim hedeflediğimiz 6 klinik uzmanlık sınıfını tahmin etmek üzere ağın sonuna projemize özel dinamik bir sınıflandırma katmanı (classifier head) entegre edildi.
- **Eğitim Döngüsü ve Optimizasyon:** `train.py` ve `engine.py` ile stabil bir süreç yürütüldü. Hızlı ve istikrarlı ağ yakınsaması (convergence) için **OneCycleLR** öğrenme oranı zamanlayıcısı kullanıldı. Ayrıca **Sınıf Ağırlıkları (Class Weights)** gözetilerek sınıflar arası adaletsizlikler önlendi.
- **Parametreler:** `config.py` üzerinden batch size, epoch ve learning rate gibi hiperparametreleri merkezi olarak (Single Source of Truth) yönettim. Bu da deney yapmayı inanılmaz derecede hızlandırdı.

## 5. Model Başarısı ve Performans Metrikleri
Modelin 6 sınıflı final fazında ulaştığı başarı oranları şöyledir:
- **Genel Doğruluk (Final Accuracy):** `%84.03`
- **Makro F1-Skoru:** `0.81` (Tüm sınıflarda ortalama ne kadar istikrarlı olduğunu kanıtlar).
- **Sağlıklı Doku Başarısı:** Modelin, "Normal" (sağlıklı) cilt fotoğraflarını hastalık teşhisi yapmadan ayırt etme başarısı **%97 - %100** aralığında kusursuza yakın bir seviyededir.

## 6. Sonuç ve Gelecek Geliştirmeler (Kapanış)
- İş akışını başarılı bir şekilde tamamlayıp (`dermai.pth` olarak) modele ait ağırlık dosyalarını dışa aktardık.
- `predict.py` ile tek bir resmi verip anında tahmin (inference) alabiliyoruz.
- Gelecek adım olarak, bu modeli bir web API'sine veya mobil uygulamaya dönüştürmeyi (deployment) hedefliyoruz.
