# DermAI: RESTful API Entegrasyonlu Derin Öğrenme Tabanlı Mobil Dermatolojik Tarama Sistemi

**İbrahim Taşkıran¹, Kerem Selçuk², Aybüke Türk³, AHD Hadi Said Alkaddour⁴**

*Bilgisayar Mühendisliği Bölümü*
*[Üniversite Adı], [Şehir, Ülke]*

¹ Mobil Uygulama Geliştirme — ibrahim@example.com
² Backend ve API Geliştirme — kerem@example.com
³ Tıbbi İçerik ve Test — aybuke@example.com
⁴ Yapay Zeka ve Makine Öğrenmesi — hadi@example.com

---

> **Özet** — Dermatolojik hastalıklar dünya genelinde önemli bir nüfusu etkilemesine karşın, uzman hekime erişim birçok bölgede oldukça kısıtlı kalmaktadır. Bu çalışmada, ince ayarı yapılmış bir EfficientNet-B0 evrişimli sinir ağını FastAPI tabanlı bir REST API arka ucu ve bir Android mobil uygulamasıyla bütünleştiren mobil öncelikli bir cilt hastalığı tespit sistemi olan DermAI sunulmaktadır. Önerilen sistem; kullanıcıların bir cilt lezyonu görüntüsü çekmesine veya seçmesine, bunu yerel bir çıkarım sunucusuna iletmesine ve klinik yönelimli bakım önerileriyle birlikte ilk 3 ayırıcı tanıyı tek bir etkileşimde almasına olanak tanımaktadır. Sınıf başına 500 görüntü sınırı ve OneCycleLR zamanlayıcısı kullanan "Dengeli Uzman" eğitim stratejisi, altı dermatolojik kategori üzerinde **%84,03** genel doğruluk ve **0,81** makro ortalamalı F1 skoru elde etmektedir. Sistem mimarisi, katı sorumluluk ayrımı ilkesiyle tasarlanmıştır: yapay zeka çıkarım katmanı, HTTP servis katmanı ve sunum katmanı birbirinden bağımsız çalışarak herhangi bir bileşenin modüler olarak değiştirilmesine veya ölçeklenmesine imkân tanımaktadır. Deneysel sonuçlar ve sistem tasarım kararları ayrıntılı biçimde aktarılmakta; makalede klinik düzeyde konuşlandırmaya yönelik kısıtlamalar, etik değerlendirmeler ve gelecekteki yönelimler de ele alınmaktadır.

> **Anahtar Kelimeler** — derin öğrenme, cilt hastalığı tespiti, EfficientNet-B0, mobil sağlık, FastAPI, REST API, Android, transfer öğrenme, dermoskopi, klinik karar destek.

---

## I. GİRİŞ

Cilt hastalıkları, ekzama, akne ve aktinik keratoz gibi durumların yıllık olarak yüz milyonlarca hastayı etkilemesiyle dünya genelinde en yaygın tıbbi durum kategorilerinden birini oluşturmaktadır [1]. Bu yaygınlığa karşın, dermatolojik uzmanlığa erişim eşitsiz bir dağılım sergilemektedir; pek çok gelişmekte olan bölgede hasta-dermatolog oranı 50.000:1'i aşabilmektedir [2]. Dermatolojik durumların erken ve doğru biçimde belirlenmesi kritik öneme sahiptir; özellikle bazal hücreli karsinom ve aktinik keratoz gibi malign lezyonlarda gecikmiş tanı prognozları önemli ölçüde kötüleştirmektedir [3].

Yüksek kaliteli akıllı telefon kameralarının hızla yaygınlaşması, evrişimli sinir ağlarındaki (CNN) ilerlemeler ve büyük dermatolojik görüntü veri kümelerinin erişilebilirliği; çıkarım sırasında bulut bağlantısı gerektirmeksizin hazır donanım üzerinde çalışabilen, erişilebilir, yapay zeka destekli tarama araçları geliştirme fırsatı yaratmıştır.

Bu çalışmada, mobil tabanlı dermatolojik tarama için eksiksiz bir uçtan uca sistem olan **DermAI** tanımlanmaktadır. Çalışmanın katkıları şu şekilde özetlenebilir:

1. Altı klinik açıdan anlamlı dermatolojik kategori üzerinde "Dengeli Uzman" stratejisiyle eğitilmiş, %84,03 doğruluk ve 0,81 makro F1 skoru elde eden ince ayarlı bir EfficientNet-B0 modeli.

2. Tam girdi doğrulama, hata yönetimi ve tekil örnek model yönetimiyle yapay zeka çıkarım katmanını mobil sunum katmanına bağlayan, FastAPI/Python ile geliştirilmiş üretime hazır bir RESTful API sunucusu.

3. Görüntü edinme, vücut bölgesi etiketleme, analiz ve boylamsal takip günlüğü gibi eksiksiz bir kullanıcı yolculuğu sunan Android mobil uygulaması (Kotlin/Jetpack Compose).

4. Modellenmiş altı hastalık kategorisinin tamamı için duruma özgü bakım önerileri, tedavi rehberliği ve aciliyet düzeyine göre sınıflandırılmış doktor uyarıları içeren klinik içerik veri tabanı.

5. Bulut altyapısı gerektirmeksizin gerçek cihaz testine olanak tanıyan, Android 13+ `content://` URI işleme ve yerel WiFi tabanlı konuşlandırma için belgelenmiş entegrasyon stratejisi.

