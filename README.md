\# 🛡️ VoiceGuard Android



\### AI-Powered Real-Time Voice Cloning \& Spoof Detection



VoiceGuard is an Android application designed to detect \*\*AI-generated, synthetic, and voice-cloned audio\*\* using an \*\*AASIST-inspired anti-spoofing deep learning model\*\*.



The application performs inference \*\*directly on the Android device using ONNX Runtime\*\*, reducing the need for cloud services and helping keep audio processing local.



\---



\## 🚨 Problem



Voice cloning technology can generate highly realistic speech from a person's voice.



Attackers can potentially use cloned voices for:



\* 📞 Impersonation calls

\* 💰 Financial scams

\* 🔐 Social engineering

\* 👤 Identity impersonation

\* 🎭 Fake voice recordings



Traditional voice verification systems may identify \*\*who is speaking\*\*, but they do not necessarily determine whether the speech itself was \*\*genuine or AI-generated\*\*.



VoiceGuard focuses on detecting this difference.



\---



\## 💡 Solution



VoiceGuard analyzes an input speech sample and estimates whether it is:



\* 🟢 \*\*Real / Genuine\*\*

\* 🔴 \*\*Synthetic / AI-generated\*\*



The application uses an \*\*AASIST-inspired audio anti-spoofing neural network\*\* trained to identify artifacts and patterns associated with synthetic speech.



\### Basic Pipeline



```text

&#x20;       🎙️ Audio Input

&#x20;             │

&#x20;             ▼

&#x20;     Audio Preprocessing

&#x20;             │

&#x20;             ▼

&#x20;     16 kHz Mono Audio

&#x20;             │

&#x20;             ▼

&#x20;     3-Second Audio Segment

&#x20;             │

&#x20;             ▼

&#x20;      AASIST Model

&#x20;             │

&#x20;             ▼

&#x20;      ONNX Runtime

&#x20;             │

&#x20;             ▼

&#x20;     Spoof Probability

&#x20;             │

&#x20;       ┌─────┴─────┐

&#x20;       ▼           ▼

&#x20;     REAL       SYNTHETIC

```



\---



\## ✨ Features



\### 🎙️ Audio Analysis



Analyze recorded or supplied speech samples for potential voice spoofing.



\### 🤖 AI-Based Detection



Uses an AASIST-inspired neural network trained for audio anti-spoofing.



\### 📱 On-Device Inference



The trained model is converted to \*\*ONNX\*\* and executed locally using ONNX Runtime.



\### 🔒 Privacy-Friendly Architecture



Audio does not need to be uploaded to a remote AI server for model inference.



\### ⚡ Lightweight Deployment



The ONNX model is packaged inside the Android application.



\### 📊 Probability-Based Result



The application provides a synthetic/real probability to help indicate the model's confidence.



\---



\## 🧠 Model



VoiceGuard uses an \*\*AASIST-inspired architecture\*\*.



AASIST stands for:



> Audio Anti-Spoofing using Integrated Spectro-Temporal graph attention networks



The model is designed to learn discriminative patterns from speech that can help distinguish genuine speech from spoofed or synthetic speech.



\### Model Input



```text

Sample Rate : 16,000 Hz

Channels    : Mono

Duration    : \~3 seconds

Samples     : 48,000

```



\### Model Output



The model produces a prediction representing the likelihood of the input being synthetic.



Example:



```text

Real Probability      : 0.18

Synthetic Probability : 0.82



Result: SYNTHETIC

```



\---



\## 📱 Android Architecture



```text

┌──────────────────────────────┐

│        Android App           │

├──────────────────────────────┤

│                              │

│      User Audio Input        │

│              │               │

│              ▼               │

│     Audio Preprocessing      │

│              │               │

│              ▼               │

│       ONNX Model Input       │

│              │               │

│              ▼               │

│       ONNX Runtime           │

│              │               │

│              ▼               │

│     AASIST-Inspired Model    │

│              │               │

│              ▼               │

│    Synthetic Probability     │

│              │               │

│              ▼               │

│       Detection Result       │

│                              │

└──────────────────────────────┘

```



\---



\## 🛠️ Technologies Used



| Technology                   | Purpose                         |

| ---------------------------- | ------------------------------- |

| Kotlin                       | Android application development |

| Android Studio               | Development environment         |

| XML                          | Android UI                      |

| ONNX                         | Model deployment format         |

| ONNX Runtime                 | On-device model inference       |

| Python                       | Model training and conversion   |

| PyTorch                      | Deep learning model development |

| AASIST-inspired architecture | Audio anti-spoofing             |

| Git \& GitHub                 | Version control                 |



\---



\## 📂 Project Structure



```text

VoiceGuardAndroid/

│

├── app/

│   └── src/

│       └── main/

│           ├── java/

│           │   └── .../

│           │       └── MainActivity.kt

│           │

│           ├── res/

│           │   ├── layout/

│           │   ├── drawable/

│           │   └── values/

│           │

│           ├── assets/

│           │   └── aasist.onnx

│           │

│           └── AndroidManifest.xml

│

├── build.gradle

├── settings.gradle

└── README.md

```



\---



\## ⚙️ Requirements



\* Android Studio

\* Android device or emulator

\* Android device with microphone support

