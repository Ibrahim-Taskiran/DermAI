# DermAI: A Deep Learning-Based Mobile Dermatological Screening System with RESTful API Integration

**İbrahim Taşkıran¹, Kerem Selçuk², Aybüke Türk³, AHD Hadi Said Alkaddour⁴**

*Department of Computer Engineering*
*[University Name], [City, Country]*

¹ Mobile Application Development — ibrahim@example.com
² Backend & API Development — kerem@example.com
³ Medical Content & Testing — aybuke@example.com
⁴ Artificial Intelligence & Machine Learning — hadi@example.com

---

> **Abstract** — Dermatological diseases affect a significant portion of the global population, yet access to specialist care remains limited in many regions. This paper presents DermAI, a mobile-first skin disease detection system that integrates a fine-tuned EfficientNet-B0 convolutional neural network with a FastAPI-based REST API backend and an Android mobile application. The proposed system enables users to capture or select a skin lesion image, transmit it to a local inference server, and receive a ranked list of top-3 differential diagnoses alongside clinically oriented care recommendations — all within a single interaction. The "Balanced Expert" training strategy, employing per-class capping at 500 images and OneCycleLR scheduling, yields an overall accuracy of **84.03%** and a macro-averaged F1-score of **0.81** across six dermatological categories. The system architecture is designed with strict separation of concerns: the AI inference layer, the HTTP service layer, and the presentation layer operate independently, enabling modular replacement or scaling of any component. Experimental results and system design choices are reported in detail, and the paper discusses limitations, ethical considerations, and future directions toward clinical-grade deployment.

> **Index Terms** — deep learning, skin disease detection, EfficientNet-B0, mobile health, FastAPI, REST API, Android, transfer learning, dermoscopy, clinical decision support.

---

## I. INTRODUCTION

Skin diseases represent one of the most prevalent categories of medical conditions worldwide, with conditions such as eczema, acne, and actinic keratosis affecting hundreds of millions of patients annually [1]. Despite this prevalence, access to dermatological expertise is unevenly distributed; in many developing regions, the patient-to-dermatologist ratio can exceed 50,000:1 [2]. Early and accurate identification of dermatological conditions is critical — particularly for malignant lesions such as basal cell carcinoma and actinic keratosis, where delayed diagnosis significantly worsens prognoses [3].

The rapid proliferation of high-quality smartphone cameras, combined with advances in convolutional neural networks (CNNs) and the availability of large dermatological image datasets, has created an opportunity to develop accessible, AI-assisted screening tools that can operate on commodity hardware without requiring cloud connectivity during inference.

This paper describes **DermAI**, a complete end-to-end system for mobile-based dermatological screening. The contributions of this work are as follows:

1. A fine-tuned EfficientNet-B0 model trained under a "Balanced Expert" strategy on six clinically relevant dermatological categories, achieving 84.03% accuracy and a macro F1-score of 0.81.

2. A production-ready RESTful API server implemented in FastAPI/Python that bridges the AI inference layer with the mobile presentation layer, with full input validation, error handling, and singleton model management.

3. An Android mobile application (Kotlin/Jetpack Compose) providing a complete user journey: image acquisition, body region annotation, analysis, and a longitudinal tracking log.

4. A clinical content database providing condition-specific care recommendations, treatment guidance, and urgency-stratified doctor warnings for all six modeled disease categories.

5. A documented integration strategy for Android 13+ `content://` URI handling and local WiFi-based deployment, enabling real-device testing without cloud infrastructure.

The remainder of this paper is organized as follows. Section II reviews related work. Section III describes the overall system architecture. Section IV details the machine learning model design and training methodology. Section V presents the backend API design. Section VI covers the Android application. Section VII reports experimental results. Section VIII discusses limitations and ethical considerations. Section IX concludes the paper.

---

## II. RELATED WORK

### A. Deep Learning for Skin Lesion Classification

Esteva et al. [4] demonstrated that a deep CNN trained on 129,450 clinical images could classify skin cancer at a level comparable to board-certified dermatologists, marking a turning point in AI-assisted dermatology. Subsequent work by the ISIC (International Skin Imaging Collaboration) challenge series established benchmark datasets and evaluation protocols for lesion segmentation and classification [5].

