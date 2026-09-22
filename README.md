# 🛡️ VoiceGuard Android

### AI-Powered Voice Cloning & Synthetic Speech Detection

VoiceGuard is an Android application designed to detect **AI-generated, synthetic, and voice-cloned audio** using an **AASIST-inspired audio anti-spoofing deep learning model**.

The application performs model inference **directly on the Android device using ONNX Runtime**, reducing the need for cloud-based processing and enabling a mobile-first approach to voice authenticity analysis.

---

## 🚨 Problem

Voice cloning technology can generate highly realistic speech from a person's voice.

Such technology can potentially be misused for:

* 📞 Impersonation calls
* 💰 Financial scams
* 🔐 Social engineering
* 👤 Identity impersonation
* 🎭 Fake voice recordings

Traditional speaker verification focuses primarily on **who is speaking**. VoiceGuard focuses on an additional question:

> **Is the speech genuine, or was it generated or manipulated by an AI system?**

---

## 💡 Solution

VoiceGuard analyzes an input speech sample and estimates whether it is:

* 🟢 **Real / Genuine**
* 🔴 **Synthetic / AI-generated**

The application uses an **AASIST-inspired audio anti-spoofing neural network** trained to learn patterns associated with synthetic and spoofed speech.

### Basic Pipeline

```text
        🎙️ Audio Input
              │
              ▼
      Audio Preprocessing
              │
              ▼
       16 kHz Mono Audio
              │
              ▼
     ~3-Second Audio Segment
              │
              ▼
      AASIST-Inspired Model
              │
              ▼
        ONNX Runtime
              │
              ▼
      Spoof Probability
              │
        ┌─────┴─────┐
        ▼           ▼
      REAL       SYNTHETIC
```

---

## ✨ Features

### 🎙️ Audio Analysis

Analyze speech audio for potential synthetic or spoofed speech characteristics.

### 🤖 AI-Based Detection

Uses an AASIST-inspired neural network for audio anti-spoofing.

### 📱 On-Device Inference

The trained model is converted to **ONNX** and executed locally using ONNX Runtime.

### 🔒 Privacy-Friendly Processing

The model can perform inference locally without requiring audio to be uploaded to a remote AI server.

### ⚡ Lightweight Deployment

The ONNX model is packaged directly inside the Android application.

### 📊 Probability-Based Output

The application provides a model probability indicating how strongly the input is classified toward synthetic speech.

---

## 🧠 AI Model

VoiceGuard uses an **AASIST-inspired architecture** for audio anti-spoofing.

AASIST refers to:

> Audio Anti-Spoofing using Integrated Spectro-Temporal graph attention networks

The approach is designed to learn discriminative patterns from speech that can help distinguish genuine speech from spoofed or synthetic speech.

### Model Input

```text
Sample Rate : 16,000 Hz
Channels    : Mono
Duration    : Approximately 3 seconds
Samples     : 48,000
```

### Model Output

The model produces a prediction representing the likelihood that the input audio is synthetic.

Example:

```text
Real Probability      : 0.18
Synthetic Probability : 0.82

Result: SYNTHETIC
```

The displayed probability is a **model prediction**, not a guarantee that an audio sample is malicious or cloned.

---

## 📱 Android Architecture

```text
┌──────────────────────────────┐
│        Android App           │
├──────────────────────────────┤
│                              │
│       Audio Input            │
│            │                 │
│            ▼                 │
│   Audio Preprocessing        │
│            │                 │
│            ▼                 │
│      Model Input             │
│            │                 │
│            ▼                 │
│      ONNX Runtime            │
│            │                 │
│            ▼                 │
│  AASIST-Inspired Model       │
│            │                 │
│            ▼                 │
│ Synthetic Probability        │
│            │                 │
│            ▼                 │
│     Detection Result         │
│                              │
└──────────────────────────────┘
```

---

## 🛠️ Technologies Used

| Technology                   | Purpose                         |
| ---------------------------- | ------------------------------- |
| Kotlin                       | Android application development |
| Android Studio               | Development environment         |
| XML                          | Android user interface          |
| ONNX                         | Model deployment format         |
| ONNX Runtime                 | On-device model inference       |
| Python                       | Model training and conversion   |
| PyTorch                      | Deep learning model development |
| AASIST-inspired architecture | Audio anti-spoofing             |
| Git & GitHub                 | Version control                 |

---

## 📂 Project Structure

```text
voiceguard/
│
├── app/
│   ├── build.gradle
│   │
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           │
│           ├── assets/
│           │   └── aasistmodel.onnx
│           │
│           ├── java/
│           │   └── com/
│           │       └── voiceguard/
│           │           └── app/
│           │               ├── MainActivity.kt
│           │               └── convert_embedded_onnx.py
│           │
│           └── res/
│               ├── layout/
│               │   └── activity_main.xml
│               │
│               └── values/
│                   ├── strings.xml
│                   └── themes.xml
│
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
│
├── .gitignore
├── build.gradle
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle
└── README.md
```

