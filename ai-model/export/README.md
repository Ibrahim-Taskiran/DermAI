# DermAI Export Package (v2)

This directory contains the necessary files to run inference using the trained DermAI model. It has been updated to support the full 12-class dataset.

## Contents
- `dermai.pth`: The trained model weights.
- `predict.py` / `inference.py`: Scripts to run predictions on new images.
- `config.py`: Configuration parameters and class mappings.
- `model.py`: Neural network architecture definition.
- `transforms.py`: Image preprocessing functions (e.g., smart padding).
- `requirements.txt`: Python dependencies needed for inference.

## Setup Instructions

1. Install the required dependencies:
   ```bash
   pip install -r requirements.txt
   ```

2. Run inference on a test image (example):
   ```bash
   python predict.py --image path/to/image.jpg
   ```
