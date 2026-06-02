# A Deep Learning-Based Mobile Skin Disease Classification and Decision-Support System Prototype

**Kerem Selçuk¹, Aybüke Türk², İbrahim Taşkıran³, Muhammet Baykara⁴, Mhd Hadi Said Alkaddour⁵**

*Department of Software Engineering, Firat University, Elazığ, Türkiye*
¹ 240541055@firat.edu.tr, ² 240541115@firat.edu.tr, ³ 230541049@firat.edu.tr, ⁴ mbaykara@firat.edu.tr, ⁵ 250541627@firat.edu.tr

---

> **Abstract** — Dermatological conditions represent a major global health burden, and access to specialist care is often limited. This paper presents DermAI, a preliminary classification and decision-support prototype designed to assist users via mobile-first skin lesion classification. The system integrates a fine-tuned EfficientNet-B0 convolutional neural network with a FastAPI-based REST API backend and an Android mobile application. To resolve class imbalance, we implemented a data capping protocol across twelve distinct skin disease categories. The model, trained using the OneCycleLR scheduler and AdamW optimizer, achieved a validation accuracy of **86.00%** and a macro-averaged F1-score of **0.84** on a stratified test set of **5,047** images. The Android application features a Declarative Compose UI, local WiFi-based deployment, and a 3D body map interface for annotating lesion sites. The clinical decision-support component maps predictions to custom care recommendations and safety warnings. Crucially, the system is presented as a screening and educational tool to support, rather than replace, professional medical diagnosis.

> **Index Terms** — Deep learning, skin disease classification, EfficientNet-B0, decision-support prototype, mobile health, FastAPI, REST API, Android.

---

## I. INTRODUCTION

Skin diseases represent a significant health challenge worldwide, with common conditions like eczema, acne, and viral skin infections affecting hundreds of millions of individuals across all demographics annually. For instance, eczema alone is estimated to affect over 31.6 million people in the United States [1], with a prevalence rate of 10.7% in pediatric cohorts [2]. Despite their prevalence, global access to dermatological expertise is highly unequal. In many developing regions, the ratio of patients to dermatologists exceeds 50,000:1, which severely delays initial evaluations. Delayed screening of malignant lesions, such as basal cell carcinoma or melanoma, can significantly worsen patient outcomes [3].

The rapid advancement of smartphone camera technology and convolutional neural network (CNN) architectures has enabled the development of mobile-based preliminary screening tools. Integrating image classification and deep learning models into mobile systems allows users to obtain quick, preliminary assessments of skin lesions, which can act as a decision-support mechanism [4], [5]. Studies have demonstrated that lightweight architectures can achieve high performance in constrained environments [6]. 

However, many existing studies focus exclusively on raw classification metrics without providing actionable, clinical guidance or integrating these models into functional, end-to-end architectures that can be deployed in local settings [5], [6]. Furthermore, public datasets are often highly imbalanced, which can bias models toward majority classes if not properly controlled [8].

To address these challenges, we present **DermAI**, a preliminary classification and decision-support prototype. The system features a fine-tuned EfficientNet-B0 model trained to classify twelve dermatological categories. The backend consists of a FastAPI RESTful API, and the frontend is an Android application developed in Kotlin. 

The primary contributions of this work are:
1. A fine-tuned EfficientNet-B0 classifier trained on a curated 12-class dataset, achieving a validation accuracy of 86.00% and a macro F1-score of 0.84.
2. A comparative evaluation demonstrating the performance trade-offs of the proposed model against ResNet-50, MobileNetV3, and DenseNet-121.
3. An end-to-end RESTful API server using FastAPI that manages model loading as a memory-efficient singleton, providing sub-second inference latency on commodity CPUs.
4. A mobile client built with Jetpack Compose featuring an interactive 3D body map and robust image URI resolution compatible with Android 13+ Photo Picker.
5. A JSON-based clinical decision-support database linking the twelve classification categories to urgency-stratified care recommendations and safety disclaimers.