Makalenin geri kalan bölümleri şu şekilde düzenlenmiştir: Bölüm II ilgili çalışmaları incelemektedir. Bölüm III genel sistem mimarisini açıklamaktadır. Bölüm IV makine öğrenmesi modeli tasarımını ve eğitim metodolojisini ele almaktadır. Bölüm V arka uç API tasarımını sunmaktadır. Bölüm VI Android uygulamasını kapsamaktadır. Bölüm VII deneysel sonuçları raporlamaktadır. Bölüm VIII kısıtlamaları ve etik değerlendirmeleri tartışmaktadır. Bölüm IX ise makaleyi sonuçlandırmaktadır.

---

## II. İLGİLİ ÇALIŞMALAR

### A. Cilt Lezyonu Sınıflandırmasında Derin Öğrenme

Esteva ve ark. [4], 129.450 klinik görüntü üzerinde eğitilmiş derin bir CNN'nin cilt kanserini sertifikalı dermatologlarla karşılaştırılabilir bir düzeyde sınıflandırabildiğini göstermiş ve yapay zeka destekli dermatolojide bir dönüm noktası oluşturmuştur. ISIC (Uluslararası Cilt Görüntüleme İşbirliği) yarışma serisinin sonraki çalışmaları, lezyon segmentasyonu ve sınıflandırması için kıyaslama veri kümeleri ve değerlendirme protokolleri oluşturmuştur [5].

Tan ve Le [6] tarafından tanıtılan EfficientNet, sabit bir katsayılar kümesi kullanarak ağ derinliğini, genişliğini ve çözünürlüğünü düzgün biçimde ölçeklendiren bir bileşik ölçeklendirme yöntemi uygulamaktadır. Temel varyant olan EfficientNet-B0, ResNet-50 veya DenseNet-121 gibi karşılaştırılabilir mimarilere kıyasla önemli ölçüde daha az parametre kullanarak ImageNet üzerinde en üst düzey doğruluk elde etmektedir; bu da onu kaynak kısıtlı ortamlarda konuşlandırma için uygun hale getirmektedir.

Literatürde çeşitli mobil dermoskopi uygulamaları önerilmiştir. SkinVision [7] ve benzeri ticari sistemler CNN tabanlı yaklaşımlar kullanmakla birlikte bulut çıkarımına dayanmaktadır. DermAI; çıkarımı sunucu tarafında yerel olarak gerçekleştirerek üçüncü taraf bulut sağlayıcılarına veri iletimini ortadan kaldırmakta ve gecikmeyi azaltmaktadır.

### B. Mobil Sağlık (mHealth) Mimarileri

Cihaz üzerinde veya yerel sunucu yapay zekasını mobil bir ön uçla birleştiren mobil sağlık sistemleri; diyabetik retinopati taramasından [8] tüberküloz tespitine [9] uzanan pek çok bağlamda araştırılmıştır. Yaygın zorluklar arasında heterojen görüntü girdilerinin işlenmesi (değişen aydınlatma, çözünürlük, kamera optikleri), model boyutu kısıtlamalarının yönetimi ve çıkarım gecikmesine karşın duyarlı kullanıcı deneyiminin sağlanması yer almaktadır. DermAI, bu zorlukları; iletimden önce istemci tarafında görüntü optimizasyonuyla birlikte önceden yüklenmiş tekil örnek model kullanan sunucu tarafı çıkarımı aracılığıyla ele almaktadır.

### C. Makine Öğrenmesi Sistemleri için REST API Tasarımı

Starlette ve Pydantic üzerine inşa edilen FastAPI çerçevesi [10], eşzamansız istek işleme, otomatik OpenAPI dokümantasyonu oluşturma ve Python'ın tür açıklama sistemiyle sıkı entegrasyonu sayesinde makine öğrenmesi servislemede tercih edilen bir seçenek haline gelmiştir. Flask tabanlı yaklaşımlarla karşılaştırıldığında FastAPI, eş zamanlı yük altında üstün verim ve görüntü tabanlı API'ler için vazgeçilmez olan çok parçalı dosya yüklemeleri için yerel destek sunmaktadır.

---

## III. SİSTEM MİMARİSİ

### A. Genel Bakış

DermAI; (1) **Android mobil istemci**, (2) **FastAPI REST API sunucusu** ve (3) **yapay zeka çıkarım modülü**nden oluşan üç katmanlı bir mimari benimsemektedir. Şekil 1, üst düzey sistem mimarisini göstermektedir.

```
Şekil 1 — DermAI Üç Katmanlı Sistem Mimarisi
[Bkz: fig1_system_architecture.png]
```

Üç katman, yalnızca iyi tanımlanmış arayüzler aracılığıyla iletişim kurmaktadır: mobil istemci, görüntü yükleme için `multipart/form-data` ve yapılandırılmış yanıtlar için `application/json` kullanan HTTP/1.1 üzerinden arka uçla iletişim kurmaktadır. Arka uç, yapay zeka kod tabanının fiziksel ayrımını korurken çalışma zamanında sıkı eşleşmeyi mümkün kılmak için `sys.path` enjeksiyonundan yararlanan doğrudan Python modül içe aktarmaları aracılığıyla çıkarım modülüyle iletişim kurmaktadır.

### B. Tasarım İlkeleri

Mimari, üç tasarım ilkesi tarafından yönetilmektedir:

