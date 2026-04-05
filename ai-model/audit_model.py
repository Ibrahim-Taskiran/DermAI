import os
import torch
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from torch.utils.data import DataLoader
from sklearn.metrics import confusion_matrix, f1_score, accuracy_score, classification_report
from tqdm import tqdm

from dataset import DermAIDataset
from transforms import get_val_transforms
from model import build_dermai_model
from config import EXPERT_CLASSES, CLASS_MAPPING, IMAGE_SIZE, BATCH_SIZE

# Fallback in case tabulate isn't installed, but we'll try to use it for clean tables
try:
    from tabulate import tabulate
    HAS_TABULATE = True
except ImportError:
    HAS_TABULATE = False

DATASET_ROOT = "./Dataset"
CHECKPOINT_PATH = "checkpoints/dermai_checkpoint_epoch_20.pth"

def evaluate_model(model, val_loader, device):
    """
    Runs inference over the entire validation dataset.
    """
    model.eval()
    all_preds = []
    all_labels = []
    
    with torch.no_grad():
        for inputs, labels in tqdm(val_loader, desc="Scanning Validation Set"):
            inputs, labels = inputs.to(device), labels.to(device)
            # Use AMP autocast for inference speed
            with torch.amp.autocast('cuda'):
                outputs = model(inputs)
            _, preds = torch.max(outputs, 1)
            all_preds.extend(preds.cpu().numpy())
            all_labels.extend(labels.cpu().numpy())
            
    return np.array(all_labels), np.array(all_preds)

def print_terminal_report(y_true, y_pred, classes):
    """
    Prints a structured terminal report showing key medical metrics and mapping confusion points.
    """
    acc = accuracy_score(y_true, y_pred)
    macro_f1 = f1_score(y_true, y_pred, average='macro')
    weighted_f1 = f1_score(y_true, y_pred, average='weighted')
    
    print("\n" + "="*60)
    print("🏆 DermAI Super-Expert Audit Report 🏆".center(60))
    print("="*60)
    
    metrics = [
        ["Overall Accuracy", f"{acc*100:.2f}%"],
        ["Macro F1-Score", f"{macro_f1:.4f}"],
        ["Weighted F1-Score", f"{weighted_f1:.4f}"]
    ]
    
    if HAS_TABULATE:
        print("\n" + tabulate(metrics, headers=["Metric", "Score"], tablefmt="fancy_grid"))
    else:
        print("\n--- Core Metrics ---")
        for m in metrics:
            print(f"{m[0]:<20} |   {m[1]}")
            
    # Find the most confused pair
    cm = confusion_matrix(y_true, y_pred, labels=range(len(classes)))
    np.fill_diagonal(cm, 0) # Zero out the correct predictions
    
    if cm.max() > 0:
        max_idx = np.unravel_index(cm.argmax(), cm.shape)
        true_confused = classes[max_idx[0]]
        pred_confused = classes[max_idx[1]]
        count = cm[max_idx]
        
        print("\n⚠️ DIAGNOSTIC VULNERABILITY ⚠️")
        print(f"The most confused pair is:")
        print(f"> '{true_confused}' is most often misdiagnosed as")
        print(f"> '{pred_confused}' ({count} times).")
    else:
        print("\n🌟 Perfect validation score! No diagnostic misclassifications found.")
        
    print("="*60 + "\n")

