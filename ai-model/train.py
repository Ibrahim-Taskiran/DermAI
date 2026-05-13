import os
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader, random_split, Subset

# Import custom DermAI modules
from transforms import get_train_transforms, get_val_transforms
from dataset import DermAIDataset
from model import build_dermai_model
from engine import train_model
from config import EXPERT_CLASSES, CLASS_MAPPING, IMAGE_SIZE, BATCH_SIZE

# Hyperparameters
EPOCHS = 20
LEARNING_RATE = 5e-4
DATASET_ROOT = "./Dataset/Master_Dataset" 

def main() -> None:
    """
    Main training script for the DermAI model.
    Initializes datasets properly from the master directory and applies an 80/20 in-memory split.
    """
    print("=== DermAI Model Training ===")
    
    # Setup device
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"Using device: {device.type.upper()}")

    # 1. Initialize Transforms
    target_size = IMAGE_SIZE
    train_transforms = get_train_transforms(target_size=target_size)
    val_transforms = get_val_transforms(target_size=target_size)

    # 2. Setup Datasets
    print("Loading dataset from Master_Dataset directory and applying 80/20 split...")
    
    train_dataset_full = DermAIDataset(
        root_dir=DATASET_ROOT, 
        transform=train_transforms, 
        allowed_classes=EXPERT_CLASSES, 
        class_mapping=CLASS_MAPPING
    )
    
    val_dataset_full = DermAIDataset(
        root_dir=DATASET_ROOT, 
        transform=val_transforms, 
        allowed_classes=EXPERT_CLASSES,
        class_mapping=CLASS_MAPPING
    )
    
    # Calculate sizes for 80% Train / 20% Val
    total_images = len(train_dataset_full)
    train_size = int(0.8 * total_images)
    val_size = total_images - train_size
    
    # CRITICAL: Create a fixed random generator. 
    generator = torch.Generator().manual_seed(42)
    
    # Generate the random indices
    train_indices, val_indices = random_split(range(total_images), [train_size, val_size], generator=generator)
    
    # Apply the indices to create the final subsets
    train_dataset = Subset(train_dataset_full, train_indices.indices)
    val_dataset = Subset(val_dataset_full, val_indices.indices)
    
    print(f"Split into: {len(train_dataset)} training and {len(val_dataset)} validation images.")

    # Extract class names from the underlying dataset object
    class_names = train_dataset_full.classes

    # 3. Setup DataLoaders
    # Pin memory for faster GPU transfer and set num_workers=4
    train_loader = DataLoader(
        train_dataset,
        batch_size=BATCH_SIZE,
        shuffle=True, # Shuffle training data
        num_workers=4,
        pin_memory=True
    )
    
    val_loader = DataLoader(
        val_dataset,
        batch_size=BATCH_SIZE,
        shuffle=False, # No need to shuffle validation data
        num_workers=4,
        pin_memory=True
    )

    # 4. Model Initialization
    print("\nInitializing EfficientNet-B0 Backbone...")
    # Dynamically determine the number of classes from config using EXPERT_CLASSES directly as requested
    dynamic_num_classes = len(EXPERT_CLASSES)
    model = build_dermai_model(num_classes=dynamic_num_classes, pretrained=True)
    model = model.to(device)

    # 5. Optimizer, Loss & Scheduler Configuration
    criterion = nn.CrossEntropyLoss()
    optimizer = optim.AdamW(model.parameters(), lr=LEARNING_RATE)
    
    # 1CycleLR strategy for faster and more accurate convergence
    scheduler = optim.lr_scheduler.OneCycleLR(
        optimizer,
        max_lr=1e-3,
        steps_per_epoch=len(train_loader),
        epochs=EPOCHS
    )

    # 6. Execution
    print("\nStarting the Training Engine...")
    os.makedirs("checkpoints", exist_ok=True)
    
    history = train_model(
        model=model,
        train_loader=train_loader,
        val_loader=val_loader,
        criterion=criterion,
        optimizer=optimizer,
        scheduler=scheduler,
        num_epochs=EPOCHS,
        device=device,
        checkpoint_dir="./checkpoints",
        class_names=class_names
    )

    from visualize import plot_loss_curves
    plot_loss_curves(history)
    print("Training complete! Loss graph saved to reports/training_loss_curve.png")

if __name__ == "__main__":
    main()