import os
import shutil

def get_file_count(directory):
    """Returns the number of files in a directory."""
    if not os.path.exists(directory):
        return 0
    return len([f for f in os.listdir(directory) if os.path.isfile(os.path.join(directory, f))])

def move_files_to_other(src_dir, dest_dir, class_name):
    """Moves all files from src_dir to dest_dir, prepending the class name."""
    if not os.path.exists(src_dir):
        return

    os.makedirs(dest_dir, exist_ok=True)
    
    files = [f for f in os.listdir(src_dir) if os.path.isfile(os.path.join(src_dir, f))]
    for file in files:
        src_path = os.path.join(src_dir, file)
        # Prepend class name to avoid collision
        new_name = f"{class_name}_{file}"
        dest_path = os.path.join(dest_dir, new_name)
        shutil.move(src_path, dest_path)
    
    # Try to remove original dir
    try:
        os.rmdir(src_dir)
    except OSError:
        print(f"Warning: Could not remove directory {src_dir}. It may not be completely empty.")

def main():
    print("=== DermAI Class Consolidation Script ===")
    base_train = os.path.join(".", "Dataset", "train")
    base_test = os.path.join(".", "Dataset", "test")

    # 1. Name Unification (Safety Check)
    atopic_dir = os.path.join(base_test, "Atopic Dermatitis Photos")
    eczema_test_dir = os.path.join(base_test, "Eczema Photos")
    
    if os.path.exists(atopic_dir):
        if not os.path.exists(eczema_test_dir):
            os.rename(atopic_dir, eczema_test_dir)
            print(f"Renamed: '{atopic_dir}' -> 'Eczema Photos'")
        else:
            # If for some reason Eczema Photos already exists, merge them
            print(f"Merging '{atopic_dir}' into '{eczema_test_dir}'...")
            for item in os.listdir(atopic_dir):
                shutil.move(os.path.join(atopic_dir, item), os.path.join(eczema_test_dir, item))
            os.rmdir(atopic_dir)

    # Ensure train and test exist
    if not os.path.exists(base_train) or not os.path.exists(base_test):
        print("Error: Dataset/train or Dataset/test does not exist!")
        return

    # 2. Find All Classes
    train_classes = [d for d in os.listdir(base_train) if os.path.isdir(os.path.join(base_train, d))]
    test_classes = [d for d in os.listdir(base_test) if os.path.isdir(os.path.join(base_test, d))]
    
    # Get unique set of all classes
    all_classes = set(train_classes + test_classes)
    if "Other" in all_classes:
        all_classes.remove("Other")  # We'll ignore the 'Other' category itself

    moved_classes = []

    # 3. Count and Evaluate
    train_other = os.path.join(base_train, "Other")
    test_other = os.path.join(base_test, "Other")

    for cls in all_classes:
        train_cls_dir = os.path.join(base_train, cls)
        test_cls_dir = os.path.join(base_test, cls)

        train_count = get_file_count(train_cls_dir)
        test_count = get_file_count(test_cls_dir)
        total_count = train_count + test_count

        # 4. The Merge Logic
        if total_count < 250:
            print(f"[{cls}]: Combined count is {total_count}. Merging into 'Other'...")
            move_files_to_other(train_cls_dir, train_other, cls)
            move_files_to_other(test_cls_dir, test_other, cls)
            moved_classes.append(cls)
        else:
            print(f"[{cls}]: Combined count is {total_count}. Keeping as standalone class.")

    # 5. Report
    print("\n" + "="*50)
    print("CONSOLIDATION SUMMARY")
    print("="*50)
    if not moved_classes:
        print("No classes were small enough to be merged.")
    else:
        print("The following classes were merged into 'Other':")
        for cls in moved_classes:
            print(f"  - {cls}")

    final_train_other_count = get_file_count(train_other)
    final_test_other_count = get_file_count(test_other)
    
    print("\nFinal Image Counts in 'Other':")
    print(f"  Train/Other : {final_train_other_count} files")
    print(f"  Test/Other  : {final_test_other_count} files")
    print("="*50)

if __name__ == "__main__":
    main()