import os
import shutil
import uuid

def merge_datasets():
    print("=== DermAI Dataset Merge Script ===")
    base_dir = "./Dataset"
    test_dir = os.path.join(base_dir, "test")
    train_dir = os.path.join(base_dir, "train")
    master_dir = os.path.join(base_dir, "Master_Dataset")

    if not os.path.exists(train_dir):
        print(f"Error: Train directory '{train_dir}' does not exist.")
        return
        
    if os.path.exists(test_dir):
        print("Moving images from test to train...")
        for folder_name in os.listdir(test_dir):
            test_folder_path = os.path.join(test_dir, folder_name)
            train_folder_path = os.path.join(train_dir, folder_name)
            
            if not os.path.isdir(test_folder_path):
                continue
                
            # Make sure corresponding train folder exists
            os.makedirs(train_folder_path, exist_ok=True)
            
            for file_name in os.listdir(test_folder_path):
                src_file = os.path.join(test_folder_path, file_name)
                if not os.path.isfile(src_file):
                    continue
                    
                dest_file = os.path.join(train_folder_path, file_name)
                
                # Check for collision
                if os.path.exists(dest_file):
                    name, ext = os.path.splitext(file_name)
                    unique_id = uuid.uuid4().hex[:8]  # short random string
                    new_name = f"{name}_merged_{unique_id}{ext}"
                    dest_file = os.path.join(train_folder_path, new_name)
                    
                shutil.move(src_file, dest_file)
            
            # Remove the empty class folder inside test
            try:
                os.rmdir(test_folder_path)
            except OSError as e:
                print(f"Warning: Could not delete empty folder '{test_folder_path}': {e}")
        
        # Remove the main test directory
        try:
            os.rmdir(test_dir)
            print(f"Deleted the empty '{test_dir}' directory.")
        except OSError as e:
            print(f"Warning: Could not delete '{test_dir}': {e}")
    else:
        print(f"Notice: '{test_dir}' does not exist. Skipping move step.")

    # Rename train to Master_Dataset
    if os.path.exists(master_dir):
        print(f"Notice: '{master_dir}' already exists. Merging train into it if necessary.")
        # Alternatively, exit if master exists to be safe
    else:
        os.rename(train_dir, master_dir)
        print(f"Renamed '{train_dir}' to '{master_dir}'.")

    # Print summary
    print("\n=== Initializing Master Dataset Count ===")
    total_images = 0
    if os.path.exists(master_dir):
        for folder_name in os.listdir(master_dir):
            folder_path = os.path.join(master_dir, folder_name)
            if os.path.isdir(folder_path):
                # Count files
                count = len([f for f in os.listdir(folder_path) if os.path.isfile(os.path.join(folder_path, f))])
                print(f" ✓ {folder_name:<40}: {count} images")
                total_images += count
    print("=" * 55)
    print(f"Total Master Dataset Images: {total_images}\n")

if __name__ == "__main__":
    merge_datasets()