def create_dashboard(y_true, y_pred, classes, history=None):
    """
    Generates a high-quality visualization dashboard containing the Confusion Matrix
    and F1-Scores Bar Chart.
    """
    # F1 scores per class
    f1_per_class = f1_score(y_true, y_pred, average=None, labels=range(len(classes)))
    
    # Full Confusion Matrix
    cm = confusion_matrix(y_true, y_pred, labels=range(len(classes)))
    
    # Layout size adapts to whether loss history is plotted or not
    fig_cols = 3 if history else 2
    fig = plt.figure(figsize=(10 * fig_cols, 10))
    
    # 1. Confusion Matrix Subplot
    ax1 = fig.add_subplot(1, fig_cols, 1)
    # Shorten class names to be readable on the matrix axes
    short_classes = [c[:20] + "..." if len(c) > 20 else c for c in classes]
    sns.heatmap(cm, annot=True, fmt='d', cmap='Blues', ax=ax1, 
                xticklabels=short_classes, yticklabels=short_classes, cbar=False)
    ax1.set_title('Super-Expert Confusion Matrix', fontsize=16, pad=15)
    ax1.set_xlabel('Predicted Diagnosis', fontsize=12, labelpad=10)
    ax1.set_ylabel('Actual Ground Truth', fontsize=12, labelpad=10)
    plt.setp(ax1.get_xticklabels(), rotation=45, ha="right", rotation_mode="anchor")
    
    # 2. F1-Score Bar Chart
    ax2 = fig.add_subplot(1, fig_cols, 2)
    sns.barplot(x=f1_per_class, y=short_classes, ax=ax2, hue=short_classes, legend=False, palette="viridis")
    ax2.set_title('Diagnostic F1-Score (Confidence per Disease)', fontsize=16, pad=15)
    ax2.set_xlabel('F1-Score', fontsize=12)
    ax2.set_xlim(0, 1.05)
    
    # Add actual value strings text to the end of bars
    for i, v in enumerate(f1_per_class):
        ax2.text(v + 0.01, i, f"{v:.2f}", va='center', fontsize=11)
    
    # 3. Training & Validation Loss Curve (if history was saved)
    if history and "train_loss" in history and "val_loss" in history:
        ax3 = fig.add_subplot(1, fig_cols, 3)
        epochs = range(1, len(history["train_loss"]) + 1)
        ax3.plot(epochs, history["train_loss"], label="Train Loss", marker='o', linewidth=2)
        ax3.plot(epochs, history["val_loss"], label="Val Loss", marker='s', linewidth=2)
        ax3.set_title('Training Convergence Analytics', fontsize=16, pad=15)
        ax3.set_xlabel('Epochs', fontsize=12)
        ax3.set_ylabel('CrossEntropy Loss', fontsize=12)
        ax3.legend(fontsize=12)
        ax3.grid(True, linestyle='--', alpha=0.7)
    
    plt.tight_layout()
    os.makedirs('reports', exist_ok=True)
    plot_name = 'reports/model_performance_dashboard.png'
    plt.savefig(plot_name, dpi=300, bbox_inches='tight')
    print(f"📊 Visual dashboard completely generated: saved locally to '{plot_name}'")

def main():
    device = "cuda" if torch.cuda.is_available() else "cpu"
    print(f"Audit Environment Target Device: {device.upper()}")
    
    if not os.path.exists(CHECKPOINT_PATH):
        print(f"Error: Required Super-Expert checkpoint '{CHECKPOINT_PATH}' not found!")
        return
        
    val_dir = os.path.join(DATASET_ROOT, "test")
    if not os.path.exists(val_dir):
        print(f"Error: Validation directory '{val_dir}' not found!")
        return

    # Setup Robust Validation DataLoader
    val_transforms = get_val_transforms(target_size=IMAGE_SIZE)
    val_dataset = DermAIDataset(
        root_dir=val_dir, 
        transform=val_transforms, 
        allowed_classes=EXPERT_CLASSES,
        class_mapping=CLASS_MAPPING
    )
    val_loader = DataLoader(val_dataset, batch_size=BATCH_SIZE, shuffle=False, num_workers=4, pin_memory=True)
    
    # Initialize Model Infrastructure (EfficientNet-B0)
    print("\nLoading EfficientNet-B0 Framework...")
    model = build_dermai_model(num_classes=len(EXPERT_CLASSES), pretrained=False)
    
    # Inject Final Weights carefully mapping to current hardware device
    checkpoint = torch.load(CHECKPOINT_PATH, map_location=device, weights_only=True)
    model.load_state_dict(checkpoint['model_state_dict'])
    
    # See if the history dict exists attached in our checkpoint dictionary
    history = checkpoint.get('history', None) 
    
    model.to(device)
    
    print("\nExecuting Comprehensive Deep-Scan Validation Array...")
    y_true, y_pred = evaluate_model(model, val_loader, device)
    
    # Map classes based on internal validation indexing identically to network
    classes = val_dataset.classes
    
    # Generate Reporting Tools
    print_terminal_report(y_true, y_pred, classes)
    create_dashboard(y_true, y_pred, classes, history)

if __name__ == "__main__":
    main()