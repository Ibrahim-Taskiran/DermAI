# config.py

# Single Source of Truth for DermAI pipeline configuration

EXPERTS_CLASSES = [
    'Eczema (Atopic Dermatitis)', 
    'Normal', 
    'Acne and Rosacea Photos', 
    'Actinic Keratosis Basal Cell Carcinoma and other Malignant Lesions', 
    'Light Diseases and Disorders of Pigmentation', 
    'Warts Molluscum and other Viral Infections'
]

# We maintain the variable name EXPERT_CLASSES for backwards compatibility
EXPERT_CLASSES = EXPERTS_CLASSES

NUM_CLASSES = len(EXPERTS_CLASSES)

# Eğitim etiket indeksi = alfabetik sıra (dataset.py → sorted). Model çıktısı bu sırayla eşlenir.
CLASS_LABEL_ORDER = sorted(EXPERTS_CLASSES)


def class_names_for_num_classes(num_classes: int) -> list[str]:
    """
    Eğitimdeki etiket sırası: DermAIDataset.classes = sorted(valid_classes).
    Checkpoint'te class_names yoksa bu sıra kullanılmalıdır.
    """
    expected = CLASS_LABEL_ORDER
    if num_classes != len(expected):
        raise ValueError(
            f"Checkpoint {num_classes} sınıflı; beklenen {len(expected)}. "
            "ai-model/train.py ile 6 sınıflı model eğitin ve best_model.pth olarak kaydedin."
        )
    return expected


CLASS_MAPPING = {
    'Eczema Photos': 'Eczema (Atopic Dermatitis)',
    'Atopic Dermatitis Photos': 'Eczema (Atopic Dermatitis)'
}

IMAGE_SIZE = 224
BATCH_SIZE = 32