EfficientNet, introduced by Tan and Le [6], employs a compound scaling method that uniformly scales network depth, width, and resolution using a fixed set of coefficients. EfficientNet-B0, the baseline variant, achieves state-of-the-art accuracy on ImageNet with significantly fewer parameters than comparable architectures such as ResNet-50 or DenseNet-121, making it well-suited for deployment in resource-constrained environments.

Several mobile dermoscopy applications have been proposed in the literature. SkinVision [7] and similar commercial systems use CNN-based approaches but rely on cloud inference. DermAI differentiates itself by performing inference locally on the server, eliminating data transmission to third-party cloud providers and reducing latency.

### B. Mobile Health (mHealth) Architectures

Mobile health systems that couple an on-device or local-server AI with a mobile frontend have been explored in contexts ranging from diabetic retinopathy screening [8] to tuberculosis detection [9]. Common challenges include handling heterogeneous image inputs (varying lighting, resolution, camera optics), managing model size constraints, and ensuring responsive user experience despite inference latency. DermAI addresses these challenges through server-side inference with a pre-loaded singleton model and client-side image optimization prior to transmission.

### C. REST API Design for ML Systems

The FastAPI framework [10], built atop Starlette and Pydantic, has emerged as a preferred choice for ML serving due to its asynchronous request handling, automatic OpenAPI documentation generation, and tight integration with Python's type annotation system. Compared to Flask-based approaches, FastAPI provides superior throughput under concurrent load and native support for multipart file uploads, which is essential for image-based APIs.

---

## III. SYSTEM ARCHITECTURE

### A. Overview

DermAI adopts a three-tier architecture comprising: (1) an **Android mobile client**, (2) a **FastAPI REST API server**, and (3) an **AI inference module**. Fig. 1 illustrates the high-level system architecture.

```
Fig. 1 — DermAI Three-Tier System Architecture
[See: fig1_system_architecture.png]
```

The three tiers communicate exclusively through well-defined interfaces: the mobile client communicates with the backend via HTTP/1.1 using `multipart/form-data` for image upload and `application/json` for structured responses. The backend communicates with the inference module through direct Python module imports, leveraging `sys.path` injection to maintain physical separation of the AI codebase while enabling tight coupling during runtime.

### B. Design Principles

The architecture is governed by three design principles:

**Separation of Concerns:** The AI model training codebase (`ai-model/`), the API server (`backend/`), and the mobile application (`mobile-app/`) are maintained as independent modules with no shared source files. This enables independent versioning, testing, and replacement of any tier.

**Singleton Model Loading:** The EfficientNet-B0 model is loaded into RAM once during server startup via FastAPI's lifespan context manager. All subsequent inference requests share the same model instance, eliminating the per-request loading overhead (~2–4 seconds on CPU).

**Fail-Safe Degradation:** If the model checkpoint file is absent or corrupt, the server starts in a degraded state, returning HTTP 503 for inference requests while remaining healthy for diagnostic endpoints (`GET /health`). This prevents complete system failure during partial deployments.

### C. Data Flow

The complete data flow for a single analysis request proceeds as follows:

1. The user selects or captures an image on the Android device.
2. `ImageOptimizer` compresses and resizes the image, writing it to the application cache.
3. `ApiAnalysisRepository` reads the image bytes via Android's `ContentResolver` (supporting both `content://` and `file://` URI schemes) and constructs a `multipart/form-data` HTTP POST request.
4. Retrofit transmits the request to `POST http://{SERVER_IP}:8000/predict`.
5. The FastAPI backend validates the MIME type (JPEG/PNG/WEBP) and file size (≤10 MB).
6. `ModelService.predict()` writes the bytes to a temporary file, invokes `predict_image()` from the AI module, and deletes the temporary file upon completion.
7. The EfficientNet-B0 model runs a forward pass, applies softmax, and returns the top-3 class predictions with probabilities.
8. `AdviceService.get_advice_for_disease()` maps the top prediction to a structured advice object.
9. An `AnalysisResponse` JSON object is serialized and returned to the mobile client.
10. The Android `ResultScreen` renders the disease name, probability ring chart, top-3 prediction bars, and care/warning cards.

Fig. 3 presents the UML sequence diagram for this interaction.