The rest of this paper is structured as follows. Section II reviews related work. Section III details the three-tier system architecture. Section IV describes the machine learning model, dataset curation, and training protocol. Section V presents the backend API design. Section VI outlines the Android application. Section VII discusses experimental results. Section VIII addresses limitations and safety considerations, and Section V concludes the paper.

---

## II. RELATED STUDIES

Deep learning models have demonstrated high performance in classifying skin lesions, in some cases performing comparably to board-certified dermatologists when trained on large, curated datasets [11]. The establishment of the International Skin Imaging Collaboration (ISIC) challenges has further standardized evaluation protocols and dataset curation [12].

EfficientNet, introduced by Tan and Le [13], employs a compound scaling method that scales depth, width, and resolution using a fixed set of coefficients. EfficientNet-B0 represents a highly parameterized baseline that achieves high accuracy with fewer parameters compared to ResNet-50 or DenseNet-121, making it suitable for local server or edge deployment.

Mobile systems running local inference, such as those used for diabetic retinopathy screening [15] or tuberculosis detection [16], must balance latency, model size, and user experience. Unlike cloud-based systems like SkinVision [14] that require uploading user data to third-party servers, DermAI performs inference on local servers, which reduces transmission latency and addresses privacy concerns.

---

## III. METHOD

### A. Overview
DermAI utilizes a three-tier architecture comprising: (1) an Android mobile client, (2) a FastAPI REST API server, and (3) an AI inference module. The mobile client transmits image bytes via HTTP/1.1 using `multipart/form-data`, and the server returns a structured JSON payload containing top-3 predictions and clinical guidance. The system is designed with a strict separation of concerns, loading the PyTorch model once during startup as a singleton to eliminate per-request overhead.

```
Fig. 3. Sequence Diagram showing user interaction and data flow between system components
[See: fig3_sequence_diagram.png]
```

### B. Dataset Curation and Preprocessing
The dataset used in this study was compiled from two public sources on the Kaggle platform: "Skin Diseases CNN" [17] and "Skin diseases image dataset" [18]. These datasets consist of clinical skin photographs. To build a robust, balanced classifier, we implemented a structured dataset construction process:

1. **Exclusion Criteria**: Classes with fewer than 250 available images were excluded to ensure sufficient representative data for each category.
2. **Class Merging Strategy**: Similar clinical etiologies were grouped to form twelve distinct, clinically coherent classes. For example, Atopic Dermatitis and Eczema were kept as distinct classes to evaluate the model's ability to differentiate closely related inflammatory conditions. Common fungal infections were grouped into *Tinea, Ringworm, Candidiasis and other Fungal Infections*.
3. **Data Capping**: To prevent class imbalance from biasing the model, training images were capped at 2,000 per class, and validation/test images were capped at 500 per class.
4. **Duplicate Image Control**: Exact duplicate images were identified and removed by computing MD5 hashes for all files before performing the train/test split, ensuring zero data leakage.
5. **Licensing**: The source datasets are distributed under Public Domain and Creative Commons licenses, permitting academic and research use.

The final dataset contains 25,230 images split into an 80% training set (20,183 images) and a 20% validation/test set (5,047 images). The split was stratified by class using a fixed random seed of 42 to ensure reproducibility. Table I details the dataset distribution across the twelve classes.

### Table I. Dataset Distribution Across 12 Classes
| Class Name | Train | Test / Val | Total |
| :--- | :---: | :---: | :---: |
| Acne or Rosacea | 2,000 | 500 | 2,500 |
| Atopic Dermatitis | 1,008 | 252 | 1,260 |
| Basal Cell Carcinoma | 2,000 | 500 | 2,500 |
| Benign Keratosis-like Lesions | 1,664 | 416 | 2,080 |
| Eczema | 1,344 | 336 | 1,680 |
| Melanocytic Nevi | 2,000 | 500 | 2,500 |
| Melanoma | 2,000 | 500 | 2,500 |
| Normal | 2,000 | 500 | 2,500 |
| Psoriasis pictures Lichen Planus and related diseases | 1,644 | 411 | 2,055 |
| Seborrheic Keratoses and other Benign Tumors | 1,480 | 370 | 1,850 |
| Tinea Ringworm Candidiasis and other Fungal Infections | 1,364 | 341 | 1,705 |
| Warts Molluscum and other Viral Infections | 1,684 | 421 | 2,105 |
| **Total** | **20,183** | **5,047** | **25,230** |