**Sorumlulukların Ayrılması:** Yapay zeka modeli eğitim kod tabanı (`ai-model/`), API sunucusu (`backend/`) ve mobil uygulama (`mobile-app/`) paylaşılan kaynak dosyası olmaksızın bağımsız modüller olarak korunmaktadır. Bu durum, herhangi bir katmanın bağımsız olarak sürümlenmesini, test edilmesini ve değiştirilmesini mümkün kılmaktadır.

**Tekil Örnek Model Yükleme:** EfficientNet-B0 modeli, FastAPI'nin yaşam döngüsü bağlam yöneticisi aracılığıyla sunucu başlangıcında RAM'e bir kez yüklenmektedir. Sonraki tüm çıkarım istekleri aynı model örneğini paylaşarak istek başına yükleme yükünü (CPU üzerinde yaklaşık 2–4 saniye) ortadan kaldırmaktadır.

**Güvenli Bozunma:** Model kontrol noktası dosyası eksik veya bozuk ise sunucu, çıkarım istekleri için HTTP 503 döndürürken tanı uç noktaları (`GET /health`) için sağlıklı kalarak bozunmuş bir durumda başlamaktadır. Bu durum, kısmi konuşlandırmalar sırasında tam sistem arızasını önlemektedir.

### C. Veri Akışı

Tek bir analiz isteği için eksiksiz veri akışı şu şekilde gerçekleşmektedir:

1. Kullanıcı, Android cihazında bir görüntü seçer veya çeker.
2. `ImageOptimizer`, görüntüyü sıkıştırıp yeniden boyutlandırarak uygulama önbelleğine yazar.
3. `ApiAnalysisRepository`, görüntü baytlarını Android'in `ContentResolver`'ı aracılığıyla okur (`content://` ve `file://` URI şemalarını destekler) ve bir `multipart/form-data` HTTP POST isteği oluşturur.
4. Retrofit, isteği `POST http://{SUNUCU_IP}:8000/predict` adresine iletir.
5. FastAPI arka ucu MIME türünü (JPEG/PNG/WEBP) ve dosya boyutunu (≤10 MB) doğrular.
6. `ModelService.predict()`, baytları geçici bir dosyaya yazar, yapay zeka modülünden `predict_image()`'i çağırır ve tamamlanmanın ardından geçici dosyayı siler.
7. EfficientNet-B0 modeli ileri geçiş çalıştırır, softmax uygular ve olasılıklarla birlikte ilk 3 sınıf tahminini döndürür.
8. `AdviceService.get_advice_for_disease()`, en yüksek tahmini yapılandırılmış bir tavsiye nesnesine eşler.
9. Bir `AnalysisResponse` JSON nesnesi serileştirilir ve mobil istemciye döndürülür.
10. Android `ResultScreen`, hastalık adını, olasılık halka grafiğini, ilk 3 tahmin çubuklarını ve bakım/uyarı kartlarını görüntüler.

Şekil 3, bu etkileşim için UML sıralı diyagramını sunmaktadır.

```
Şekil 3 — UML Sıralı Diyagramı: DermAI Analiz İstek-Yanıt Akışı
[Bkz: fig3_sequence_diagram.png]
```

---

## IV. YAPAY ZEKA MODELİ TASARIMI VE EĞİTİMİ

### A. Veri Kümesi ve Sınıf Seçimi

Veri kümesi, yaklaşık 20 dermatolojik kategori üzerinde görüntüler içeren halka açık bir cilt hastalığı görüntü koleksiyonundan türetilmiştir. Odaklanmış, klinik açıdan anlamlı bir sınıflandırıcı üretmek için "Dengeli Uzman" seçim stratejisi uygulanmıştır:

**Sınıf Filtreleme:** Yalnızca minimum 250 görüntüye sahip sınıflar korunarak genelleme için yeterli istatistiksel temsil sağlanmıştır.

**Sınıf Sınırlandırma:** Korunan her sınıf 500 görüntüyle sınırlandırılmıştır. Bu üst sınır, dengesiz tıbbi görüntüleme veri kümelerinde iyi belgelenmiş bir olgu olan baskın sınıfların modelin karar sınırını etkilemesini önlemektedir [11].

Ortaya çıkan veri kümesi altı sınıftan oluşmaktadır:

| Sınıf | Açıklama | Risk Düzeyi |
|---|---|---|
| Egzama (Atopik Dermatit) | Kronik inflamatuar cilt durumu | Orta |
| Normal | Sağlıklı cilt dokusu | Temel |
| Akne ve Rosacea | Sebase ve inflamatuar durumlar | Düşük–Orta |
| Aktinik Keratoz / Malign Lezyonlar | Kansere öncül ve malign durumlar | Yüksek |
| Işık Hastalıkları / Pigmentasyon Bozuklukları | Vitiligo, hiperpigmentasyon varyantları | Düşük–Orta |
| Siğiller, Molluscum / Viral Enfeksiyonlar | HPV bağlantılı ve viral cilt lezyonları | Düşük |

**Eğitim/Doğrulama Ayrımı:** Sınıf başına %80/%20 tabakalı bölünme uygulanarak eğitim dağılımını temsil eden dengeli bir doğrulama kümesi elde edilmiştir.

### B. Model Mimarisi

Omurga, ImageNet üzerinde ön eğitimli **EfficientNet-B0**'dır [6]. Son sınıflandırma katmanı (`classifier[1]`), 1.280 girdi özelliğinden 6 çıktı logitine eşleme yapan özel bir doğrusal katmanla değiştirilmiştir:

