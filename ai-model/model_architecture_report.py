import torch
from model import build_dermai_model
from config import EXPERT_CLASSES, IMAGE_SIZE

def generate_model_report():
    print("="*60)
    print("🧠 DermAI Model Architecture Report 🧠".center(60))
    print("="*60)
    
    # 1. Basic Info
    num_classes = len(EXPERT_CLASSES)
    
    print(f"\n[1] General Architecture Details")
    print(f"-> Base Framework: EfficientNet-B0")
    print(f"-> Accepted Input Size: {IMAGE_SIZE}x{IMAGE_SIZE} pixels (RGB)")
    print(f"-> Output Classifier Size: {num_classes} Expert Classes")
    
    # 2. Build the Model
    # We load the structure to inspect its layers and parameters
    model = build_dermai_model(num_classes=num_classes, pretrained=False)
    
    # 3. Parameter Counting
    total_params = sum(p.numel() for p in model.parameters())
    trainable_params = sum(p.numel() for p in model.parameters() if p.requires_grad)
    
    print(f"\n[2] Weight & Parameter Information")
    print(f"-> Total Parameters: {total_params:,}")
    print(f"-> Trainable Parameters: {trainable_params:,} (Weights updated during training)")
    print(f"-> Model Size (Estimate): {total_params * 4 / (1024 ** 2):.2f} Megabytes")
    
    # 4. Layer Counting
    layer_counts = {}
    total_modules = 0
    
    # Iterate through all modules/layers in the architecture
    for name, module in model.named_modules():
        # Identify specific types of layers
        if isinstance(module, torch.nn.Conv2d):
            layer_counts['Convolutional (Conv2d)'] = layer_counts.get('Convolutional (Conv2d)', 0) + 1
            total_modules += 1
        elif isinstance(module, torch.nn.Linear):
            layer_counts['Dense / Fully Connected (Linear)'] = layer_counts.get('Dense / Fully Connected (Linear)', 0) + 1
            total_modules += 1
        elif isinstance(module, torch.nn.BatchNorm2d):
            layer_counts['Batch Normalization'] = layer_counts.get('Batch Normalization', 0) + 1
            total_modules += 1
        elif isinstance(module, torch.nn.SiLU) or isinstance(module, torch.nn.ReLU):
            layer_counts['Activation Functions (SiLU/ReLU)'] = layer_counts.get('Activation Functions (SiLU/ReLU)', 0) + 1
            total_modules += 1
        elif isinstance(module, torch.nn.Dropout) or isinstance(module, torch.nn.Dropout2d):
            layer_counts['Dropout (Regularization)'] = layer_counts.get('Dropout (Regularization)', 0) + 1
            total_modules += 1
            
    print(f"\n[3] Deep Structural Analysis")
    print(f"-> Total Operational Layers Counted: {total_modules}")
    for layer_type, count in sorted(layer_counts.items(), key=lambda x: x[1], reverse=True):
        print(f"   - {count} x {layer_type}")
        
    print("\n" + "="*60)
    print("Report generated successfully.".center(60))

if __name__ == "__main__":
    generate_model_report()
