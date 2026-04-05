import torch
import torch.nn as nn
from torchvision.models import efficientnet_b0, EfficientNet_B0_Weights
import torchinfo
from typing import Tuple

def build_dermai_model(num_classes: int, pretrained: bool = True) -> nn.Module:
    """
    Builds and returns a modified EfficientNet-B0 model for DermAI classification.

    Args:
        num_classes (int): The number of output classes for the final layer.
        pretrained (bool): If True, loads the default weights (EfficientNet_B0_Weights.DEFAULT).
                           If False, initializes without pretrained weights. Default is True.

    Returns:
        nn.Module: The configured EfficientNet-B0 PyTorch model with a custom classification head.
    """
    if pretrained:
        weights = EfficientNet_B0_Weights.DEFAULT
    else:
        weights = None
        
    # Initialize the backbone
    model = efficientnet_b0(weights=weights)
    
    # EfficientNet-b0's classifier[1] is the final linear layer
    in_features = model.classifier[1].in_features
    # Verify it matches expected EfficientNet-b0 features (1280)
    assert in_features == 1280, f"Expected 1280 in_features for EfficientNet-B0, got {in_features}"
    
    model.classifier[1] = nn.Linear(in_features=in_features, out_features=num_classes)
    
    return model

def print_model_summary(model: nn.Module, input_size: Tuple[int, int, int, int] = (1, 3, 224, 224)) -> None:
    """
    Prints the network architecture and its estimated VRAM usage in Megabytes.
     Critical to ensure we stay under the 4.0 GB VRAM constraint of the target RTX 3050 GPU.

    Args:
        model (nn.Module): The PyTorch model to summarize.
        input_size (Tuple[int, int, int, int]): The input tensor shape (batch_size, channels, height, width).
                                                Default is (1, 3, 224, 224).
    """
    print("=" * 60)
    print("DermAI Architecture & VRAM Usage Summary")
    print("=" * 60)
    
    # torchinfo.summary calculates MACs, parameters, and sizes including VRAM estimates
    summary_str = torchinfo.summary(model, input_size=input_size, col_names=("input_size", "output_size", "num_params", "mult_adds"), verbose=0)
    print(summary_str)
    print("=" * 60)