```
Fig. 2. Distribution of DermAI Dataset
[See: fig2_dataset_distribution.png]
```

To preserve the shape, borders, and geometry of the lesions, we used the **Smart Padding (Letterboxing)** preprocessing technique. Naive resizing can stretch clinical anomalies and lead to misclassification. Smart Padding adds black pixels to the borders of the image to make it square while preserving its original aspect ratio, before resizing it to 224x224 pixels.

```
Fig. 1. Comparison of standard resizing and letterbox padding method
[See: fig1_resizing_comparison.png]
```

### C. Model Architecture and Training Strategy
We utilized **EfficientNet-B0** pre-trained on ImageNet. The final classification head was replaced with a linear layer mapping 1,280 features to 12 logits. The model contains approximately 4.01 million trainable parameters.

The **data augmentation pipeline** applied to the training set includes:
* Smart padding and resizing to 224x224.
* Random rotation up to ±45°.
* Color jitter (brightness=0.3, contrast=0.3).
* Random horizontal and vertical flips (p=0.5).
* Random erasing (p=0.5, scale=(0.02, 0.1), ratio=(0.3, 3.3)).

The model was trained for 20 epochs using the **AdamW optimizer** and the **OneCycleLR scheduler** with a base learning rate of 5e-4 and a maximum learning rate of 1e-3. The batch size was set to 32, and CrossEntropyLoss was used as the loss function. No class weights were applied, as class imbalance was already minimized through data capping. PyTorch's Automatic Mixed Precision (AMP) was enabled for memory efficiency. Checkpoints were saved at the end of each epoch, and the final model was selected based on the highest validation accuracy achieved during training.

### D. Clinical Decision-Support Component
The decision-support database is maintained as a JSON file, separating clinical suggestions from the inference code. For each of the twelve classes, the database provides a risk level, care recommendations, and safety warnings compiled from standard dermatological guidelines (such as the American Academy of Dermatology guidelines). 

For example, the entry for *Melanoma* is structured as follows:
```json
{
  "class": "Melanoma",
  "risk_level": "High",
  "care": "Protect the affected area from UV radiation. Do not scrub, scratch, or apply cosmetic acids to the lesion.",
  "recommendation": "Wear broad-spectrum SPF 50+ sunscreen, protective clothing, and avoid tanning beds.",
  "doctor_warning": "URGENT: This result indicates a high risk of a malignant lesion. Please consult a dermatologist or an oncology specialist immediately. Do not attempt self-treatment."
}
```
*Note*: These recommendations are intended for educational and decision-support purposes and were reviewed by the engineering co-authors. They do not constitute certified medical advice, and the system is not a certified medical device.

---

## IV. FINDINGS

### A. Model Performance and Evaluation
The model was evaluated on the stratified test set (n=5,047). The training configuration is summarized in Table II.

### Table II. Model Training Configuration
| Hyperparameter / Component | Value |
| :--- | :--- |
| Neural Network Backbone | EfficientNet-B0 |
| Total Trainable Parameters | ~4.01 Million |
| Optimizer | AdamW |
| Loss Function | CrossEntropyLoss |
| Learning Rate Scheduler | OneCycleLR (max. 1e-3, base 5e-4) |
| Batch Size / Epochs | 32 / 20 |
| Preprocessing | Aspect Ratio Preserving Smart Padding |
| Validation Procedure | Stratified hold-out validation (20% split) |
| Checkpoint Strategy | Highest validation accuracy |

The model achieved an overall **validation accuracy of 86.00%** and a **macro-averaged F1-score of 0.84**. Table III reports the class-specific precision, recall, and F1-score metrics.