```
EfficientNet-B0 Omurgası (dondurulmuş/ince ayarlı)
    └─ Özellikler: Evrişim + MBConv blokları
    └─ AdaptiveAvgPool2d
    └─ Dropout(0.2)
    └─ Linear(1280 → 6)  [özel baş]
```

Bu mimari, ImageNet'ten öğrenilen zengin özellik temsillerini korurken çıktı uzayını hedef dermatolojik taksonomiye uyarlamaktadır. Toplam eğitilebilir parametre sayısı: yaklaşık 4,01 milyon.

### C. Ön İşleme Hattı

Temel bir ön işleme yeniliği **Mektup Kutusu (Akıllı Dolgu)** yaklaşımıdır. Lezyon görüntülerinin en-boy oranını bozan naif bir yeniden boyutlandırma veya merkez kırpma uygulamak yerine, görüntüler önce 224×224'e yeniden boyutlandırılmadan önce kare bir sınırlayıcı kutu oluşturacak biçimde siyah piksellerle doldurulmaktadır. Bu işlem, tanısal açıdan önemli olan lezyon morfolojisini korumaktadır (örn. melanomda ABCDE kriterlerinde sınır düzensizliği).

**Eğitim dönüşüm hattı** şunları uygulamaktadır:
- Akıllı Dolgu → Yeniden Boyutlandırma(224) → Merkez Kırpma(224)
- RastgeleRotasyon(±45°)
- RenkTitremesi(parlaklık=0,3, kontrast=0,3)
- RastgeleYatayÇevirme(p=0,5)
- RastgeleDikeyÇevirme(p=0,5)
- GörüntüyeDönüştür → float32TipineDönüştür → Normalize(ImageNet μ/σ)

**Doğrulama dönüşüm hattı** stokastik veri artırmayı dışarıda bırakarak yalnızca şunları uygulamaktadır:
- Akıllı Dolgu → Yeniden Boyutlandırma(224) → Merkez Kırpma(224)
- GörüntüyeDönüştür → float32TipineDönüştür → Normalize(ImageNet μ/σ)

### D. Eğitim Konfigürasyonu

| Hiperparametre | Değer |
|---|---|
| Optimize Edici | AdamW |
| Öğrenme Oranı Zamanlayıcı | OneCycleLR |
| Maksimum Öğrenme Oranı | 1e-3 |
| Eğitim Dönemi | 20 |
| Grup Boyutu | 32 |
| Kayıp Fonksiyonu | CrossEntropyLoss |
| Karışık Hassasiyet | AMP (torch.cuda.amp) |
| Kontrol Noktası Stratejisi | En iyi doğrulama doğruluğunu kaydet |

**OneCycleLR**, özellikle transfer öğrenme ortamlarında sabit öğrenme oranı zamanlamalarıyla karşılaştırıldığında hızlı yakınsama ve gelişmiş genelleme sağlama kapasitesiyle tercih edilmiştir [12]. Zamanlayıcı, eğitim adımlarının ilk %30'unda öğrenme oranını doğrusal olarak maksimuma artırmakta, ardından kalan süre boyunca kosinüs tavlamasıyla neredeyse sıfıra indirmektedir.

Şekil 2, eksiksiz model eğitim hattını göstermektedir.

```
Şekil 2 — DermAI için EfficientNet-B0 Eğitim Hattı
[Bkz: fig2_ml_pipeline.png]
```

### E. Çıkarım

Çıkarım zamanında `predict_image()` işlevi şu adımları izlemektedir:
1. Görüntüyü PIL aracılığıyla yükler ve doğrulama dönüşüm hattını uygular.
2. Grup boyutu ekler ve tensörü yapılandırılmış cihaza aktarır.
3. `torch.no_grad()` bağlamında ileri geçiş çalıştırır.
4. Kalibre edilmiş olasılık tahminleri elde etmek için `F.softmax(outputs, dim=1)` uygular.
5. `torch.topk` aracılığıyla ilk 3 `(sınıf_adı, olasılık)` çiftini döndürür.

Çıktı, hem Android `AnalysisResponse` veri modeli hem de doğrudan JSON serileştirmesiyle uyumlu yapılandırılmış bir sözlüktür:

```json
{
  "success": true,
  "top_prediction": { "disease": "Eczema (Atopic Dermatitis)", "probability": 0.8712 },
  "top3_predictions": [
    { "disease": "Eczema (Atopic Dermatitis)", "probability": 0.8712 },
    { "disease": "Normal", "probability": 0.0891 },
    { "disease": "Warts Molluscum and other Viral Infections", "probability": 0.0241 }
  ],
  "advice": {
    "care": "Cildinizi günde en az iki kez...",
    "recommendation": "Parfümsüz, hipoalerjenik nemlendirici...",
    "doctor_warning": "Kaşıntı uyku düzeninizi bozuyorsa..."
  }
}
```

---

## V. ARKA UÇ API TASARIMI

### A. Teknoloji Yığını

Arka uç sunucu, aşağıdaki birincil bağımlılıklar kullanılarak **Python 3.13**'te gerçekleştirilmiştir:

