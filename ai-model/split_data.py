import os
import shutil
import random
from tqdm import tqdm

SOURCE_DIR = './Raw_Dataset'
TRAIN_DIR = './Dataset/train'
VAL_DIR = './Dataset/val'
TRAIN_RATIO = 0.8

def split_dataset():
    """
    Randomly splits the dataset in SOURCE_DIR into training and validation sets
    and copies them to TRAIN_DIR and VAL_DIR.
    """
    if not os.path.exists(SOURCE_DIR):
        print(f"Error: Source directory '{SOURCE_DIR}' does not exist.")
        print("Please ensure your raw images are placed in './Raw_Dataset' with disease subfolders.")
        return

    # Find all subdirectories (classes) in the source directory
    classes = [d for d in os.listdir(SOURCE_DIR) if os.path.isdir(os.path.join(SOURCE_DIR, d))]
    
    if not classes:
        print(f"No class subdirectories found in '{SOURCE_DIR}'.")
        return

    # Collect all copy operations as tuples of (source_path, destination_path)
    copy_tasks = []
    
    # Optional: seed random for reproducibility
    random.seed(42)

    for cls_name in classes:
        class_src_dir = os.path.join(SOURCE_DIR, cls_name)
        
        # Get all valid files in this class directory
        files = [f for f in os.listdir(class_src_dir) if os.path.isfile(os.path.join(class_src_dir, f))]
        
        if not files:
            print(f"Warning: No files found in '{class_src_dir}'. Skipping.")
            continue
            
        # Shuffle the files
        random.shuffle(files)
        
        # Calculate split index
        split_idx = int(len(files) * TRAIN_RATIO)
        
        train_files = files[:split_idx]
        val_files = files[split_idx:]
        
        # Create target directories for this specific class
        train_cls_dir = os.path.join(TRAIN_DIR, cls_name)
        val_cls_dir = os.path.join(VAL_DIR, cls_name)
        
        os.makedirs(train_cls_dir, exist_ok=True)
        os.makedirs(val_cls_dir, exist_ok=True)
        
        # Map files to their respective target destinations
        for f in train_files:
            src_path = os.path.join(class_src_dir, f)
            dst_path = os.path.join(train_cls_dir, f)
            copy_tasks.append((src_path, dst_path))
            
        for f in val_files:
            src_path = os.path.join(class_src_dir, f)
            dst_path = os.path.join(val_cls_dir, f)
            copy_tasks.append((src_path, dst_path))

    print(f"Identified {len(classes)} classes with {len(copy_tasks)} total images.")
    print(f"Splitting strategy: {TRAIN_RATIO*100:.0f}% Train / {(1-TRAIN_RATIO)*100:.0f}% Val")
    print(f"Destinations: '{TRAIN_DIR}' and '{VAL_DIR}'")
    
    # Execute all copy operations with a single progress bar
    for src, dst in tqdm(copy_tasks, desc="Copying files", unit="img"):
        shutil.copy2(src, dst)
        
    print("\nDataset splitting completed successfully!")

if __name__ == "__main__":
    split_dataset()