### Table III. Class-Specific Metrics (Test Set, n=5,047)
| Disease Class | Precision | Recall | F1-Score | Support (n) |
| :--- | :---: | :---: | :---: | :---: |
| Acne or Rosacea | 0.86 | 0.95 | 0.90 | 500 |
| Atopic Dermatitis | 0.56 | 0.88 | 0.68 | 252 |
| Basal Cell Carcinoma | 0.85 | 0.94 | 0.89 | 500 |
| Benign Keratosis-like Lesions | 0.86 | 0.85 | 0.86 | 416 |
| Eczema | 0.85 | 0.81 | 0.83 | 336 |
| Melanocytic Nevi | 0.93 | 0.92 | 0.93 | 500 |
| Melanoma | 0.97 | 0.99 | 0.98 | 500 |
| Normal | 0.87 | 0.99 | 0.93 | 500 |
| Psoriasis pictures Lichen Planus and related diseases | 0.93 | 0.59 | 0.73 | 411 |
| Seborrheic Keratoses and other Benign Tumors | 0.92 | 0.76 | 0.83 | 370 |
| Tinea Ringworm Candidiasis and other Fungal Infections | 0.97 | 0.63 | 0.76 | 341 |
| Warts Molluscum and other Viral Infections | 0.76 | 0.84 | 0.80 | 421 |
| **Accuracy** | | | **0.86** | **5,047** |
| **Macro Average** | **0.86** | **0.84** | **0.84** | **5,047** |
| **Weighted Average** | **0.87** | **0.86** | **0.86** | **5,047** |

```
Fig. 4. Confusion Matrix — Rows show actual ground truth, columns show model predictions
[See: fig4_confusion_matrix.png]
```

```
Fig. 5. Distribution of F1 scores by disease class
[See: fig5_f1_scores_distribution.png]
```

```
Fig. 6. Comprehensive performance report of the test set (n=5,047)
[See: fig6_performance_report.png]
```

### B. Confusion Matrix Analysis
Analyzing the confusion matrix reveals key clinical and visual patterns:
1. **Atopic Dermatitis vs. Eczema**: The *Atopic Dermatitis* class exhibits the lowest precision (0.56) but a high recall (0.88), resulting in an F1-score of 0.68. There is a strong visual overlap between Atopic Dermatitis and Eczema. Specifically, the confusion matrix shows that 32 actual Eczema images were predicted as Atopic Dermatitis, and 11 actual Atopic Dermatitis images were predicted as Eczema. Clinically, atopic dermatitis is a major subtype of eczema, and their morphological features (erythema, scaling, and lichenification) are highly similar, causing label noise in public datasets.
2. **Psoriasis and Fungal Infections**: The *Psoriasis pictures Lichen Planus and related diseases* class has a recall of 0.59. It is frequently confused with Atopic Dermatitis (69 cases) and Warts/Viral Infections (47 cases). This confusion arises because psoriasis plaques can share visual characteristics with chronic eczema and viral lesions under non-standardized lighting conditions. Similarly, *Tinea and Fungal Infections* (recall 0.63) are frequently confused with Atopic Dermatitis (27 cases) and Acne/Rosacea (22 cases).
3. **Malignant Lesions**: *Melanoma* is classified with high accuracy (precision 0.97, recall 0.99, F1-score 0.98), and *Basal Cell Carcinoma* achieves an F1-score of 0.89. The high recall for Melanoma is critical, as it minimizes the risk of false negatives for malignant conditions.
4. **Normal Class**: Unlike the previous version of the system which evaluated only 19 normal tissue samples, the updated evaluation uses a test set of 500 Normal images. The model achieved a recall of 0.99 and an F1-score of 0.93 for healthy skin, indicating a strong capability to identify normal skin and reduce false positives.

---

## V. BASELINE COMPARISON

To evaluate the performance of EfficientNet-B0, we trained and tested three alternative architectures on the same 12-class dataset using identical training hyperparameters (AdamW, OneCycleLR, 20 epochs, batch size 32). Table IV compares the results.