| Kütüphane | Sürüm | Rol |
|---|---|---|
| FastAPI | 0.111.0 | ASGI web çerçevesi |
| Uvicorn | 0.30.0 | ASGI sunucusu (HTTP/1.1) |
| Pydantic | 2.x | İstek/yanıt şeması doğrulama |
| PyTorch | 2.x | Model çıkarım çalışma zamanı |
| python-multipart | 0.0.9 | Çok parçalı form verisi ayrıştırma |
| Pillow | 11.x | Görüntü kod çözme |
| torchinfo | 1.8.0 | Model mimarisi incelemesi |

### B. Modül Yapısı

Arka uç, katı modül sınırlarıyla katmanlı bir mimariyi izlemektedir:

```
backend/
├── main.py            ← Uygulama giriş noktası, yaşam döngüsü, CORS
├── core/config.py     ← .env'den merkezi ayarlar
├── schemas/response.py ← Pydantic modeller (AnalysisResponse, Prediction, Advice)
├── services/
│   ├── model_service.py   ← Tekil örnek model yükleyici ve çıkarım köprüsü
│   └── advice_service.py  ← Hastalıktan tavsiyeye arama tablosu
└── routers/predict.py ← HTTP uç nokta tanımları (/predict, /health)
```

### C. API Uç Noktaları

**POST /predict**

Birincil uç nokta, `file` adlı tek bir dosya alanıyla `multipart/form-data` isteğini kabul etmektedir. İşleme hattı, dört aşamalı bir doğrulama-ardından-çıkarım deseni uygulamaktadır:

```
Aşama 1: Model kullanılabilirlik denetimi (model yüklü değilse HTTP 503)
Aşama 2: MIME türü doğrulama — {image/jpeg, image/png, image/webp}
Aşama 3: Dosya boyutu denetimi — maksimum 10 MB
Aşama 4: Çıkarım → tavsiye arama → yanıt serileştirme
```

HTTP durum kodları RFC 7231 semantiğini izlemektedir: başarılı çıkarım için 200 OK, geçersiz girdiler için 400 Bad Request, model yüklü değilse 503 Service Unavailable ve beklenmedik istisnalar için 500 Internal Server Error.

**GET /health**

Sunucu canlılığını ve model hazırlığını gösteren hafif bir JSON nesnesi döndürür:

```json
{ "status": "ok", "model_loaded": true, "message": "Sunucu çalışıyor, model hazır." }
```

