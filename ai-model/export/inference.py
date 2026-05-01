import torch
import torch.nn.functional as F
from PIL import Image
from typing import List, Dict, Any, Callable

def predict_image(
    image_path: str, 
    model: torch.nn.Module, 
    val_transforms: Callable, 
    class_names: List[str], 
    device: str
) -> Dict[str, List[Dict[str, Any]]]:
    """
    Runs inference on a single medical image and returns the Top-3 predictions.
    
    The output is strictly formatted as a dictionary to be consumed by the UI,
    mapping the predicted class to its corresponding probability.

    Args:
        image_path (str): Absolute or relative path to the image file.
        model (torch.nn.Module): The trained PyTorch model (e.g., ResNet-18).
        val_transforms (Callable): The validation transforms pipeline from Module A.
        class_names (List[str]): List mapping output indices to string class names.
        device (str): Device to run inference on ('cuda' or 'cpu').

    Returns:
        Dict[str, List[Dict[str, Any]]]: Ex: 
            {"predictions": [{"disease_id": "Normal", "probability": 0.95}, ...]}
    """
    # Ensure model is in evaluation mode
    model.eval()
    
    # 1. Preprocessing: Load the image safely using PIL
    with open(image_path, "rb") as f:
        image = Image.open(f).convert("RGB")
        
    # Apply validation transforms and add the batch dimension: (1, C, H, W)
    input_tensor = val_transforms(image).unsqueeze(0).to(device)
    
    # 2. Inference: Forward pass within no_grad context
    with torch.no_grad():
        outputs = model(input_tensor)
        
        # Apply softmax across the class dimension to generate 0.0 - 1.0 confidence scores
        probabilities = F.softmax(outputs, dim=1)
        
    # 3. Top-K Logic: Extract the top 3 highest probabilities and their tensor indices
    k = min(3, len(class_names))
    top_prob, top_indices = torch.topk(probabilities, k=k, dim=1)
    
    # Flatten the batch dimension for easy iteration
    top_prob = top_prob.squeeze(0).cpu().tolist()
    top_indices = top_indices.squeeze(0).cpu().tolist()
    
    # 4. JSON Mapping
    predictions_list = []
    for prob, idx in zip(top_prob, top_indices):
        disease_id = class_names[idx]
        predictions_list.append({
            "disease_id": disease_id,
            "probability": float(prob)
        })
        
    return {"predictions": predictions_list}
