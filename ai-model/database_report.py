import os
import math
import matplotlib.pyplot as plt
import numpy as np

try:
    from tabulate import tabulate
    HAS_TABULATE = True
except ImportError:
    HAS_TABULATE = False

CLASS_MAPPING = {
    'Eczema Photos': 'Eczema (Atopic Dermatitis)',
    'Atopic Dermatitis Photos': 'Eczema (Atopic Dermatitis)'
}

TRAIN_DIR = "./Dataset/train"
VAL_DIR = "./Dataset/test"
VALID_EXTENSIONS = {".jpg", ".jpeg", ".png", ".bmp", ".webp"}

# Dynamically detect all classes from the train directory
if os.path.exists(TRAIN_DIR):
    _folders = [d for d in os.listdir(TRAIN_DIR) if os.path.isdir(os.path.join(TRAIN_DIR, d))]
    EXPERT_CLASSES = sorted(list({CLASS_MAPPING.get(f, f) for f in _folders}))
else:
    EXPERT_CLASSES = []

def count_images_in_dir(base_dir):
    """
    Scans the directory and maps folder contents to the EXPERT_CLASSES.
    Returns a dictionary of counts per unified class.
    """
    counts = {cls: 0 for cls in EXPERT_CLASSES}
    
    if not os.path.exists(base_dir):
        return counts

    for folder in os.listdir(base_dir):
        folder_path = os.path.join(base_dir, folder)
        if not os.path.isdir(folder_path):
            continue
            
        # Map physical folder to unified class
        unified_class = CLASS_MAPPING.get(folder, folder)
        
        # Only count if it's one of our target expert classes
        if unified_class in counts:
            images = [f for f in os.listdir(folder_path) 
                      if os.path.splitext(f)[1].lower() in VALID_EXTENSIONS]
            counts[unified_class] += len(images)
            
    return counts

def calculate_data_health(class_totals, overall_total):
    """
    Calculates a "Data Health Score" out of 100 using normalized Shannon Entropy.
    100 = perfectly balanced dataset
    0 = all images belong to a single class
    """
    if overall_total == 0:
        return 0.0
        
    num_classes = len(EXPERT_CLASSES)
    entropy = 0.0
    
    for count in class_totals:
        if count > 0:
            p_i = count / overall_total
            # Log base N normalizes the entropy between 0 and 1
            entropy -= p_i * math.log(p_i, num_classes)
            
    return entropy * 100

def generate_report():
    print("Scanning Dataset...")
    train_counts = count_images_in_dir(TRAIN_DIR)
    val_counts = count_images_in_dir(VAL_DIR)
    
    # Aggregate data
    report_data = []
    overall_total = 0
    
    for cls in EXPERT_CLASSES:
        t_count = train_counts[cls]
        v_count = val_counts[cls]
        total = t_count + v_count
        overall_total += total
        report_data.append({
            "class": cls,
            "train": t_count,
            "val": v_count,
            "total": total
        })
        
    if overall_total == 0:
        print(f"Error: No valid images found in {TRAIN_DIR} or {VAL_DIR} matching EXPERT_CLASSES.")
        return

    # Sort data by total descending for better visualization
    report_data.sort(key=lambda x: x["total"], reverse=True)
    
    # Fill in percentages & prep for table
    table_rows = []
    class_names_sorted = []
    totals_sorted = []
    
    for item in report_data:
        pct = (item["total"] / overall_total) * 100
        table_rows.append([
            item["class"], 
            item["train"], 
            item["val"], 
            item["total"], 
            f"{pct:.1f}%"
        ])
        class_names_sorted.append(item["class"])
        totals_sorted.append(item["total"])

    # 1. Print Terminal Report
    print("\n" + "="*80)
    print("DermAI Database Audit Report".center(80))
    print("="*80)
    
    headers = ["Disease Category", "Training Count", "Validation Count", "Total", "% of Total Dataset"]
    if HAS_TABULATE:
        print(tabulate(table_rows, headers=headers, tablefmt="fancy_grid"))
    else:
        # Fallback formatting
        print(f"{headers[0]:<45} | {headers[1]:<14} | {headers[2]:<16} | {headers[3]:<6} | {headers[4]}")
        print("-" * 105)
        for row in table_rows:
            print(f"{row[0]:<45} | {row[1]:<14} | {row[2]:<16} | {row[3]:<6} | {row[4]}")

    # 2. Insights & Data Health Score
    health_score = calculate_data_health(totals_sorted, overall_total)
    print("\n" + "="*80)
    print("DATABASE INSIGHTS".center(80))
    print("="*80)
    print(f"Total Medical Images Indexed: {overall_total}")
    print(f"Dataset Health Score: {health_score:.1f}/100.0")
    
    if health_score >= 90:
        print("Verdict: Excellent Balance! The model will learn patterns fairly across all diseases.")
    elif health_score >= 70:
        print("Verdict: Acceptable Balance. Some minor representation bias exists but is manageable.")
    else:
        print("Verdict: High Imbalance Detected! The dataset is dangerously skewed towards specific 'Data-Rich' classes.")
        print("Recommendation: Utilize the dynamic class weighting in train.py (already implemented!) to penalize over-represented classes.")
    print("="*80 + "\n")

    # 3. Generate Data Health Visualization
    short_names = [c[:25] + "..." if len(c) > 25 else c for c in class_names_sorted]
    
    # Create colormap based on 'Data-Rich' vs 'Data-Lean'
    # We map the counts to a color gradient (Reds for low, Greens/Blues for high)
    norm = plt.Normalize(min(totals_sorted), max(totals_sorted))
    colors = plt.cm.RdYlGn(norm(totals_sorted))
    
    fig, ax = plt.subplots(figsize=(12, 8))
    bars = ax.barh(short_names, totals_sorted, color=colors, edgecolor='black')
    
    # Invert y-axis to have the highest count at the top
    ax.invert_yaxis()
    
    # Add values at the end of each bar
    for bar in bars:
        width = bar.get_width()
        label_x_pos = width + (max(totals_sorted) * 0.01)
        ax.text(label_x_pos, bar.get_y() + bar.get_height()/2, f'{int(width)}', 
                va='center', fontsize=11, fontweight='bold')
                
    ax.set_title('DermAI Dataset Distribution: Data-Rich vs Data-Lean Classes', fontsize=16, pad=15)
    ax.set_xlabel('Total Number of Images', fontsize=12, labelpad=10)
    ax.set_ylabel('Disease Category', fontsize=12, labelpad=10)
    
    # Light grid for readability
    ax.xaxis.grid(True, linestyle='--', alpha=0.6)
    
    plt.tight_layout()
    os.makedirs('reports', exist_ok=True)
    plot_name = "reports/dataset_distribution.png"
    plt.savefig(plot_name, dpi=300)
    print(f"📊 Visualization successfully generated: '{plot_name}'")

if __name__ == "__main__":
    generate_report()
