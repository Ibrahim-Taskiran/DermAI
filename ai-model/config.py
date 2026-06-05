# config.py

EXPERT_CLASSES = [
    'Acne or Rosacea',
    'Atopic Dermatitis',
    'Basal Cell Carcinoma',
    'Benign Keratosis-like Lesions',
    'Eczema',
    'Melanocytic Nevi',
    'Melanoma',
    'Normal',
    'Psoriasis pictures Lichen Planus and related diseases',
    'Seborrheic Keratoses and other Benign Tumors',
    'Tinea Ringworm Candidiasis and other Fungal Infections',
    'Warts Molluscum and other Viral Infections'
]

# 2. Dynamic Capping Settings
MAX_TRAIN_IMAGES_PER_CLASS = 2000
MAX_VAL_IMAGES_PER_CLASS = 500

# 3. Model Hyperparameters
IMAGE_SIZE = 224
BATCH_SIZE = 32

# 4. Class Mapping (Generated dynamically for the Predictor & App)
CLASS_MAPPING = {i: cls_name for i, cls_name in enumerate(EXPERT_CLASSES)}