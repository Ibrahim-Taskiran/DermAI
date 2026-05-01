import argparse
import json
import torch
import os
from model import build_dermai_model
from transforms import get_val_transforms
from inference import predict_image
from config import EXPERT_CLASSES, IMAGE_SIZE

def generate_recommendation(top_prediction: dict) -> str:
    """
    Generates a user-friendly recommendation based on the top prediction.
    """
    disease = top_prediction.get("disease_id", "")
    prob = top_prediction.get("probability", 0.0)
    
    if "Malignant" in disease and prob >= 0.5:
        return "⚠️ HIGH PRIORITY: The model detected a potentially malignant lesion. Please consult a dermatologist immediately."
    elif prob >= 0.8:
        return f"High confidence ({prob*100:.1f}%) for '{disease}'. Consider consulting a healthcare professional for guidance."
    elif prob >= 0.5:
        return f"Moderate confidence ({prob*100:.1f}%) for '{disease}'. A dermatologist can provide a more conclusive diagnosis."
    else:
        return "Low confidence prediction. Please do not rely on this result and consult a medical professional."

def main():
    parser = argparse.ArgumentParser(description="DermAI Inference Script")
    parser.add_argument("-i", "--image", required=True, help="Path to the input image file to predict")
    parser.add_argument("-c", "--checkpoint", default="checkpoints/dermai_checkpoint_epoch_15.pth", help="Path to the saved model checkpoint")
    args = parser.parse_args()

    if not os.path.isfile(args.image):
        print(f"Error: Input image file '{args.image}' not found.")
        return

    if not os.path.isfile(args.checkpoint):
        print(f"Error: Checkpoint file '{args.checkpoint}' not found.")
        return

    device = "cuda" if torch.cuda.is_available() else "cpu"
    print(f"Using device: {device.upper()}")

    # 1. Initialize EfficientNet-B0 Model configured for 8 classes
    num_classes = len(EXPERT_CLASSES)
    model = build_dermai_model(num_classes=num_classes, pretrained=False)
    
    # Load the checkpoint weights correctly handling disaster-recovery dict mapping
    checkpoint = torch.load(args.checkpoint, map_location=device, weights_only=True)
    model.load_state_dict(checkpoint['model_state_dict'])
    model = model.to(device)
    model.eval()

    # 2. Get Validation Transforms (includes Smart Pad and Resize from v2)
    val_transforms = get_val_transforms(target_size=IMAGE_SIZE)

    # 3. Predict Top-3 Results
    results = predict_image(
        image_path=args.image,
        model=model,
        val_transforms=val_transforms,
        class_names=EXPERT_CLASSES,
        device=device
    )

    # 4. Output JSON
    print("\n" + "="*50)
    print("DermAI Top-3 Predictions JSON")
    print("="*50)
    print(json.dumps(results, indent=4))
    
    # 5. Output Recommendation
    print("\n" + "="*50)
    print("Clinical Recommendation")
    print("="*50)
    
    top_pred = results["predictions"][0]
    recommendation = generate_recommendation(top_pred)
    print(recommendation)
    print("="*50 + "\n")

if __name__ == "__main__":
    main()
