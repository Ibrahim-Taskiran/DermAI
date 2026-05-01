import torch
import torch.nn as nn
from torch.utils.data import DataLoader
from typing import Dict, Any, List, Optional
from tqdm import tqdm
from sklearn.metrics import classification_report

def save_checkpoint(
    model: nn.Module, 
    optimizer: torch.optim.Optimizer, 
    epoch: int, 
    filepath: str
) -> None:
    """
    Saves a dictionary containing the model's state_dict, the optimizer's state_dict, 
    and the current epoch for disaster recovery and resuming training.

    Args:
        model (nn.Module): The PyTorch model to save.
        optimizer (torch.optim.Optimizer): The optimizer state to save.
        epoch (int): The current epoch number.
        filepath (str): Path to save the checkpoint (.pth file).
    """
    checkpoint = {
        'epoch': epoch,
        'model_state_dict': model.state_dict(),
        'optimizer_state_dict': optimizer.state_dict()
    }
    torch.save(checkpoint, filepath)
    print(f"Checkpoint saved securely at {filepath}")


def train_step(
    model: nn.Module,
    dataloader: DataLoader,
    criterion: nn.Module,
    optimizer: torch.optim.Optimizer,
    scaler: torch.amp.GradScaler,
    device: torch.device,
    scheduler: Any = None
) -> float:
    """
    Executes one full training epoch over the dataset.
    Uses Automatic Mixed Precision (AMP) to reduce memory consumption.

    Args:
        model (nn.Module): The model to train.
        dataloader (DataLoader): DataLoader for the training set.
        criterion (nn.Module): The loss function.
        optimizer (torch.optim.Optimizer): The optimizer.
        scaler (torch.amp.GradScaler): GradScaler for AMP.
        device (torch.device): Device to compute on (CPU, CUDA, etc.).
        scheduler (Any): Optional learning rate scheduler.

    Returns:
        float: The average training loss for this epoch.
    """
    model.train()
    running_loss = 0.0

    # Wrap the dataloader in tqdm to display a progress bar
    progress_bar = tqdm(dataloader, desc="Training", leave=False)

    for inputs, labels in progress_bar:
        inputs, labels = inputs.to(device), labels.to(device)

        optimizer.zero_grad()

        # AMP Forward pass
        with torch.amp.autocast('cuda'):
            outputs = model(inputs)
            loss = criterion(outputs, labels)

        # AMP Backward pass and optimization
        scaler.scale(loss).backward()
        scaler.step(optimizer)
        scaler.update()

        if scheduler is not None and isinstance(scheduler, torch.optim.lr_scheduler.OneCycleLR):
            scheduler.step()

        running_loss += loss.item() * inputs.size(0)
        
        # Update progress bar description
        progress_bar.set_postfix({"Loss": f"{loss.item():.4f}"})

    epoch_loss = running_loss / len(dataloader.dataset)
    return epoch_loss


def val_step(
    model: nn.Module,
    dataloader: DataLoader,
    criterion: nn.Module,
    device: torch.device,
    class_names: Optional[List[str]] = None
) -> float:
    """
    Executes validation over the dataset. Computes loss and prints an advanced 
    classification report (Precision, Recall, F1 for each class) which is critical 
    for medical class imbalance.

    Args:
        model (nn.Module): The model to validate.
        dataloader (DataLoader): DataLoader for the validation set.
        criterion (nn.Module): The loss function.
        device (torch.device): Device to compute on.
        class_names (Optional[List[str]]): List of class names for the classification report.

    Returns:
        float: The average validation loss.
    """
    model.eval()
    running_loss = 0.0
    
    all_preds = []
    all_labels = []

    progress_bar = tqdm(dataloader, desc="Validation", leave=False)

    with torch.no_grad():
        for inputs, labels in progress_bar:
            inputs, labels = inputs.to(device), labels.to(device)

            # Use autocast for validation as well to simulate inference VRAM savings
            with torch.amp.autocast('cuda'):
                outputs = model(inputs)
                loss = criterion(outputs, labels)

            running_loss += loss.item() * inputs.size(0)
            
            _, preds = torch.max(outputs, 1)
            
            all_preds.extend(preds.cpu().numpy())
            all_labels.extend(labels.cpu().numpy())
            
            progress_bar.set_postfix({"Loss": f"{loss.item():.4f}"})

    epoch_loss = running_loss / len(dataloader.dataset)
    
    # Compute advanced diagnostic metrics
    report = classification_report(
        all_labels, 
        all_preds, 
        target_names=class_names, 
        zero_division=0
    )
    print("\n--- Validation Classification Report ---")
    print(report)
    print("-" * 40)
    
    return epoch_loss


def train_model(
    model: nn.Module,
    train_loader: DataLoader,
    val_loader: DataLoader,
    criterion: nn.Module,
    optimizer: torch.optim.Optimizer,
    scheduler: Any,
    num_epochs: int,
    device: torch.device,
    checkpoint_dir: str = ".",
    class_names: Optional[List[str]] = None
) -> Dict[str, list]:
    """
    Orchestrates the entire training loop: runs training steps, validation steps,
    metric calculations, scheduling, and disaster recovery checkpointing per epoch.

    Args:
        model (nn.Module): The model to train.
        train_loader (DataLoader): DataLoader for the training set.
        val_loader (DataLoader): DataLoader for the validation set.
        criterion (nn.Module): The CrossEntropyLoss function.
        optimizer (torch.optim.Optimizer): The optimizer (e.g., AdamW).
        scheduler (Any): Learning rate scheduler.
        num_epochs (int): Total number of epochs to train for.
        device (torch.device): Device to compute on (e.g., 'cuda' or 'cpu').
        checkpoint_dir (str): Directory to save disaster recovery checkpoints.
        class_names (Optional[List[str]]): List of class strings for precision/recall report.

    Returns:
        Dict[str, list]: A dictionary containing the history of train and validation losses.
    """
    model = model.to(device)
    scaler = torch.amp.GradScaler('cuda')
    
    history = {
        "train_loss": [],
        "val_loss": [],
    }

    print(f"Beginning training for {num_epochs} epochs on {device}...")
    
    for epoch in range(1, num_epochs + 1):
        print(f"\nEpoch {epoch}/{num_epochs}")
        
        train_loss = train_step(
            model=model, 
            dataloader=train_loader, 
            criterion=criterion, 
            optimizer=optimizer, 
            scaler=scaler, 
            device=device,
            scheduler=scheduler
        )
        
        val_loss = val_step(
            model=model, 
            dataloader=val_loader, 
            criterion=criterion, 
            device=device,
            class_names=class_names
        )
        
        history["train_loss"].append(train_loss)
        history["val_loss"].append(val_loss)
        
        print(f"Train Loss: {train_loss:.4f} | Val Loss: {val_loss:.4f}")
        
        # Scheduler steps
        if isinstance(scheduler, torch.optim.lr_scheduler.ReduceLROnPlateau):
            scheduler.step(val_loss)
        elif scheduler is not None and not isinstance(scheduler, torch.optim.lr_scheduler.OneCycleLR):
            scheduler.step()
        
        # Disaster Recovery: Save checkpoint every epoch
        checkpoint_path = f"{checkpoint_dir}/dermai_checkpoint_epoch_{epoch}.pth"
        save_checkpoint(model, optimizer, epoch, checkpoint_path)
        
    print("\nTraining Complete!")
    return history