### Table IV. Comparative Evaluation Against Baseline Architectures
| Model Architecture | Parameter Count | Val. Accuracy | Macro F1-Score | Inference Latency (CPU) |
| :--- | :---: | :---: | :---: | :---: |
| ResNet-50 | ~25.6 Million | 80.10% | 0.77 | ~420 ms |
| MobileNetV3-Large | ~5.4 Million | 82.40% | 0.79 | ~180 ms |
| DenseNet-121 | ~8.0 Million | 83.50% | 0.81 | ~350 ms |
| **EfficientNet-B0 (DermAI)**| **~4.01 Million**| **86.00%** | **0.84** | **~240 ms** |

EfficientNet-B0 achieved the highest validation accuracy (86.00%) and macro F1-score (0.84) while maintaining a low parameter footprint (~4.01 million parameters). Although MobileNetV3 is faster (180 ms), it exhibits lower classification accuracy. EfficientNet-B0 offers the most balanced trade-off between classification performance and CPU inference latency for local server deployment.

---

## VI. DISCUSSION AND SAFETY CONSIDERATIONS

### A. Safety and Misuse Risks
DermAI is a research prototype designed for preliminary screening and decision support. It is not certified for clinical diagnostics. The primary safety risk is **misclassification leading to false reassurance**. For example, if a malignant melanoma is misclassified as a benign melanocytic nevus or normal skin, the user might delay seeking professional care. Conversely, false positives can cause unnecessary patient anxiety.

To mitigate these risks:
1. The user interface includes prominent disclaimers stating that the system is not a diagnostic tool and that all results must be verified by a qualified physician.
2. The decision-support component is configured to flag high-risk categories (such as Melanoma and Basal Cell Carcinoma) with urgent consultation warnings, prioritizing patient safety over specificity.
3. No patient data or images are stored permanently on the server, ensuring privacy and compliance with data protection principles.

### B. Limitations and Future Work
Several limitations apply to the current prototype:
* **Image Quality**: Performance depends on image quality, lighting, and camera angle. Clinical photography differs from dermoscopy, and standard smartphone cameras introduce variations that can affect classification.
* **Skin Tone Bias**: The model was trained on public datasets that may underrepresent darker skin tones. Evaluative performance may vary across different Fitzpatrick skin types.
* **Clinical Validation**: The system has not undergone clinical trials. Future work will focus on validating the prototype using real-world clinical data and conducting a multi-center reader study comparing the model's classifications with evaluations from board-certified dermatologists.
* **Explainability**: Integrating explainable AI (XAI) techniques, such as Grad-CAM [20], would allow the system to highlight the visual regions that influenced the model's prediction, improving transparency for clinical users.

---

## VII. CONCLUSION

This paper presented DermAI, a three-tier preliminary skin disease classification and decision-support system prototype. The system combines an EfficientNet-B0 model fine-tuned on twelve disease classes, a FastAPI backend, and an Android client. By applying a data capping protocol and letterbox preprocessing, the model achieved a validation accuracy of 86.00% and a macro F1-score of 0.84 on a stratified test set of 5,047 images.

The prototype demonstrates the feasibility of combining lightweight CNN backbones with asynchronous web frameworks to provide fast, preliminary screening. Future work will focus on clinical validation, expanding demographic representation, and integrating explainability mechanisms to transition this prototype toward a clinical-grade decision-support tool.

---

## REFERENCES

[1] J. M. Hanifin, M. L. Reed, and Eczema Prevalence and Impact Working Group, "A population-based survey of eczema prevalence in the United States," *Dermatitis*, vol. 18, no. 2, pp. 82–91, Jun. 2007.

[2] T. E. Shaw, G. P. Currie, C. W. Koudelka, and E. L. Simpson, "Eczema prevalence in the United States: Data from the 2003 National Survey of Children's Health," *Journal of Investigative Dermatology*, vol. 131, no. 1, pp. 67–73, Jan. 2011.

[3] M. M. Ali, M. Raj, A. Farid, M. Y. Minhaj, P. Umang, and S. Patel, "Deep learning in dermatology: Convolutional neural network-based classification of skin diseases and cancer," in *Proc. 2nd Int. Conf. Microwave, Antenna and Communication (MAC)*, Oct. 2024, pp. 1–7.

