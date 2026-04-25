# =============================================
# DermAI Backend - Bakım Önerileri Servisi
# =============================================
# AI modelinin tahmin ettiği hastalık sınıfına
# göre kullanıcıya özel bakım tavsiyeleri,
# tedavi önerileri ve doktor uyarıları üretir.
#
# Sınıf listesi ai-model/config.py'deki
# EXPERTS_CLASSES ile senkronize edilmelidir.
# =============================================

from schemas.response import Advice


# =============================================
# Her hastalık sınıfı için önceden tanımlanmış
# bakım önerisi veritabanı.
# Anahtar: ai-model/config.py'deki sınıf adı
# =============================================
_ADVICE_DATABASE: dict[str, dict] = {

    "Eczema (Atopic Dermatitis)": {
        "care": (
            "Cildinizi günde en az iki kez parfümsüz, yoğun nemlendirici ile nemlendirin. "
            "Uzun süreli sıcak duş almaktan kaçının; ılık su tercih edin ve duş sonrası "
            "cildi nazikçe kurulayıp hemen nemlendirici uygulayın. "
            "Pamuklu, nefes alan giysiler tercih edin; yün ve sentetik kumaşlardan uzak durun. "
            "Bilinen tetikleyicileri (sabun, deterjan, toz akarı, hayvan tüyü) tespit edip önleyin."
        ),
        "recommendation": (
            "Parfümsüz, hipoalerjenik nemlendirici kremler (seramid içerenler tercih edilir) kullanın. "
            "Akut dönemlerde doktor önerisiyle topikal kortikosteroid krem uygulanabilir. "
            "Antihistaminikler kaşıntıyı hafifletebilir. "
            "Ev ortamında hava nemlendirici kullanmak semptomları azaltabilir."
        ),
        "doctor_warning": (
            "Kaşıntı uyku düzeninizi bozuyorsa, ciltte sarı kabuklanma veya irin gibi "
            "enfeksiyon belirtileri görülüyorsa ya da mevcut tedaviye yanıt vermiyorsa "
            "bir dermatoloğa başvurmanız gerekmektedir."
        ),
    },

    "Normal": {
        "care": (
            "Cildiniz sağlıklı görünüyor. Düzenli cilt bakım rutininizi sürdürün: "
            "sabah ve akşam hafif bir temizleyici kullanın, "
            "gündüz SPF 30+ güneş koruyucu sürün ve geceleri nemlendirici uygulayın. "
            "Bol su içmek ve dengeli beslenmek cilt sağlığını destekler."
        ),
        "recommendation": (
            "Cilt tipinize uygun (yağlı, kuru, karma) ürünler tercih edin. "
            "Antioksidan içeren ürünler (C vitamini serumu gibi) yaşlanma belirtilerini geciktirebilir. "
            "Sigara ve aşırı alkol tüketiminden kaçının; bunlar cilt sağlığını olumsuz etkiler."
        ),
        "doctor_warning": (
            "Cildinizde yeni bir ben, leke veya asimetrik büyüme fark ederseniz, "
            "ya da renk, şekil veya boyut değişikliği görürseniz vakit kaybetmeden "
            "bir dermatolog veya aile hekiminize başvurun. "
            "Yılda bir düzenli cilt kontrolü tavsiye edilir."
        ),
    },

    "Acne and Rosacea Photos": {
        "care": (
            "Yüzünüzü sabah ve akşam ılık suyla nazik bir temizleyiciyle yıkayın. "
            "Sivilceleri elinizle sıkmayın; bakteri yayılımını ve iz kalmasını önler. "
            "Yağsız (non-comedogenic) nemlendirici ve güneş koruyucu kullanın. "
            "Yüksek glisemik indeksli gıdalar ve süt ürünleri bazı kişilerde akneyi kötüleştirebilir; "
            "diyet takibi faydalı olabilir."
        ),
        "recommendation": (
            "Salisilik asit (%0,5-2) veya benzoil peroksit içeren ürünler, aknede etkili olabilir. "
            "Rozasea için kırmızı et, baharatlı yiyecekler, alkol ve sıcak içecekler gibi "
            "tetikleyicilerden kaçının. "
            "Retinoller gözenek tıkanıklığını azaltabilir; düşük konsantrasyondan başlayın. "
            "Kimyasal peeling ve profesyonel cilt bakımı uzun vadede fayda sağlayabilir."
        ),
        "doctor_warning": (
            "Sivilceler derin nodül veya kistlere dönüşüyorsa, "
            "yüzde kalıcı kızarıklık, damar genişlemesi veya görme problemi varsa "
            "(rozaseada göz tutulumu mümkündür), "
            "ev tedavilerine yanıt vermiyorsa bir dermatolog tarafından değerlendirilmeniz önerilir."
        ),
    },

    "Actinic Keratosis Basal Cell Carcinoma and other Malignant Lesions": {
        "care": (
            "ETKİLENEN BÖLGEYİ KORUYUN: Güneş ışığına maruz kalmayı en aza indirin. "
            "Yüksek SPF (50+) geniş spektrumlu güneş koruyucu her gün uygulayın. "
            "Şüpheli lezyona tahriş edici kozmetik, asit veya ev yapımı ürün uygulamayın. "
            "Lezyonu kaşımayın veya kurcalamayın."
        ),
        "recommendation": (
            "Güneşe çıkarken koruyucu giysi, geniş kenarlı şapka ve UV korumalı güneş gözlüğü kullanın. "
            "Solaryumdan kesinlikle kaçının. "
            "Vücudunuzdaki tüm benleri ve lekeleri düzenli aralıklarla ABCDE kuralıyla "
            "(Asimetri, Sınır, Renk, Çap, Evrim) takip edin."
        ),
        "doctor_warning": (
            "⚠️ ACİL UYARI: Bu tahmin, tedavi gerektiren kötü huylu veya pre-malign bir "
            "cilt lezyonuna işaret edebilir. "
            "Lütfen en kısa sürede bir dermatoloji uzmanına veya onkoloji kliniğine başvurun. "
            "Erken teşhis tedaviyi büyük ölçüde kolaylaştırır. "
            "Bu sonucu kendi kendinize tedavi etmeye çalışmayın."
        ),
    },

    "Light Diseases and Disorders of Pigmentation": {
        "care": (
            "Etkilenen bölgeleri güneş ışığından mutlaka koruyun; "
            "güneş pigmentasyon bozukluklarını belirgin biçimde kötüleştirebilir. "
            "Her gün, bulutlu havada bile SPF 50+ güneş koruyucu kullanın. "
            "Cildi nazik temizleyicilerle temizleyin; ovma ve tahriş etmekten kaçının."
        ),
        "recommendation": (
            "Niasinamid, azelaik asit veya alfa-arbutin içeren aydınlatıcı serumlar "
            "renk eşitsizliğini azaltabilir. "
            "D vitamini eksikliği bazı pigmentasyon sorunlarıyla ilişkilidir; "
            "kan değerlerinizi kontrol ettirin. "
            "Vitiligo veya melazma gibi durumlar için doktor eşliğinde "
            "topikal veya lazer tedavileri değerlendirilebilir."
        ),
        "doctor_warning": (
            "Beyaz veya açık renkli lekeler hızla yayılıyorsa, "
            "renk değişikliğine kaşıntı, ağrı veya kabuklanma eşlik ediyorsa "
            "ya da lekelerin sınırları düzensiz ve asimetrik görünüyorsa "
            "bir dermatoloğa başvurmanız önerilir. "
            "Bazı pigmentasyon bozuklukları sistemik hastalıkların belirtisi olabilir."
        ),
    },

    "Warts Molluscum and other Viral Infections": {
        "care": (
            "Siğil veya lezyonlara mümkün olduğunca dokunmayın; "
            "dokunduktan sonra ellerinizi sabun ve suyla yıkayın. "
            "Havlu, tıraş makinesi, havuz terliği gibi kişisel eşyaları paylaşmayın. "
            "Lezyonun üzerini bandaj veya örtü ile kapatarak başka bölgelere "
            "veya diğer kişilere bulaşmasını önleyin."
        ),
        "recommendation": (
            "Siğiller için eczaneden temin edilebilen salisilik asit içeren "
            "bant veya solüsyonlar ev tedavisinde kullanılabilir. "
            "Bağışıklık sistemini güçlendirmek (düzenli uyku, dengeli beslenme, "
            "C vitamini) viral enfeksiyonlarla mücadeleye yardımcı olur. "
            "Yüzme havuzları, duş ve locker room gibi ortak alanlarda ayakkabı giyin."
        ),
        "doctor_warning": (
            "Lezyon sayısı hızla artıyorsa, ağrı, kanama veya çevresinde kızarıklık varsa, "
            "yüz, kasık veya göz çevresinde görülüyorsa "
            "ya da bağışıklık sistemi baskılanmış bir bireyseniz "
            "bir dermatoloğa başvurmanız önerilir. "
            "Molluscum contagiosum çocuklarda sık görülür; "
            "yaygın vakalarda tıbbi tedavi gerekebilir."
        ),
    },
}

