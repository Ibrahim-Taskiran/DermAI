import os
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader

# Import custom DermAI modules
from transforms import get_train_transforms, get_val_transforms
from dataset import DermAIDataset
from model import build_dermai_model
from engine import train_model
from config import EXPERT_CLASSES, CLASS_MAPPING, IMAGE_SIZE, BATCH_SIZE

# Hyperparameters
EPOCHS = 20
LEARNING_RATE = 5e-4
DATASET_ROOT = "./Dataset"

def main() -> None:
    """
    Main training script for the DermAI model.
    Initializes datasets, model, optimizer, scheduler, and starts the training loop.
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
    train_dir = os.path.join(DATASET_ROOT, "train")
    val_dir = os.path.join(DATASET_ROOT, "test")
    
    print("Loading datasets...")
    train_dataset = DermAIDataset(
        root_dir=train_dir, 
        transform=train_transforms, 
        allowed_classes=EXPERT_CLASSES, 
        class_mapping=CLASS_MAPPING
    )
    val_dataset = DermAIDataset(
        root_dir=val_dir, 
        transform=val_transforms, 
        allowed_classes=EXPERT_CLASSES,
        class_mapping=CLASS_MAPPING
    )
    
    print(f"Loaded {len(train_dataset)} training images and {len(val_dataset)} validation images.")
    print(f"Identified {len(train_dataset.classes)} unique classes.")

    # Extract class names to pass to the engine for classification report
    class_names = train_dataset.classes

    # 3. Setup DataLoaders
    # Pin memory for faster GPU transfer and set num_workers=4 for AMD Ryzen CPU
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
    # Dynamically determine the number of classes
    dynamic_num_classes = len(train_dataset.classes)
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

if __name__ == "__main__":
    main()