[4] M. Chowdhury, S. Sultana, T. A. Ridi, A. F. Chowdhury, W. Alam, and F. Amin, "Elegant: A skincare assistant mobile application with beauty salon search, skin disorder check, and dermatologist booking features," in *Proc. Int. Conf. Computing and Communication Technologies (ICCCT)*, Apr. 2025, pp. 1–6.

[5] S. Mehta and A. Aneja, "Hybrid AI models for dermatology: Combining CNN and RF for skin lesion classification," in *Proc. 5th Int. Conf. Data Intelligence and Cognitive Informatics (ICDICI)*, Nov. 2024, pp. 431–436.

[6] Z. Wu et al., "Studies on different CNN algorithms for facial skin disease classification based on clinical images," *IEEE Access*, vol. 7, pp. 66505–66511, 2019.

[7] H. Nisar, Y. K. Ch'ng, and Y. K. Ho, "Automatic segmentation and classification of eczema skin lesions using supervised learning," in *Proc. IEEE Conf. Open Systems (ICOS)*, Nov. 2020, pp. 25–30.

[8] V. R. Balaji, S. T. Suganthi, R. Rajadevi, V. Krishna Kumar, B. Saravana Balaji, and S. Pandiyan, "Skin disease detection and segmentation using dynamic graph cut algorithm and classification through Naive Bayes classifier," *Measurement*, vol. 163, p. 107922, Oct. 2020.

[9] S. Park, A. L. Chien, B. Lin, and K. Li, "FACES: A deep-learning-based parametric model to improve rosacea diagnoses," *Applied Sciences*, vol. 13, no. 2, p. 970, Jan. 2023.

[10] R. K. M. S. Karunanayake et al., "CURETO: Skin diseases detection using image processing and CNN," in *Proc. 14th Int. Conf. Innovations in Information Technology (IIT)*, Nov. 2020, pp. 1–6.

[11] A. Esteva et al., "Dermatologist-level classification of skin cancer with deep neural networks," *Nature*, vol. 542, no. 7639, pp. 115–118, Feb. 2017.

[12] N. C. F. Codella et al., "Skin lesion analysis toward melanoma detection: A challenge at the 2017 International Symposium on Biomedical Imaging (ISBI)," *arXiv preprint arXiv:1710.05006*, 2017.

[13] M. Tan and Q. V. Le, "EfficientNet: Rethinking model scaling for convolutional neural networks," in *Proc. ICML*, 2019, pp. 6105–6114.

[14] A. Udrea et al., "Accuracy of a smartphone application for triage of skin lesions," *British Journal of Dermatology*, vol. 183, no. 3, pp. 548–553, 2020.

[15] V. Gulshan et al., "Development and validation of a deep learning algorithm for detection of diabetic retinopathy in retinal fundus photographs," *JAMA*, vol. 316, no. 22, pp. 2402–2410, Dec. 2016.

[16] P. Lakhani and B. Sundaram, "Deep learning at chest radiography: Automated classification of pulmonary tuberculosis by using convolutional neural networks," *Radiology*, vol. 284, no. 2, pp. 574–582, Aug. 2017.

[17] M. P. Wolke, "Skin Diseases CNN," Kaggle Code Repository, 2024. [Online]. Available: https://kaggle.com/code/mpwolke/skin-diseases-cnn

[18] I. Promus, "Skin diseases image dataset," Kaggle Dataset, 2024. [Online]. Available: https://www.kaggle.com/datasets/ismailpromus/skin-diseases-image-dataset

[19] A. Paszke et al., "PyTorch: An imperative style, high-performance deep learning library," in *Advances in Neural Information Processing Systems (NeurIPS)*, 2019, pp. 8024–8035.

[20] R. R. Selvaraju et al., "Grad-CAM: Visual explanations from deep networks via gradient-based localization," in *Proc. IEEE Int. Conf. Computer Vision (ICCV)*, 2017, pp. 618–626.
