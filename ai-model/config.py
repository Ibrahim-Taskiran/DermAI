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

CLASS_MAPPING = {
    'Eczema Photos': 'Eczema (Atopic Dermatitis)',
    'Atopic Dermatitis Photos': 'Eczema (Atopic Dermatitis)'
}

IMAGE_SIZE = 224
BATCH_SIZE = 32