---

## ⚙️ Requirements

* Android Studio
* Android SDK
* Kotlin
* Android device or emulator
* Microphone-enabled Android device
* ONNX Runtime for Android

For actual audio testing, a **physical Android device** is recommended.

---

## 🚀 Installation

### 1. Clone the repository

```bash
git clone https://github.com/SivananthamP/voiceguard.git
```

### 2. Open the project

Open the cloned `voiceguard` folder in **Android Studio**.

Allow Android Studio to synchronize the Gradle project and download the required dependencies.

### 3. Build the project

From Android Studio:

```text
Build → Make Project
```

### 4. Connect an Android device

Enable:

```text
Developer Options
USB Debugging
```

Connect the Android device to your computer.

### 5. Run the application

Click:

```text
▶ Run
```

Android Studio will build and install VoiceGuard on the connected device.

---

## 🔐 Permissions

VoiceGuard requires microphone access when audio is captured from the device.

The application uses:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
```

Microphone access should be granted by the user when requested by Android.

---

## 🧪 Detection Flow

When an audio sample is provided:

```text
1. Capture audio
       ↓
2. Convert to mono
       ↓
3. Resample to 16 kHz
       ↓
4. Prepare approximately 3 seconds of audio
       ↓
5. Normalize / preprocess audio
       ↓
6. Prepare model tensor
       ↓
7. Run ONNX inference
       ↓
8. Obtain prediction
       ↓
9. Display detection result
```

---

## 📊 Example Result

```text
╔══════════════════════════════╗
║     VOICEGUARD ANALYSIS      ║
╠══════════════════════════════╣
║                              ║
║ Synthetic Probability: 82%   ║
║                              ║
║ Prediction: SYNTHETIC        ║
║                              ║
╚══════════════════════════════╝
```

The probability represents the model's output and should not be interpreted as absolute proof that an audio recording is AI-generated.

---

## 🔬 Training Pipeline

The model development process uses Python and PyTorch before deployment to Android.

```text
Training Dataset
       │
       ▼
Audio Preprocessing
       │
       ▼
16 kHz Mono Audio
       │
       ▼
AASIST-Inspired Model
       │
       ▼
PyTorch Training
       │
       ▼
Trained Checkpoint
       │
       ▼
ONNX Conversion
       │
       ▼
Android Deployment
```

---

## 🔄 PyTorch → ONNX → Android

```text
PyTorch Model
      │
      ▼
.pth Checkpoint
      │
      ▼
ONNX Export
      │
      ▼
aasistmodel.onnx
      │
      ▼
Android assets/
      │
      ▼
ONNX Runtime
      │
      ▼
Mobile Inference
```

---

## 🎯 Project Goal

The goal of VoiceGuard is to explore a **mobile-first AI-based defense layer against voice cloning and synthetic speech attacks**.

Instead of depending entirely on cloud-based analysis, the project demonstrates the deployment of an audio anti-spoofing model directly onto an Android device.

---

## ⚠️ Limitations

VoiceGuard is a **research and prototype system** and should not be treated as a guaranteed fraud or deepfake detector.

Performance can vary depending on:

* Audio quality
* Background noise
* Recording device
* Audio compression
* Speaker characteristics
* Unseen voice-cloning systems
* Synthetic speech generation methods
* Dataset differences

A model trained on particular spoofing methods may not detect every future voice-cloning technique.

---

## 🔮 Future Improvements

* [ ] Real-time streaming audio analysis
* [ ] Sliding-window inference
* [ ] Improved noise robustness
* [ ] Larger multilingual dataset
* [ ] More voice-cloning generators during training
* [ ] Speaker verification + spoof detection fusion
* [ ] Call-level detection research
* [ ] Hardware acceleration
* [ ] Improved model calibration
* [ ] Continuous live risk monitoring

---

## 🏆 Hackathon / SIH Context

VoiceGuard was developed as an **AI-based solution for detecting voice-cloning impersonation attacks**.

The project combines:

```text
Deep Learning
      +
Audio Anti-Spoofing
      +
ONNX Model Deployment
      +
Android Development
      +
On-Device AI
```

This allows an audio anti-spoofing model developed in a Python/PyTorch environment to be deployed as a mobile Android prototype.

---

## 👨‍💻 Development

Built with:

**Python + PyTorch + AASIST-inspired architecture + ONNX + Kotlin + Android Studio**

---

## 📄 License

This project is intended for **educational, research, and prototype development purposes**.

If the project is released as open source, an appropriate license can be added to the repository.

---

## ⭐ Acknowledgements

This project builds upon research in **audio deepfake and anti-spoofing detection**, particularly approaches related to AASIST and graph-attention-based audio spoof detection.

---

## 🛡️ VoiceGuard

**Detect the voice. Verify the authenticity.**
