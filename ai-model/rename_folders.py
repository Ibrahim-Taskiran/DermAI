import os

def rename_dataset_folders():
    """
    Renames the long dataset folder names into clean, UI-friendly names
    for both the train and test directories.
    """
    base_train = os.path.join(".", "Dataset", "train")
    base_test = os.path.join(".", "Dataset", "test")
    
    # Define the mapping from old legacy names to clean UI names
    folder_mapping = {
        "Acne and Rosacea Photos": "Acne and Rosacea",
        "Actinic Keratosis Basal Cell Carcinoma and other Malignant Lesions": "Malignant Lesions",
        "Eczema (Atopic Dermatitis)": "Eczema",
        "Eczema Photos": "Eczema",  # Added fallback just in case 'rescue_dataset' wasn't run
        "Light Diseases and Disorders of Pigmentation": "Pigmentation Disorders",
        "Warts Molluscum and other Viral Infections": "Viral Infections"
    }

    folders_to_scan = [base_train, base_test]

    print("=== DermAI UI-Friendly Folder Renamer ===")
    
    for split_dir in folders_to_scan:
        if not os.path.exists(split_dir):
            print(f"Warning: Directory '{split_dir}' not found. Skipping.")
            continue
            
        print(f"\nScanning: {split_dir}")
        for folder_name in os.listdir(split_dir):
            folder_path = os.path.join(split_dir, folder_name)
            
            # Make sure it's actually a directory
            if not os.path.isdir(folder_path):
                continue
                
            # Check if this folder needs to be renamed
            if folder_name in folder_mapping:
                new_folder_name = folder_mapping[folder_name]
                new_folder_path = os.path.join(split_dir, new_folder_name)
                
                # Perform the rename safely
                if not os.path.exists(new_folder_path):
                    os.rename(folder_path, new_folder_path)
                    print(f"✅ Renamed: '{folder_name}' -> '{new_folder_name}'")
                else:
                    print(f"⚠️ Skipping '{folder_name}': Dest '{new_folder_name}' already exists.")

    print("\n=========================================")
    print("Folder renaming process fully complete!")
    print("=========================================")

if __name__ == "__main__":
    rename_dataset_folders()