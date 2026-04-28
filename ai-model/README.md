# DermAI Exported Checkpoint

This directory contains the final trained DermAI deep learning model (`dermai.pth`). It uses a modified **EfficientNet-B0** architecture trained to classify 6 expert dermatological classes.

### Detectable Classes
1. Eczema (Atopic Dermatitis)
2. Normal (Healthy Skin)
3. Acne and Rosacea
4. Actinic Keratosis, Basal Cell Carcinoma, and other Malignant Lesions
5. Light Diseases and Disorders of Pigmentation
6. Warts, Molluscum, and other Viral Infections

## Included Files & Their Usage

This standalone bundle includes the necessary scripts to rebuild the architecture and preprocess images exactly as they were during training.

* **`dermai.pth`**: The trained PyTorch weights. You load this file into the model during initialization.
* **`predict.py`**: The main entry point for CLI inference. You run this script directly to test images from the command line.
* **`inference.py`**: Contains the core `predict_image()` function. It handles tensor operations, Softmax probability calculations, and formatting the Top-3 JSON response. Import this when building a backend API.
* **`transforms.py`**: Contains `get_val_transforms()`. It automatically applies the mathematical "Smart Padding" (letterboxing) and resizing. **Always** pass raw user images through this before sending them to the model.
* **`model.py`**: Defines the `build_dermai_model()` function that reconstructs the EfficientNet-B0 architecture and our custom classifier head.
* **`config.py`**: The Single Source of Truth. Contains the `EXPERT_CLASSES` list (which translates the model's numerical outputs into readable disease names) and target `IMAGE_SIZE` (224).
* **`requirements.txt`**: The list of pip dependencies required to safely run the model.

## Prerequisites

You need Python 3.8+ to run this inference module. Ensure you install the required dependencies:

```bash
pip install -r requirements.txt
```

## Production Integration (How to Run)

This exported folder is a **fully standalone production bundle**. You do not need the original training environment to run inferences. 

Additionally, **no manual image preprocessing (such as padding or cropping) is required**. The included `transforms.py` automatically applies "Smart Padding" (letterboxing) to preserve the aspect ratio of user-uploaded images before resizing them to the required 224x224 resolution.

### 1. Command Line Interface (CLI)
Run inference directly from your command line:

```bash
python predict.py --image "path/to/test_image.jpg" --checkpoint "dermai.pth"
```

The script will output a JSON contract containing:
- The top 3 predicted diseases.
- Probability confidence scores.
- Clinical recommendations.

### 2. Backend Server Integration (FastAPI / Flask)
To integrate this into a production API server:
1. Import the prediction setup into your router: `from predict import build_dermai_model, get_val_transforms` and `from inference import predict_image`.
2. Initialize the model and transforms **once** during server startup to save memory.
3. Pass any raw user-uploaded image files through the inference function to return JSON responses to your web or mobile applications.
