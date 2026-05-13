# config.py

EXPERT_CLASSES = [
    'Acne and Rosacea',
    'Eczema',
    'Malignant Lesions',
    'Normal',
    'Other',
    'Pigmentation Disorders',
    'Viral Infections'
]

# 2. Dynamic Capping Settings
MAX_IMAGES_PER_CLASS = 500

# 3. Model Hyperparameters
IMAGE_SIZE = 224
BATCH_SIZE = 32

# 4. Class Mapping (Generated dynamically for the Predictor & App)
CLASS_MAPPING = {i: cls_name for i, cls_name in enumerate(EXPERT_CLASSES)}