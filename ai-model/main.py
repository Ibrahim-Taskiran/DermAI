import torch
import json
from PIL import Image

# Import from all 5 custom DermAI modules
from transforms import get_train_transforms, get_val_transforms
from dataset import DermAIDataset
from model import build_dermai_model, print_model_summary
from engine import train_model, train_step, val_step, save_checkpoint
from inference import predict_image

def main() -> None:
    """
    Dry run script to test the integration of all DermAI PyTorch modules 
    prior to starting the full training process.
    """
    print("=== DermAI Phase 1: Integration Test ===")
    
    # 1. Setup Phase 1 variables
    num_classes = 23
    device = "cuda" if torch.cuda.is_available() else "cpu"
    print(f"Target Device: {device.upper()}")
    
    # 2. VRAM Check & Model Initialization
    print("\n--- Initializing ResNet-18 Backbone ---")
    # For a purely structural dry run, we leverage pretrained=False optionally to save bandwidth, 
    # but we'll use the default settings (pretrained=True) as implemented in model.py
    model = build_dermai_model(num_classes=num_classes, pretrained=True)
    model = model.to(device)
    
    # Display the summary to visualize the memory footprint in the terminal
    print_model_summary(model, input_size=(1, 3, 224, 224))
    
    # 3. Pipeline Test (End-to-End Inference Mock)
    print("\n--- Running Inference Pipeline Test ---")
    
    # Create a dummy image (500x500 red square) and save locally
    dummy_image_path = "test_lesion.jpg"
    dummy_image = Image.new('RGB', (500, 500), color='red')
    dummy_image.save(dummy_image_path)
    print(f"Generated dummy image at {dummy_image_path}")
    
    # Define placeholder class names for the 23 diseases
    class_names = [f"Disease_{i}" for i in range(num_classes)]
    
    # Initialize the validation transforms strictly configured for our architecture
    val_transforms = get_val_transforms(target_size=224)
    
    # Execute the prediction constraint
    results = predict_image(
        image_path=dummy_image_path,
        model=model,
        val_transforms=val_transforms,
        class_names=class_names,
        device=device
    )
    
    # 4. Output validation
    print("\n--- Final UI JSON Contract Output ---")
    print(json.dumps(results, indent=4))
    print("=======================================")

if __name__ == "__main__":
    main()