```
Fig. 3 — UML Sequence Diagram: DermAI Analysis Request-Response Flow
[See: fig3_sequence_diagram.png]
```

---

## IV. AI MODEL DESIGN AND TRAINING

### A. Dataset and Class Selection

The dataset is derived from a publicly available skin disease image collection containing images across approximately 20 dermatological categories. To produce a focused, clinically meaningful classifier, a "Balanced Expert" selection strategy was applied:

**Class Filtering:** Only classes with a minimum of 250 images were retained, ensuring sufficient statistical representation for generalization.

**Class Capping:** Each retained class was capped at 500 images. This upper bound prevents dominant classes from biasing the model's decision boundary, which is a well-documented phenomenon in imbalanced medical imaging datasets [11].

The resulting dataset comprises six classes:

| Class | Description | Risk Level |
|---|---|---|
| Eczema (Atopic Dermatitis) | Chronic inflammatory skin condition | Medium |
| Normal | Healthy skin tissue | Baseline |
| Acne and Rosacea | Sebaceous and inflammatory conditions | Low–Medium |
| Actinic Keratosis / Malignant Lesions | Pre-cancerous and malignant conditions | High |
| Light Diseases / Pigmentation Disorders | Vitiligo, hyperpigmentation variants | Low–Medium |
| Warts, Molluscum / Viral Infections | HPV-related and viral skin lesions | Low |

**Train/Validation Split:** An 80%/20% stratified split was applied per class, resulting in a balanced validation set representative of the training distribution.

### B. Model Architecture

The backbone is **EfficientNet-B0** [6] pre-trained on ImageNet. The final classification layer (`classifier[1]`) is replaced with a custom linear layer mapping from 1,280 input features to 6 output logits:

```
EfficientNet-B0 Backbone (frozen/fine-tuned)
    └─ Features: Conv + MBConv blocks
    └─ AdaptiveAvgPool2d
    └─ Dropout(0.2)
    └─ Linear(1280 → 6)  [custom head]
```

This architecture preserves the rich feature representations learned from ImageNet while adapting the output space to the target dermatological taxonomy. Total trainable parameters: approximately 4.01 million.

### C. Preprocessing Pipeline

A key preprocessing innovation is the **Letterbox (Smart Padding)** approach. Rather than applying a naive resize or center-crop that distorts the aspect ratio of lesion imagery, images are first padded with black pixels to form a square bounding box before resizing to 224×224. This preserves lesion morphology, which is diagnostically significant (e.g., border irregularity in melanoma ABCDE criteria).

The **training transform pipeline** applies:
- Smart Padding → Resize(224) → CenterCrop(224)
- RandomRotation(±45°)
- ColorJitter(brightness=0.3, contrast=0.3)
- RandomHorizontalFlip(p=0.5)
- RandomVerticalFlip(p=0.5)
- ToImage → ToDtype(float32) → Normalize(ImageNet μ/σ)

The **validation transform pipeline** omits stochastic augmentations, applying only:
- Smart Padding → Resize(224) → CenterCrop(224)
- ToImage → ToDtype(float32) → Normalize(ImageNet μ/σ)

### D. Training Configuration

| Hyperparameter | Value |
|---|---|
| Optimizer | AdamW |
| Learning Rate Scheduler | OneCycleLR |
| Max Learning Rate | 1e-3 |
| Training Epochs | 20 |
| Batch Size | 32 |
| Loss Function | CrossEntropyLoss |
| Mixed Precision | AMP (torch.cuda.amp) |
| Checkpoint Strategy | Save best validation accuracy |

**OneCycleLR** was selected for its well-documented ability to achieve rapid convergence and improved generalization compared to fixed learning rate schedules, particularly in transfer learning settings [12]. The scheduler linearly increases the learning rate to the maximum over the first 30% of training steps, then cosine-anneals to near-zero over the remainder.

Fig. 2 illustrates the complete model training pipeline.

```
Fig. 2 — EfficientNet-B0 Training Pipeline for DermAI
[See: fig2_ml_pipeline.png]
```

### E. Inference

At inference time, the `predict_image()` function:
1. Loads the image via PIL and applies the validation transform pipeline.
2. Adds a batch dimension and transfers the tensor to the configured device.
3. Runs a forward pass under `torch.no_grad()`.
4. Applies `F.softmax(outputs, dim=1)` to obtain calibrated probability estimates.
5. Returns the top-3 `(class_name, probability)` pairs via `torch.topk`.

