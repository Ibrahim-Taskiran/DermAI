import os
import matplotlib.pyplot as plt

def plot_loss_curves(history, save_dir="reports"):
    """
    Plots the training and validation loss curves and saves the figure.
    """
    # Create the directory if it doesn't exist
    os.makedirs(save_dir, exist_ok=True)
    
    # Extract data securely
    train_loss = history.get("train_loss", [])
    val_loss = history.get("val_loss", [])
    
    epochs = range(1, len(train_loss) + 1)
    
    plt.figure(figsize=(10, 6))
    
    # Plot lines according to styling requirements
    plt.plot(epochs, train_loss, linestyle='-', label='Training Loss', color='blue', linewidth=2)
    if val_loss:
        # Assuming val_loss has the same length as train_loss
        plt.plot(epochs, val_loss, linestyle='--', label='Validation Loss', color='orange', linewidth=2)
        
    # English labels & Grid
    plt.xlabel("Epoch", fontsize=12)
    plt.ylabel("Loss", fontsize=12)
    plt.title("Training vs Validation Loss", fontsize=14)
    plt.legend(fontsize=12)
    
    # Add horizontal gridlines for readability
    plt.grid(axis='y', linestyle='--', alpha=0.7)
    
    # Save as high-resolution PNG
    save_path = os.path.join(save_dir, "training_loss_curve.png")
    plt.savefig(save_path, dpi=300, bbox_inches='tight')
    plt.close()
    
    print(f"✅ Loss curve successfully saved to: {save_path}")

