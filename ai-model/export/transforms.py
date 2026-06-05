import torch
from torchvision.transforms import v2
from PIL import Image, ImageDraw

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

class BlackoutCenteredWatermark:
    """
    Draws a black blackout box precisely over the center of the image.
    This effectively "crops out" the area where the dataset watermark exists, 
    so the AI is forced to look at the surrounding tissue rather than learning 
    the text as a shortcut feature.
    """
    def __init__(self, width_ratio: float = 0.5, height_ratio: float = 0.3):
        self.width_ratio = width_ratio
        self.height_ratio = height_ratio

    def __call__(self, img: Image.Image) -> Image.Image:
        img_copy = img.copy()
        draw = ImageDraw.Draw(img_copy)
        w, h = img_copy.size
        
        box_w = w * self.width_ratio
        box_h = h * self.height_ratio
        
        x0 = (w - box_w) / 2
        y0 = (h - box_h) / 2
        x1 = x0 + box_w
        y1 = y0 + box_h
        
        draw.rectangle([x0, y0, x1, y1], fill=(0, 0, 0))
        return img_copy

def get_train_transforms(target_size: int = 224) -> v2.Compose:
    """
    Returns the aggressive training transforms pipeline using torchvision v2.
    """
    return v2.Compose([
        v2.Resize(target_size, antialias=True),
        v2.CenterCrop(target_size),
        BlackoutCenteredWatermark(width_ratio=0.7, height_ratio=0.15),
        v2.RandomRotation(degrees=45),
        v2.ColorJitter(brightness=0.3, contrast=0.3),
        v2.RandomHorizontalFlip(p=0.5),
        v2.RandomVerticalFlip(p=0.5),
        v2.ToImage(),
        v2.ToDtype(torch.float32, scale=True),
        v2.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
        v2.RandomErasing(p=0.5, scale=(0.02, 0.1), ratio=(0.3, 3.3), value=0)
    ])

def get_val_transforms(target_size: int = 224) -> v2.Compose:
    """
    Returns the validation pipeline using torchvision v2.
    """
    return v2.Compose([
        v2.Resize(target_size, antialias=True),
        v2.CenterCrop(target_size),
        BlackoutCenteredWatermark(width_ratio=0.7, height_ratio=0.15),
        v2.ToImage(),
        v2.ToDtype(torch.float32, scale=True),
        v2.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
    ])