**GET /**

Sürüm, uç nokta dizini ve model durumu dahil API meta verilerini döndürür.

### D. Android İstemcisiyle Şema Uyumu

Kritik bir mühendislik kısıtı, Python Pydantic modelleri ile Android uygulamasındaki Kotlin `data class` tanımları arasında katı alan adı eşitliğinin sağlanmasıydı. Eşleme şu şekildedir:

| Python (Pydantic) | Kotlin (@SerializedName) | JSON Anahtarı |
|---|---|---|
| `top_prediction` | `topPrediction` | `top_prediction` |
| `top3_predictions` | `top3Predictions` | `top3_predictions` |
| `doctor_warning` | `doctorWarning` | `doctor_warning` |

Android tarafındaki Gson'ın `@SerializedName` anotasyonu, Kotlin alan adlarının camelCase kuralını izlemesine olanak tanırken JSON aktarım formatı snake_case kullanmakta; böylece özel bir serileştirici gerektirmeksizin Python adlandırma standartlarıyla uyumluluk sağlanmaktadır.

### E. Model Servisi ve Tekil Örnek Deseni

`ModelService`, modül düzeyinde örnekleme yoluyla Tekil Örnek tasarım desenini uygulamaktadır. Tek `model_service` örneği içe aktarma zamanında oluşturulur ve tüm istek işleyicileri arasında paylaşılmaktadır. Bu durum, EfficientNet-B0 kontrol noktası için yaklaşık 16 MB gibi zorlu bir miktarda olacak istek başına model yüklemenin bellek ve gecikme yükünü ortadan kaldırmaktadır.

`load()` yöntemi, birden fazla kontrol noktası formatını işlemektedir: `engine.py` tarafından üretilen `model_state_dict` anahtarları içeren sözlükler ile ham `state_dict` nesneleri; bu durum eğitim yinelemeleri arasında geriye dönük uyumluluk sağlamaktadır.

### F. Klinik Danışmanlık Sistemi

`AdviceService`, altı hastalık kategorisinin her birini üç bölümlü bir tavsiye nesnesine eşleyen statik bir arama tablosu uygulamaktadır:

- **care (bakım)**: Günlük cilt yönetim talimatları.
- **recommendation (öneri)**: Reçetesiz veya klinik tedavi önerileri.
- **doctor_warning (doktor uyarısı)**: Uzman yönlendirmesi için aciliyet düzeyine göre sınıflandırılmış tetikleyici koşullar.

Aktinik Keratoz / Malign Lezyonlar kategorisi için `doctor_warning` alanı, bu kategorinin yüksek klinik riskini yansıtarak açık bir aciliyet işaretiyle ("ACİL UYARI") ön plana alınmakta ve bir dermatolog veya onkologla derhal görüşme tavsiye edilmektedir.

---

## VI. ANDROID UYGULAMASI

### A. Teknoloji Yığını

Android uygulaması, bildirimsel UI oluşturma için **Jetpack Compose** kullanan **Kotlin**'de geliştirilmiştir. Temel kütüphaneler şunlardır:

| Kütüphane | Rol |
|---|---|
| Hilt (Dagger) | Bağımlılık enjeksiyonu |
| Retrofit + OkHttp | API iletişimi için HTTP istemcisi |
| CameraX | Kamera önizlemesi ve görüntü yakalama |
| Coil | Eş zamansız görüntü yükleme |
| Sceneview | 3B vücut haritası oluşturma |
| Navigation Compose | Ekran navigasyon grafiği |
| Core SplashScreen | Android 12+ açılış ekranı API'si |

### B. Uygulama Akışı

Navigasyon grafiği şu kullanıcı yolculuğunu tanımlamaktadır:

```
MetadataForm (ilk kullanım) ──► Görsel Seçimi
                                       │
                        ┌──────────────┼──────────────┐
                     Kamera          Galeri          Takip
                        └──────────────┤
                                   VücutHaritası
                                       │
                                    Analiz
                                       │
                                    Sonuç
                                       │
                               (Takibe Kaydet)
```

İlk kez kullanan kullanıcılar hasta profili oluşturmak için `MetadataFormScreen`'e yönlendirilmektedir (yaş, cinsiyet, cilt tipi). Tekrar eden kullanıcılar doğrudan `ImageSelectionScreen`'e ilerlemektedir.

### C. Görüntü İşleme ve Android 13+ Uyumluluğu

Android 13'ün Fotoğraf Seçici API'sinin doğrudan dosya yolları yerine `content://media/picker_get_content/...` URI'ları döndürmesinden kaynaklanan önemli bir uyumluluk sorunu ortaya çıkmıştır. Standart `java.io.File(yol)` yapıcısı bu URI'ları çözememekte ve çalışma zamanında `FileNotFoundException` üretmektedir.

Çözüm, herhangi bir URI şemasından görüntü baytlarını okumak için Android'in `ContentResolver.openInputStream(uri)` yöntemini kullanmaktadır:

```kotlin
val uri: Uri = when {
    imagePath.startsWith("content://") || imagePath.startsWith("file://") ->
        Uri.parse(imagePath)
    else ->
        Uri.fromFile(File(imagePath))  // ImageOptimizer'dan düz yol
}
val imageBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
```

Bu üç dallı strateji; Fotoğraf Seçici'den gelen `content://` URI'larını, FileProvider'dan gelen `file://` URI'larını ve uygulama içi `ImageOptimizer` yardımcı programının ürettiği düz dosya yollarını işlemektedir.

### D. Bağımlılık Enjeksiyonu Mimarisi

`AppModule` ve `NetworkModule` (Hilt `@InstallIn(SingletonComponent::class)`), şunlar için uygulama kapsamlı tekil örnekler sağlamaktadır:
- Yapılandırılabilir zaman aşımlarıyla (bağlantı: 30sn, okuma: 30sn, yazma: 30sn) `OkHttpClient`
- Yapılandırılmış sunucu temel URL'sine bağlı `Retrofit` örneği
- `DermAIApiService` Retrofit arayüz proxy'si
- `ContentResolver` erişimi için `ApplicationContext` ile enjekte edilen `ApiAnalysisRepository`
- `SharedPreferences` + Gson aracılığıyla yerel kalıcılık için `TrackerRepository`

---

## VII. DENEYSEL SONUÇLAR

### A. Model Performansı

EfficientNet-B0 modeli, NVIDIA RTX 3050 (4 GB VRAM) donanımlı bir sistemde 20 dönem boyunca eğitilmiştir. Son kontrol noktası (dönem 20), tutulmuş doğrulama kümesinde şu metrikleri elde etmiştir:

| Metrik | Değer |
|---|---|
| Genel Doğruluk | **%84,03** |
| Makro F1 Skoru | **0,81** |
| Normal Sınıf Doğruluğu | %97–100 |
| Ağırlıklı Hassasiyet | ~0,85 |
| Ağırlıklı Geri Çağırma | ~0,84 |

"Normal" sınıfındaki olağanüstü yüksek doğruluk (%97–100), modelin sağlıklı cilt dokusunu tanıma kapasitesinin güçlü olduğunu yansıtmaktadır; bu durum, uzman yönlendirmesinden önce patolojiyi doğru biçimde dışlama açısından negatif tarama kapasitesi olarak klinik değer taşımaktadır.

### B. Sınıf Bazında Analiz

"Aktinik Keratoz / Malign Lezyonlar" sınıfı, bu kategorideki lezyon sunumlarının heterojenliği nedeniyle (aktinik keratoz, bazal hücreli karsinom, skuamöz hücreli karsinom ve melanom bir arada gruplandırılmıştır) en yüksek tanısal karmaşıklığı sergilemektedir. Buna karşın model anlamlı bir farklılaşma sağlamakta; bu durum, bu sınıftaki tüm pozitif tahminler için danışmanlık sistemine yerleştirilen acil doktor uyarısını haklı kılmaktadır.

"Egzama" ve "Akne ve Rosacea" sınıfları, birbirleriyle en yüksek karışıklığı sergilemektedir; bu durum, söz konusu durumların örtüşen görsel özellikler (eritem, papüller) paylaştığı ve sıklıkla birlikte görüldüğüne ilişkin klinik gözlemlerle tutarlılık göstermektedir.

### C. Sistem Gecikmesi

Uçtan uca çıkarım gecikmesi (HTTP POST alımından JSON yanıtına kadar), tüketici sınıfı bir dizüstü bilgisayarda (Intel Core i7, 16 GB RAM, GPU yok) aşağıdaki yaklaşık profille ölçülmüştür:

| Aşama | Gecikme (ms) |
|---|---|
| Görüntü kod çözme + dönüşüm | 30–80 |
| EfficientNet-B0 ileri geçişi (CPU) | 200–400 |
| Softmax + TopK | <5 |
| Tavsiye arama | <1 |
| JSON serileştirme | <5 |
| **Toplam (yaklaşık)** | **250–500** |

Bu gecikme profili, gerçek zamanlı akış çıkarımı yerine saniyenin altında yanıtların beklendiği bir karar destek kullanım senaryosu için kabul edilebilir düzeydedir.

### D. Temel Çizgiyle Karşılaştırma

| Model | Doğruluk | Makro F1 | Parametre Sayısı |
|---|---|---|---|
| ResNet-18 (temel çizgi) | ~%72 | ~0,69 | 11,7M |
| MobileNetV2 | ~%79 | ~0,76 | 3,4M |
| **EfficientNet-B0 (DermAI)** | **%84,03** | **0,81** | **4,01M** |

EfficientNet-B0, hem doğruluğun hem de hesaplama verimliliğinin kısıtlandığı tıbbi görüntü sınıflandırma görevlerinde bileşik ölçeklendirmenin uygunluğunu doğrulayarak orta düzeyde parametre sayısıyla en yüksek doğruluğu elde etmektedir.

---

## VIII. KISITLAMALAR VE ETİK DEĞERLENDİRMELER

### A. Klinik Kısıtlamalar

DermAI, açıkça bir **karar destek aracı** olarak tasarlanmış olup tanı aracı niteliği taşımamaktadır. Sistemin çıktıları profesyonel tıbbi değerlendirmenin yerini tutacak şekilde kullanılmamalıdır. Çeşitli klinik kısıtlamalar geçerliliğini korumaktadır:

1. **Görüntü Kalitesine Bağımlılık:** Model, derlenmiş veri kümesi görüntüleri üzerinde eğitilmiştir. Yetersiz aydınlatmada, eğik açılarda veya hareket bulanıklığıyla gerçek dünyada çekilen fotoğraflar sınıflandırma doğruluğunu düşürebilir.

2. **Sınıf Kapsamı:** Altı sınıflı taksonomi yaygın durumları kapsamakla birlikte nadir dermatozları, otoimmün büllöz hastalıkları, ilaç döküntülerini ve kutanöz belirtili sistemik tezahürleri dışarıda bırakmaktadır.

3. **Popülasyon Yanlılığı:** Eğitim veri kümesi demografik açıdan temsil edici değilse (örn. daha koyu ten tonlarının yetersiz temsili) model performansı hasta popülasyonları arasında eşitsizlik gösterebilir [13].

4. **Dermoskopi ve Klinik Fotoğrafçılık Karşılaştırması:** Dermoskopik görüntüler (büyütülmüş, polarize ışık), standart klinik fotoğraflara kıyasla daha zengin tanısal bilgi sunmaktadır. DermAI standart akıllı telefon fotoğrafları üzerinde çalışmakta; bu durum modelin kullanabileceği görsel özellikleri kısıtlamaktadır.

### B. Etik Değerlendirmeler

Tüm tahminlere tavsiye metninde açık bir feragatname eşlik etmektedir: "Bu analiz yapay zeka tarafından yapılmıştır ve kesin bir tıbbi teşhis niteliği taşımamaktadır."

Aktinik Keratoz / Malign Lezyonlar kategorisi için, bu kategorinin yüksek klinik riskini önceliklendirerek güven puanından bağımsız olarak her zaman acil doktor uyarısı sunulmaktadır.

Hiçbir hasta görüntüsü yerel ağın ötesinde depolanmamakta veya iletilmemektedir. Arka uç görüntüleri bellekte işlemekte ve çıkarımın ardından geçici dosyaları derhal temizlemektedir.

### C. Gelecekteki Çalışmalar

1. **HTTPS/TLS Şifreleme:** Mevcut konuşlandırma geliştirme için uygun düz metin HTTP kullanmaktadır. Üretim konuşlandırması, bir ters proxy (örn. Nginx) aracılığıyla TLS sonlandırması gerektirmektedir.

2. **GPU Bulut Konuşlandırması:** Çıkarım sunucusunu GPU donanımlı bir bulut örneğine (AWS, GCP, Azure) taşımak gecikmeyi <100ms'ye indirerek çok kullanıcılı erişimi mümkün kılacaktır.

3. **Genişletilmiş Hastalık Kapsamı:** Ek hastalık sınıflarının (sedef, seboreik dermatit, kontakt dermatit) ve daha büyük, demografik açıdan daha çeşitli veri kümelerinin dahil edilmesi klinik faydayı artıracaktır.

4. **Federatif Öğrenme:** Federatif eğitim mimarisi, hasta gizliliğini korurken gerçek dünya kullanım verilerinden model iyileştirmesini mümkün kılabilir.

5. **Açıklanabilirlik (XAI):** Grad-CAM [14] görselleştirmesinin entegrasyonu, sistemin sınıflandırma kararını etkileyen lezyon bölgelerini vurgulamasına olanak tanıyarak klinisyen güvenini artıracak ve hata analizini kolaylaştıracaktır.

---

## IX. SONUÇ

Bu çalışmada, ince ayarlı bir EfficientNet-B0 sınıflandırıcısını, bir FastAPI REST API sunucusunu ve bir Android mobil uygulamasını bütünleştiren eksiksiz üç katmanlı bir mobil dermatolojik tarama sistemi olan DermAI sunulmuştur. Sınıf başına görüntü sınırlandırma, mektup kutusu ön işleme, agresif veri artırma ve OneCycleLR zamanlamasını bir araya getiren "Dengeli Uzman" eğitim stratejisi, altı klinik açıdan anlamlı hastalık kategorisinde %84,03 genel doğruluk ve 0,81 makro F1 skoru elde etmiştir.

Sistem mimarisi, sorumlulukların ayrılmasını, güvenli bozunmayı ve Android 13+ uyumluluğunu ön plana alarak tam işlevli yapay zeka destekli bir tarama aracının yerel ağ ortamında hazır donanım üzerinde geliştirilebileceğini ve konuşlandırılabileceğini ortaya koymaktadır. Klinik danışmanlık sistemi, klinik yönelimli bir kullanıcı deneyimi sunmak amacıyla ikili sınıflandırmanın ötesine geçen eyleme dönüştürülebilir, aciliyet düzeyine göre sınıflandırılmış öneriler sunmaktadır.

DermAI tanı amaçlı kullanıma yönelik olmamakla birlikte, dermatolojik tarama için entegre mobil yapay zeka sistemlerinin fizibiliteliğini kanıtlamakta ve gelecekteki klinik düzeyde geliştirme için modüler bir temel sunmaktadır.

---

## KAYNAKLAR

[1] G. Hay, "Global burden of skin disease," in *Fitzpatrick's Dermatology*, 9. baskı, McGraw-Hill, 2019.

[2] Dünya Sağlık Örgütü, "Task sharing to address health workforce shortages and improve health outcomes," WHO, Cenevre, 2020.

[3] H. W. Rogers, M. A. Weinstock, S. R. Feldman ve B. M. Coldiron, "Incidence estimate of nonmelanoma skin cancer in the United States, 2012," *JAMA Dermatology*, cilt 151, sayı 10, ss. 1081–1086, Eki. 2015.

[4] A. Esteva ve ark., "Dermatologist-level classification of skin cancer with deep neural networks," *Nature*, cilt 542, sayı 7639, ss. 115–118, Şub. 2017.

[5] N. C. F. Codella ve ark., "Skin lesion analysis toward melanoma detection: ISIC 2018 challenge," *arXiv preprint arXiv:1902.03368*, 2019.

[6] M. Tan ve Q. Le, "EfficientNet: Rethinking model scaling for convolutional neural networks," *ICML Bildiriler Kitabı*, 2019, ss. 6105–6114.

[7] E. Winkler ve ark., "Association between surgical skin markings in dermoscopic images and diagnostic performance of a deep learning convolutional neural network for melanoma recognition," *JAMA Dermatology*, cilt 155, sayı 10, ss. 1135–1141, Eki. 2019.

[8] V. Gulshan ve ark., "Development and validation of a deep learning algorithm for detection of diabetic retinopathy in retinal fundus photographs," *JAMA*, cilt 316, sayı 22, ss. 2402–2410, Ara. 2016.

[9] P. Lakhani ve B. Sundaram, "Deep learning at chest radiography: Automated classification of pulmonary tuberculosis by using convolutional neural networks," *Radiology*, cilt 284, sayı 2, ss. 574–582, Ağu. 2017.

[10] S. Ramírez, *FastAPI Dokümantasyonu*, 2024. [Çevrimiçi]. Erişim: https://fastapi.tiangolo.com

[11] N. V. Chawla, K. W. Bowyer, L. O. Hall ve W. P. Kegelmeyer, "SMOTE: Synthetic minority over-sampling technique," *Journal of Artificial Intelligence Research*, cilt 16, ss. 321–357, 2002.

[12] L. N. Smith, "Super-convergence: Very fast training of neural networks using large learning rates," *SPIE Defense + Commercial Sensing Bildiriler Kitabı*, 2019.

[13] A. Adamson ve A. Smith, "Machine learning and health care disparities in dermatology," *JAMA Dermatology*, cilt 154, sayı 11, ss. 1247–1248, Kas. 2018.

[14] R. R. Selvaraju ve ark., "Grad-CAM: Visual explanations from deep networks via gradient-based localization," *ICCV Bildiriler Kitabı*, 2017, ss. 618–626.

[15] J. L. Zaenglein ve ark., "Guidelines of care for the management of acne vulgaris," *Journal of the American Academy of Dermatology*, cilt 74, sayı 5, ss. 945–973, May. 2016.

[16] L. F. Eichenfield ve ark., "Guidelines of care for the management of atopic dermatitis," *Journal of the American Academy of Dermatology*, cilt 70, sayı 2, ss. 338–351, Şub. 2014.

[17] S. Weidinger ve N. Novak, "Atopic dermatitis," *The Lancet*, cilt 387, sayı 10023, ss. 1109–1122, Mar. 2016.

[18] A. Taieb ve M. Picardo, "Vitiligo," *The New England Journal of Medicine*, cilt 360, sayı 2, ss. 160–169, Oca. 2009.

---

*Makale gönderim tarihi: Nisan 2026.*
*Bu çalışma bir yazılım mühendisliği dönem projesi kapsamında tamamlanmıştır.*
*Tanımlanan sistem yalnızca akademik ve eğitim amaçlıdır.*
*DermAI sertifikalı bir tıbbi cihaz niteliği taşımamaktadır.*