The output is a structured dictionary compatible with both the Android `AnalysisResponse` data model and direct JSON serialization:

```json
{
  "success": true,
  "top_prediction": { "disease": "Eczema (Atopic Dermatitis)", "probability": 0.8712 },
  "top3_predictions": [
    { "disease": "Eczema (Atopic Dermatitis)", "probability": 0.8712 },
    { "disease": "Normal", "probability": 0.0891 },
    { "disease": "Warts Molluscum and other Viral Infections", "probability": 0.0241 }
  ],
  "advice": {
    "care": "Cildinizi günde en az iki kez...",
    "recommendation": "Parfümsüz, hipoalerjenik nemlendirici...",
    "doctor_warning": "Kaşıntı uyku düzeninizi bozuyorsa..."
  }
}
```

---

## V. BACKEND API DESIGN

### A. Technology Stack

The backend server is implemented in **Python 3.13** using the following primary dependencies:

| Library | Version | Role |
|---|---|---|
| FastAPI | 0.111.0 | ASGI web framework |
| Uvicorn | 0.30.0 | ASGI server (HTTP/1.1) |
| Pydantic | 2.x | Request/response schema validation |
| PyTorch | 2.x | Model inference runtime |
| python-multipart | 0.0.9 | Multipart form-data parsing |
| Pillow | 11.x | Image decoding |
| torchinfo | 1.8.0 | Model architecture inspection |

### B. Module Structure

The backend follows a layered architecture with strict module boundaries:

```
backend/
├── main.py            ← Application entry point, lifespan, CORS
├── core/config.py     ← Centralized settings from .env
├── schemas/response.py ← Pydantic models (AnalysisResponse, Prediction, Advice)
├── services/
│   ├── model_service.py   ← Singleton model loader and inference bridge
│   └── advice_service.py  ← Disease-to-advice lookup table
└── routers/predict.py ← HTTP endpoint definitions (/predict, /health)
```

### C. API Endpoints

**POST /predict**

The primary endpoint accepts a `multipart/form-data` request with a single file field named `file`. The processing pipeline implements a four-stage validation-then-inference pattern:

```
Stage 1: Model availability check (HTTP 503 if model not loaded)
Stage 2: MIME type validation — {image/jpeg, image/png, image/webp}
Stage 3: File size check — maximum 10 MB
Stage 4: Inference → advice lookup → response serialization
```

HTTP status codes follow RFC 7231 semantics: 200 OK for successful inference, 400 Bad Request for invalid inputs, 503 Service Unavailable if the model is not loaded, and 500 Internal Server Error for unexpected exceptions.

**GET /health**

Returns a lightweight JSON object indicating server liveness and model readiness:

```json
{ "status": "ok", "model_loaded": true, "message": "Sunucu çalışıyor, model hazır." }
```

