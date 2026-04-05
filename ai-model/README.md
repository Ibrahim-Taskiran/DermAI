# DermAI Yapay Zeka Modelleri - Dosya Açıklamaları

Bu dizin (`ai-model`), DermAI projesinin derin öğrenme (yapay zeka) tarafını oluşturan Python betiklerini içerir. Aşağıda bu klasörde bulunan her bir `.py` uzantılı dosyanın ne işe yaradığı detaylı bir şekilde açıklanmıştır:

### 1. `dataset.py`
PyTorch için özel bir veri kümesi (Dataset) sınıfı olan `DermAIDataset`'i tanımlar. Resimleri diskteki klasör mimarisinden okur, klasör adlarını sınıf etiketlerine dönüştürür. Hatalı dosyaları atlayıp eksik veri durumlarını yönetir ve modellere beslenmeye hazır veri çiftleri oluşturur.

### 2. `engine.py`
Modelin eğitim (train) ve doğrulama (validation) aşamalarının çekirdek döngülerini barındırır. Ekran kartı belleğinden tasarruf sağlamak için *Automatic Mixed Precision (AMP)* yapısını kullanır. Her epoch (çağ) sonrası model ağırlıklarını kaydeder (checkpoint) ve analiz için detaylı sınıflandırma raporları (Precision, Recall, F1) üretir.

### 3. `inference.py`
Dışarıdan verilen tek bir tıbbi görüntü üzerinde çıkarım (inference) yapmak için `predict_image` fonksiyonunu kullanır. Görüntüyü alır, modelden geçirir ve en yüksek olasılığa sahip 3 hastalık sınıfını, kullanıcı arayüzünde (UI) kolayca gösterilebilmesi için JSON/Sözlük formatında geri döndürür.

### 4. `main.py`
Sistemin çalışabilirliğini test etmek için kullanılan bir deneme (dry-run) betiğidir. Uzun sürecek asıl eğitimi başlatmadan önce; modelin, transformasyonların, sınıfların ve veri kümelerinin başarılı bir şekilde entegre olup olmadığını sahte (dummy) bir resim üreterek uçtan uca test eder.

### 5. `model.py`
Yapay zeka ağ mimarisinin yapılandırıldığı dosyadır. Temel özellik çıkarıcı olarak `EfficientNet-B0` ağını kullanır ve son sınıflandırma katmanını DermAI projesinin sınıflarına göre düzenler. Aynı zamanda hedef cihazın bellek (VRAM) sınırlarını aşmamak için model parametrelerini özetleyen yardımcı bir fonksiyon (summary) içerir.

### 6. `predict.py`
Komut satırı üzerinden pratik bir şekilde tahmin (predict) işlemlerini gerçekleştirmek için hazırlanmış çalıştırılabilir (CLI) bir betiktir. Kullanıcıdan bir görüntü yolu ve eğitilmiş model konumunu alır, sonucu değerlendirerek potansiyel tehlike düzeyine göre hastaya klinik bir tavsiye (recommendation) metni üretir.

### 7. `scan_dataset.py`
Veri setinin yapısını analiz eden bir yardımcı araçtır. Veri dizinlerini tarar ve hangi hastalık sınıfında (klasörde) kaç adet görüntü olduğunu sayarak terminale bir özet tablosu olarak basar. Sınıflar arası dengesizliği (class imbalance) tespit etmek için kullanılır.

### 8. `split_data.py`
Ham veri setini (Raw_Dataset) işlenebilir formlara ayırmak için kullanılır. Görüntüleri rastgele karıştırır ve varsayılan olarak %80 eğitim (train), %20 doğrulama (val/test) oranında bölerek ilgili klasörlere otomatik kopyalama işlemini gerçekleştirir.

### 9. `train.py`
Eğitim sürecini fiilen başlatan **ana (entry point)** betiktir. Epoch sayısı, batch size, öğrenme oranı (learning rate) gibi hiperparametreleri tanımlar. Modeli donanıma (GPU vb.) yükler, kayıp (loss) mekanizması ile sınıf dengesizlikleri için ağırlıklandırma yapar ve eğitim döngüsünü `engine.py` üzerinden ateşler.

### 10. `transforms.py`
Makine öğrenmesi modelleri için yapay veri artırma (Data Augmentation) ve ön işleme (Preprocessing) tekniklerini içerir. Eğitim setindeki görüntüleri döndürme, aydınlatma, pad ekleme ve yeniden boyutlandırma gibi işlemlerden (`get_train_transforms`) geçirirken analiz sırasında uygulanacak kısıtlı doğrulama yöntemlerini (`get_val_transforms`) belirler.