# Tanınmayan sınıf adı geldiğinde kullanılacak varsayılan öneri
_DEFAULT_ADVICE: dict = {
    "care": (
        "Cildinizi temiz ve nemli tutun. "
        "Tahriş edici kozmetik ürünlerden kaçının ve deri tipinize uygun bakım yapın."
    ),
    "recommendation": (
        "Güneş koruyucu kullanmayı ihmal etmeyin. "
        "Düzenli cilt kontrolü için yılda bir dermatolog ziyareti önerilir."
    ),
    "doctor_warning": (
        "Cildinizde herhangi bir değişiklik, yeni leke veya rahatsızlık fark ederseniz "
        "vakit kaybetmeden bir dermatoloğa başvurun."
    ),
}


def get_advice_for_disease(disease_name: str) -> Advice:
    """
    Verilen hastalık adına karşılık gelen Advice nesnesini döndürür.

    Parametre:
        disease_name (str): ai-model/config.py'deki EXPERTS_CLASSES içindeki sınıf adı.

    Döndürür:
        Advice: Bakım, öneri ve doktor uyarısı bilgilerini içeren nesne.

    Not:
        Bilinmeyen bir hastalık adı gelirse varsayılan genel öneri kullanılır.
    """
    # Veritabanında bu hastalık için kayıt var mı?
    advice_data = _ADVICE_DATABASE.get(disease_name, _DEFAULT_ADVICE)

    return Advice(
        care=advice_data["care"],
        recommendation=advice_data["recommendation"],
        doctor_warning=advice_data["doctor_warning"],
    )