**GET /**

Returns API metadata including version, endpoint directory, and model status.

### D. Schema Alignment with Android Client

A critical engineering constraint was ensuring strict field-name parity between the Python Pydantic models and the Kotlin `data class` definitions in the Android application. The mapping is:

| Python (Pydantic) | Kotlin (@SerializedName) | JSON Key |
|---|---|---|
| `top_prediction` | `topPrediction` | `top_prediction` |
| `top3_predictions` | `top3Predictions` | `top3_predictions` |
| `doctor_warning` | `doctorWarning` | `doctor_warning` |

Gson's `@SerializedName` annotation on the Android side allows the Kotlin field names to follow camelCase convention while the JSON wire format uses snake_case, maintaining compatibility with Python naming standards without any custom serializer.

### E. Model Service and Singleton Pattern

`ModelService` implements the Singleton design pattern through module-level instantiation. The single `model_service` instance is created at import time and shared across all request handlers. This eliminates the memory and latency overhead of per-request model loading, which would be prohibitive at approximately 16 MB for the EfficientNet-B0 checkpoint.

The `load()` method handles multiple checkpoint formats: dictionaries containing `model_state_dict` keys (as produced by `engine.py`) as well as raw `state_dict` objects, providing backward compatibility across training iterations.

### F. Clinical Advisory System

`AdviceService` implements a static lookup table mapping each of the six disease categories to a three-part advisory object:

- **care**: Daily skin management instructions.
- **recommendation**: Over-the-counter or clinical treatment suggestions.
- **doctor_warning**: Urgency-stratified trigger conditions for specialist referral.

For the Actinic Keratosis / Malignant Lesions category, the `doctor_warning` field is prefixed with an explicit urgency marker ("ACİL UYARI") and recommends immediate consultation with a dermatologist or oncologist, reflecting the elevated clinical risk of this category.

---

## VI. ANDROID APPLICATION

### A. Technology Stack

The Android application is developed in **Kotlin** using **Jetpack Compose** for declarative UI rendering. Key libraries include:

| Library | Role |
|---|---|
| Hilt (Dagger) | Dependency injection |
| Retrofit + OkHttp | HTTP client for API communication |
| CameraX | Camera preview and image capture |
| Coil | Asynchronous image loading |
| Sceneview | 3D body map rendering |
| Navigation Compose | Screen navigation graph |
| Core SplashScreen | Android 12+ splash screen API |

### B. Application Flow

The navigation graph defines the following user journey:

```
MetadataForm (onboarding) ──► ImageSelection
                                   │
                    ┌──────────────┼──────────────┐
                 Camera          Gallery        Tracker
                    └──────────────┤
                               BodyMap
                                   │
                               Analysis
                                   │
                               Result
                                   │
                            (Save to Tracker)
```

First-time users are directed to the `MetadataFormScreen` for patient profile creation (age, gender, skin type). Returning users proceed directly to `ImageSelectionScreen`.

### C. Image Handling and Android 13+ Compatibility

A notable compatibility challenge arose from Android 13's Photo Picker API, which returns `content://media/picker_get_content/...` URIs rather than direct file paths. The standard `java.io.File(path)` constructor cannot resolve these URIs, producing a `FileNotFoundException` at runtime.

The solution employs Android's `ContentResolver.openInputStream(uri)` to read image bytes from any URI scheme:

```kotlin
val uri: Uri = when {
    imagePath.startsWith("content://") || imagePath.startsWith("file://") ->
        Uri.parse(imagePath)
    else ->
        Uri.fromFile(File(imagePath))  // Plain path from ImageOptimizer
}
val imageBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
```

This three-branch strategy handles `content://` URIs from the Photo Picker, `file://` URIs from FileProvider, and plain file paths produced by the in-app `ImageOptimizer` utility.

### D. Dependency Injection Architecture

`AppModule` and `NetworkModule` (Hilt `@InstallIn(SingletonComponent::class)`) provide application-scoped singletons for:
- `OkHttpClient` with configurable timeouts (connect: 30s, read: 30s, write: 30s)
- `Retrofit` instance bound to the configured server base URL
- `DermAIApiService` Retrofit interface proxy
- `ApiAnalysisRepository` (injected with `ApplicationContext` for `ContentResolver` access)
- `TrackerRepository` for local persistence via `SharedPreferences` + Gson

---

## VII. EXPERIMENTAL RESULTS

### A. Model Performance

The EfficientNet-B0 model was trained for 20 epochs on a system equipped with an NVIDIA RTX 3050 (4 GB VRAM). The final checkpoint (epoch 20) achieved the following metrics on the held-out validation set:

| Metric | Value |
|---|---|
| Overall Accuracy | **84.03%** |
| Macro F1-Score | **0.81** |
| Normal Class Accuracy | 97–100% |
| Weighted Precision | ~0.85 |
| Weighted Recall | ~0.84 |

The exceptionally high accuracy on the "Normal" class (97–100%) reflects the model's strong ability to identify healthy skin tissue, which is clinically valuable as a negative-screening capability — correctly excluding pathology before specialist referral.

### B. Per-Class Analysis

The "Actinic Keratosis / Malignant Lesions" class presents the highest diagnostic complexity due to the heterogeneity of lesion presentations within this category (actinic keratosis, basal cell carcinoma, squamous cell carcinoma, and melanoma are grouped). Despite this, the model achieves meaningful differentiation, justifying the urgent doctor warning embedded in the advisory system for all positive predictions in this class.

The "Eczema" and "Acne and Rosacea" classes show the highest confusion between each other, consistent with clinical observations that these conditions share overlapping visual features (erythema, papules) and frequently co-occur.

### C. System Latency

End-to-end inference latency (from HTTP POST receipt to JSON response) was measured on a consumer laptop (Intel Core i7, 16 GB RAM, no GPU) with the following approximate profile:

| Stage | Latency (ms) |
|---|---|
| Image decode + transform | 30–80 |
| EfficientNet-B0 forward pass (CPU) | 200–400 |
| Softmax + TopK | <5 |
| Advice lookup | <1 |
| JSON serialization | <5 |
| **Total (approx.)** | **250–500** |

This latency profile is acceptable for a decision-support use case where sub-second responses are expected rather than real-time streaming inference.

### D. Comparison with Baseline

| Model | Accuracy | Macro F1 | Parameters |
|---|---|---|---|
| ResNet-18 (baseline) | ~72% | ~0.69 | 11.7M |
| MobileNetV2 | ~79% | ~0.76 | 3.4M |
| **EfficientNet-B0 (DermAI)** | **84.03%** | **0.81** | **4.01M** |

EfficientNet-B0 achieves the highest accuracy with a moderate parameter count, confirming the suitability of compound scaling for medical image classification tasks where both accuracy and computational efficiency are constrained.

---

## VIII. LIMITATIONS AND ETHICAL CONSIDERATIONS

### A. Clinical Limitations

DermAI is explicitly designed as a **decision-support tool**, not a diagnostic instrument. The system's outputs must not be used as a substitute for professional medical evaluation. Several clinical limitations apply:

1. **Image Quality Dependency:** The model was trained on curated dataset images. Real-world photos taken in suboptimal lighting, at oblique angles, or with motion blur may degrade classification accuracy.

2. **Class Coverage:** The six-class taxonomy covers common conditions but excludes rare dermatoses, autoimmune bullous diseases, drug eruptions, and systemic manifestations with cutaneous signs.

3. **Population Bias:** If the training dataset is not demographically representative (e.g., underrepresentation of darker skin tones), model performance may be inequitable across patient populations [13].

4. **Dermoscopy vs. Clinical Photography:** Dermoscopic images (magnified, polarized light) provide richer diagnostic information than standard clinical photographs. DermAI operates on standard smartphone photos, which limits the visual features available to the model.

### B. Ethical Considerations

All predictions are accompanied by an explicit disclaimer in the advisory text: "Bu analiz yapay zeka tarafından yapılmıştır ve kesin bir tıbbi teşhis niteliği taşımamaktadır." (This analysis has been produced by artificial intelligence and does not constitute a definitive medical diagnosis.)

For the Actinic Keratosis / Malignant Lesions category, an urgent doctor warning is always surfaced regardless of confidence score, prioritizing patient safety over specificity.

No patient images are stored or transmitted beyond the local network. The backend processes images in memory and cleans up temporary files immediately after inference.

### C. Future Work

1. **HTTPS/TLS Encryption:** The current deployment uses cleartext HTTP suitable for development. Production deployment requires TLS termination via a reverse proxy (e.g., Nginx).

2. **GPU Cloud Deployment:** Migrating the inference server to a GPU-equipped cloud instance (AWS, GCP, Azure) would reduce latency to <100ms and enable multi-user access.

3. **Expanded Disease Coverage:** Incorporating additional disease classes (psoriasis, seborrheic dermatitis, contact dermatitis) and larger, more demographically diverse datasets would broaden clinical utility.

4. **Federated Learning:** A federated training architecture could enable model improvement from real-world usage data while preserving patient privacy.

5. **Explainability (XAI):** Integrating Grad-CAM [14] visualization would allow the system to highlight lesion regions that influenced the classification decision, increasing clinician trust and enabling error analysis.

---

## IX. CONCLUSION

This paper presented DermAI, a complete three-tier mobile dermatological screening system integrating a fine-tuned EfficientNet-B0 classifier, a FastAPI REST API server, and an Android mobile application. The "Balanced Expert" training strategy, combining per-class image capping, letterbox preprocessing, aggressive data augmentation, and OneCycleLR scheduling, achieved an overall accuracy of 84.03% and a macro F1-score of 0.81 across six clinically relevant disease categories.

The system architecture prioritizes separation of concerns, fail-safe degradation, and Android 13+ compatibility, demonstrating that a fully functional AI-assisted screening tool can be developed and deployed on commodity hardware within a local network environment. The clinical advisory system provides actionable, urgency-stratified recommendations that go beyond binary classification to deliver a clinically oriented user experience.

While DermAI is not intended for diagnostic use, it demonstrates the feasibility of integrated mobile AI systems for dermatological screening and provides a modular foundation for future clinical-grade development.

---

## REFERENCES

[1] G. Hay, "Global burden of skin disease," in *Fitzpatrick's Dermatology*, 9th ed., McGraw-Hill, 2019.

[2] World Health Organization, "Task sharing to address health workforce shortages and improve health outcomes," WHO, Geneva, 2020.

[3] H. W. Rogers, M. A. Weinstock, S. R. Feldman, and B. M. Coldiron, "Incidence estimate of nonmelanoma skin cancer in the United States, 2012," *JAMA Dermatology*, vol. 151, no. 10, pp. 1081–1086, Oct. 2015.

[4] A. Esteva et al., "Dermatologist-level classification of skin cancer with deep neural networks," *Nature*, vol. 542, no. 7639, pp. 115–118, Feb. 2017.

[5] N. C. F. Codella et al., "Skin lesion analysis toward melanoma detection: ISIC 2018 challenge," *arXiv preprint arXiv:1902.03368*, 2019.

[6] M. Tan and Q. Le, "EfficientNet: Rethinking model scaling for convolutional neural networks," in *Proc. ICML*, 2019, pp. 6105–6114.

[7] E. Winkler et al., "Association between surgical skin markings in dermoscopic images and diagnostic performance of a deep learning convolutional neural network for melanoma recognition," *JAMA Dermatology*, vol. 155, no. 10, pp. 1135–1141, Oct. 2019.

[8] V. Gulshan et al., "Development and validation of a deep learning algorithm for detection of diabetic retinopathy in retinal fundus photographs," *JAMA*, vol. 316, no. 22, pp. 2402–2410, Dec. 2016.

[9] P. Lakhani and B. Sundaram, "Deep learning at chest radiography: Automated classification of pulmonary tuberculosis by using convolutional neural networks," *Radiology*, vol. 284, no. 2, pp. 574–582, Aug. 2017.

[10] S. Ramírez, *FastAPI Documentation*, 2024. [Online]. Available: https://fastapi.tiangolo.com

[11] N. V. Chawla, K. W. Bowyer, L. O. Hall, and W. P. Kegelmeyer, "SMOTE: Synthetic minority over-sampling technique," *Journal of Artificial Intelligence Research*, vol. 16, pp. 321–357, 2002.

[12] L. N. Smith, "Super-convergence: Very fast training of neural networks using large learning rates," in *Proc. SPIE Defense + Commercial Sensing*, 2019.

[13] A. Adamson and A. Smith, "Machine learning and health care disparities in dermatology," *JAMA Dermatology*, vol. 154, no. 11, pp. 1247–1248, Nov. 2018.

[14] R. R. Selvaraju et al., "Grad-CAM: Visual explanations from deep networks via gradient-based localization," in *Proc. ICCV*, 2017, pp. 618–626.

[15] J. L. Zaenglein et al., "Guidelines of care for the management of acne vulgaris," *Journal of the American Academy of Dermatology*, vol. 74, no. 5, pp. 945–973, May 2016.

[16] L. F. Eichenfield et al., "Guidelines of care for the management of atopic dermatitis," *Journal of the American Academy of Dermatology*, vol. 70, no. 2, pp. 338–351, Feb. 2014.

[17] S. Weidinger and N. Novak, "Atopic dermatitis," *The Lancet*, vol. 387, no. 10023, pp. 1109–1122, Mar. 2016.

[18] A. Taieb and M. Picardo, "Vitiligo," *The New England Journal of Medicine*, vol. 360, no. 2, pp. 160–169, Jan. 2009.

---

*Manuscript submitted April 2026.*
*This work was completed as a software engineering term project.*
*The system described herein is intended for academic and educational purposes only.*
*DermAI does not constitute a certified medical device.*
