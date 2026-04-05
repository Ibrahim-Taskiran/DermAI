import torch
from torchvision.transforms import v2
from PIL import Image

class CheckSquarePadTransform:
    """
    Optional Smart Padding:
    Pads the image with black pixels to make it square BEFORE resizing or cropping
    so that no texture stretching occurs.
    """
    def __init__(self, fill: int = 0):
        self.fill = fill

    def __call__(self, img: Image.Image) -> Image.Image:
        w, h = img.size
        if w == h:
            return img
        
        # Calculate padding to make it a perfect square
        pad_w = max(0, h - w)
        pad_h = max(0, w - h)
        
        padding = (pad_w // 2, pad_h // 2, pad_w - (pad_w // 2), pad_h - (pad_h // 2))
        return v2.Pad(padding=padding, fill=self.fill)(img)

def get_train_transforms(target_size: int = 224) -> v2.Compose:
    """
    Returns the aggressive training transforms pipeline using torchvision v2.
    """
    return v2.Compose([
        CheckSquarePadTransform(fill=0),
        v2.Resize(target_size, antialias=True),
        v2.CenterCrop(target_size),
        v2.RandomRotation(degrees=45),
        v2.ColorJitter(brightness=0.3, contrast=0.3), # hue removed since it wasn't requested
        v2.RandomHorizontalFlip(p=0.5),
        v2.RandomVerticalFlip(p=0.5),
        v2.ToImage(),
        v2.ToDtype(torch.float32, scale=True),
        v2.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
    ])

def get_val_transforms(target_size: int = 224) -> v2.Compose:
    """
    Returns the validation pipeline using torchvision v2.
    """
    return v2.Compose([
        CheckSquarePadTransform(fill=0),
        v2.Resize(target_size, antialias=True),
        v2.CenterCrop(target_size),
        v2.ToImage(),
        v2.ToDtype(torch.float32, scale=True),
        v2.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
    ])
