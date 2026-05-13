import os
import shutil

def rename_or_merge_folder(src, dst):
    """
    Renames the source folder to the destination folder.
    If the destination already exists, moves all files from src to dst.
    """
    if os.path.exists(src):
        if not os.path.exists(dst):
            os.rename(src, dst)
            print(f"Renamed folder: '{src}' -> '{dst}'")
        else:
            # Merge files if the destination somehow already exists
            print(f"Merging folder: '{src}' into '{dst}'")
            for item in os.listdir(src):
                shutil.move(os.path.join(src, item), os.path.join(dst, item))
            os.rmdir(src)

def rescue_files(split_type, prefixes):
    """
    Rescues files from the 'Other' folder in the specified split (train or test).
    """
    other_folder = os.path.join(".", "Dataset", split_type, "Other")
    
    if not os.path.exists(other_folder):
        print(f"Warning: The folder '{other_folder}' does not exist. Skipping.")
        return 0

    moved_count = 0

    for filename in os.listdir(other_folder):
        for prefix in prefixes:
            if filename.startswith(prefix):
                # We found a corrupted file matching the prefix!
                # E.g. Normal_123.jpg -> destination class is "Normal"
                
                # Create destination folder path
                dest_dir = os.path.join(".", "Dataset", split_type, prefix)
                os.makedirs(dest_dir, exist_ok=True)
                
                # Strip the prefix and any immediately following separators (like '_' or ' ' or '-')
                new_filename = filename[len(prefix):].lstrip(' _-')
                
                old_filepath = os.path.join(other_folder, filename)
                new_filepath = os.path.join(dest_dir, new_filename)
                
                # Prevent overwriting if a file with the exact same name somehow exists
                if os.path.exists(new_filepath):
                    base, ext = os.path.splitext(new_filename)
                    new_filename = f"{base}_rescued{ext}"
                    new_filepath = os.path.join(dest_dir, new_filename)
                
                # Move the file
                shutil.move(old_filepath, new_filepath)
                moved_count += 1
                break  # Stop checking other prefixes for this file
                
    return moved_count

def main():
    print("=== DermAI Dataset Recovery Script ===")
    
    # 1. Unify the Eczema Folders
    train_eczema_src = os.path.join(".", "Dataset", "train", "Eczema Photos")
    train_eczema_dst = os.path.join(".", "Dataset", "train", "Eczema (Atopic Dermatitis)")
    rename_or_merge_folder(train_eczema_src, train_eczema_dst)
    
    test_eczema_src = os.path.join(".", "Dataset", "test", "Atopic Dermatitis Photos")
    test_eczema_dst = os.path.join(".", "Dataset", "test", "Eczema (Atopic Dermatitis)")
    rename_or_merge_folder(test_eczema_src, test_eczema_dst)
    
    # 2. Define the Main Classes (Prefixes)
    target_prefixes = [
        "Acne and Rosacea Photos",
        "Actinic Keratosis Basal Cell Carcinoma and other Malignant Lesions",
        "Light Diseases and Disorders of Pigmentation",
        "Normal",
        "Warts Molluscum and other Viral Infections",
        "Eczema (Atopic Dermatitis)",  # Included in case some files were moved here too
        "Eczema Photos",               # Check legacy names just in case
        "Atopic Dermatitis Photos"     # Check legacy names just in case
    ]
    
    # 3. Rescue the Files
    print("\nRescuing Train files...")
    train_moves = rescue_files("train", target_prefixes)
    
    print("Rescuing Test files...")
    test_moves = rescue_files("test", target_prefixes)
    
    # 4. Clean Up and Summary
    print("\n" + "="*40)
    print("RESCUE SUMMARY")
    print("="*40)
    print(f"Files safely moved back to Train directories : {train_moves}")
    print(f"Files safely moved back to Test directories  : {test_moves}")
    print(f"Total files safely rescued                   : {train_moves + test_moves}")
    print("All un-matching files remain securely isolated inside the 'Other' folders.")
    print("="*40)

if __name__ == "__main__":
    main()