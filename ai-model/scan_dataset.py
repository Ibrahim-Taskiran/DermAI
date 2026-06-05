import os
from collections import Counter
import matplotlib.pyplot as plt

def scan_medical_dataset(root_path):
    print(f"--- Scanning Dataset at: {root_path} ---")
    
    report = {}
    
    # Check if there are train/test splits, or direct class folders
    splits = ['train', 'test']
    has_splits = any(os.path.exists(os.path.join(root_path, split)) for split in splits)
    
    if has_splits:
        scan_dirs = splits
    else:
        scan_dirs = ['']
        
    for split in scan_dirs:
        split_path = os.path.join(root_path, split) if split else root_path
        if not os.path.exists(split_path):
            continue
            
        print(f"\nAnalyzing {split.upper() if split else 'ROOT'} split...")
        class_counts = {}
        for folder in os.listdir(split_path):
            folder_path = os.path.join(split_path, folder)
            if os.path.isdir(folder_path):
                images = [f for f in os.listdir(folder_path) if f.lower().endswith(('.jpg', '.jpeg', '.png'))]
                class_counts[folder] = len(images)
        
        report[split] = class_counts
        
        # Print a quick summary table
        print(f"{'Disease Category':<60} | {'Images':<6}")
        print("-" * 70)
        for cls, count in sorted(class_counts.items(), key=lambda x: x[1], reverse=True):
            print(f"{cls:<60} | {count:<6}")

    return report

if __name__ == "__main__":
    dataset_path = "./Dataset2" # Change this if your path is different
    results = scan_medical_dataset(dataset_path)