\* Kotlin

\* ONNX Runtime for Android



For actual audio testing, a \*\*physical Android device\*\* is recommended.



\---



\## 🚀 Installation



\### 1. Clone the repository



```bash

git clone https://github.com/SivananthamP/voiceguard.git

```



\### 2. Open the project



Open the cloned project in \*\*Android Studio\*\*.



\### 3. Build the project



Allow Gradle to download the required dependencies.



Then select:



```text

Build → Make Project

```



\### 4. Connect an Android device



Enable:



```text

Developer Options

USB Debugging

```



Then connect the device to your computer.



\### 5. Run



Click:



```text

▶ Run

```



Android Studio will install VoiceGuard on the connected device.



\---



\## 🔐 Permissions



VoiceGuard may require microphone permission for recording/analyzing audio.



```xml

<uses-permission android:name="android.permission.RECORD\_AUDIO"/>

```



The application should request this permission at runtime before accessing the microphone.



\---



\## 🧪 Detection Flow



When an audio sample is provided:



```text

1\. Capture audio

&#x20;      ↓

2\. Convert to mono

&#x20;      ↓

3\. Resample to 16 kHz

&#x20;      ↓

4\. Prepare approximately 3 seconds of audio

&#x20;      ↓

5\. Normalize/preprocess audio

&#x20;      ↓

6\. Pass tensor to ONNX model

&#x20;      ↓

7\. Run inference

&#x20;      ↓

8\. Obtain prediction

&#x20;      ↓

9\. Display result

```



\---



\## 📊 Example Result



```text

VOICEGUARD ANALYSIS



Synthetic Probability

&#x20;       82%



Prediction

&#x20;       ⚠️ SYNTHETIC



Confidence

&#x20;       HIGH

```



The probability is a \*\*model output\*\*, not a guarantee that an audio sample is malicious or genuinely cloned.



\---



\## 🔬 Training Pipeline



The model was developed using Python and PyTorch.



```text

Training Dataset

&#x20;      │

&#x20;      ▼

Audio Preprocessing

&#x20;      │

&#x20;      ▼

16 kHz Mono Audio

&#x20;      │

&#x20;      ▼

AASIST-Inspired Model

&#x20;      │

&#x20;      ▼

PyTorch Training

&#x20;      │

&#x20;      ▼

Trained Checkpoint

&#x20;      │

&#x20;      ▼

ONNX Conversion

&#x20;      │

&#x20;      ▼

Android Deployment

```



\---



\## 🔄 PyTorch → ONNX → Android



```text

PyTorch Model

&#x20;    │

&#x20;    ▼

.pth checkpoint

&#x20;    │

&#x20;    ▼

ONNX Export

&#x20;    │

&#x20;    ▼

aasist.onnx

&#x20;    │

&#x20;    ▼

Android assets/

&#x20;    │

&#x20;    ▼

ONNX Runtime

&#x20;    │

&#x20;    ▼

Mobile Inference

```



\---



\## 🎯 Project Goal



The goal of VoiceGuard is to provide a \*\*mobile-first AI-based defense layer against voice cloning and synthetic speech attacks\*\*.



Instead of depending entirely on cloud-based analysis, the project explores whether anti-spoofing inference can be performed directly on a smartphone.



\---



\## ⚠️ Limitations



VoiceGuard is a research/prototype system and should not be treated as a guaranteed fraud detector.



Performance can vary depending on:



\* Audio quality

\* Background noise

\* Recording device

\* Compression

\* Speaker characteristics

\* Unseen voice-cloning systems

\* Synthetic speech generation methods

\* Dataset differences



A model trained on particular spoofing methods may not detect every future voice-cloning technique.



\---



\## 🔮 Future Improvements



\* \[ ] Real-time streaming audio analysis

\* \[ ] Sliding-window inference

\* \[ ] Improved noise robustness

\* \[ ] Larger multilingual dataset

\* \[ ] More voice-cloning generators during training

\* \[ ] Speaker verification + spoof detection fusion

\* \[ ] Call-level detection research

\* \[ ] Hardware acceleration

\* \[ ] Improved model calibration

\* \[ ] Continuous live risk monitoring



\---



\## 🏆 Hackathon / SIH Context



VoiceGuard was developed as an \*\*AI-based solution for detecting voice-cloning impersonation attacks\*\*.



The project combines:



```text

Deep Learning

&#x20;     +

Audio Anti-Spoofing

&#x20;     +

ONNX Model Optimization

&#x20;     +

Android Deployment

&#x20;     +

On-Device AI

```



This enables the trained anti-spoofing model to move from a Python research environment into a practical mobile prototype.



\---



\## 👨‍💻 Development



Built with:



\*\*Python + PyTorch + AASIST-inspired architecture + ONNX + Kotlin + Android Studio\*\*



\---



\## 📄 License



This project is intended for educational, research, and prototype development purposes.



Add an appropriate open-source license such as MIT if you decide to release the source code publicly.



\---



\## ⭐ Acknowledgements



This project builds upon research in \*\*audio deepfake and anti-spoofing detection\*\*, particularly the ideas behind AASIST and graph-attention-based audio spoof detection.



\---



\### 🛡️ VoiceGuard



\*\*Detect the voice. Verify the authenticity.\*\*



