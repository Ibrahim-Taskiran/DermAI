import os
import random
from typing import List, Optional, Tuple, Callable

from PIL import Image
from torch.utils.data import Dataset
from config import EXPERT_CLASSES, CLASS_MAPPING, IMAGE_SIZE

class DermAIDataset(Dataset):
    """
    A custom PyTorch Dataset for loading DermAI medical image data.
    
    Dynamically maps folder names to integer labels based on the directory structure.
    Optionally filters loaded classes based on an explicit `allowed_classes` list.
    """

    def __init__(
        self,
        root_dir: str,
        allowed_classes: Optional[List[str]] = EXPERT_CLASSES,
        transform: Optional[Callable] = None,
        class_mapping: Optional[dict] = CLASS_MAPPING,
    ):
        """
        Initializes the DermAIDataset.

        Args:
            root_dir (str): Path to the root dataset directory (e.g., 'Dataset/train' or 'Dataset/test').
            allowed_classes (Optional[List[str]]): A list of specific class names to load.
                                                   Defaults to EXPERT_CLASSES from config.py.
            transform (Optional[Callable]): A function/transform that takes in a PIL image
                                            and returns a transformed version.
            class_mapping (Optional[dict]): A dictionary mapping folder names to unified class names.
                                            Defaults to CLASS_MAPPING from config.py.
        """
        self.root_dir = root_dir
        self.transform = transform
        self.class_mapping = class_mapping or {}
        
        # Discover all subdirectories
        try:
            all_folders = [d for d in os.listdir(root_dir) if os.path.isdir(os.path.join(root_dir, d))]
        except FileNotFoundError:
            raise FileNotFoundError(f"Root directory {root_dir} does not exist.")
            
        if not all_folders:
            raise ValueError(f"No subdirectories found in {root_dir}.")

        # Map folders to their respective class names
        self.folder_to_class = {folder: self.class_mapping.get(folder, folder) for folder in all_folders}
        all_classes = list(set(self.folder_to_class.values()))

        # Filter by allowed_classes if provided
        if allowed_classes is not None:
            valid_classes = set(all_classes).intersection(set(allowed_classes))
            if not valid_classes:
                raise ValueError("None of the specified `allowed_classes` were found down in the class mappings.")
            self.classes = sorted(list(valid_classes))
        else:
            self.classes = sorted(all_classes)
            
        # Dynamically map unified class names to integer labels
        self.class_to_idx = {cls_name: idx for idx, cls_name in enumerate(self.classes)}
        
        # Load all image paths and their corresponding labels
        self.samples: List[Tuple[str, int]] = []
        valid_extensions = {".jpg", ".jpeg", ".png", ".bmp", ".webp"}
        
        class_paths = {cls_name: [] for cls_name in self.classes}
        
        for folder in all_folders:
            cls_name = self.folder_to_class[folder]
            
            # Skip folders that do not map to an allowed class
            if cls_name not in self.classes:
                continue
                
            cls_dir = os.path.join(self.root_dir, folder)
            class_idx = self.class_to_idx[cls_name]
            
            for file_name in os.listdir(cls_dir):
                ext = os.path.splitext(file_name)[1].lower()
                if ext in valid_extensions:
                    img_path = os.path.abspath(os.path.normpath(os.path.join(cls_dir, file_name)))
                    # Check that the file actually exists
                    if os.path.isfile(img_path):
                        class_paths[cls_name].append((img_path, class_idx))
                        
        # Cap images at 500 per class using a fixed random seed
        for cls_name, paths in class_paths.items():
            if len(paths) > 500:
                random.seed(42)
                paths = random.sample(paths, 500)
            self.samples.extend(paths)

    def __len__(self) -> int:
        """Returns the total number of samples in the dataset."""
        return len(self.samples)

    def __getitem__(self, idx: int) -> Tuple[Image.Image, int]:
        """
        Fetches the image and label at the specified index.

        Args:
            idx (int): Index of the sample to fetch.

        Returns:
            Tuple[Image.Image, int]: A tuple containing the transformed image and its label.
        """
        img_path, label = self.samples[idx]
        
        try:
            # Use simple context manager to avoid leaking file handles
            with open(img_path, "rb") as f:
                image = Image.open(f).convert("RGB")
                
            if self.transform is not None:
                image = self.transform(image)
                
            return image, label
        except Exception as e:
            print(f"Warning: Failed to load image {img_path} ({e}). Sampling another random image instead.")
            # Randomly select a different index to prevent training from crashing
            new_idx = random.randint(0, len(self.samples) - 1)
            return self.__getitem__(new_idx)
