# TextToSpeech_AndroidFramework

## 1. Introduction

This project is an **Android Text-to-Speech (TTS) application** that converts text (typed by the user or extracted from PDF files) into natural-sounding speech. It also supports **translation into multiple languages** before playback, making it useful for accessibility, learning, and productivity.

**Problem it solves:** Helps users read and listen to text/PDFs hands-free, especially useful for visually impaired users or language learners.
**Usefulness:** Converts text to audio, supports translation, and enhances accessibility.
**Necessity:** Reduces manual reading effort and enables listening to documents in different languages.

**Technologies Used:**

* Java (Android Studio)
* Android TextToSpeech API
* LibreTranslate API (for translations)
* PDFBox-Android (for extracting text from PDF)
* Gradle (for dependency management)

---

## 2. Features

1. Convert text input into natural-sounding speech.
2. Extract and read text from **PDF files**.
3. **Translate text** into multiple languages before playback.
4. Pause and ▶️ Resume speech playback.
5. Can listen to the saved audios.
6. Simple, lightweight, and mobile-friendly design.

---

## 3. How to Use

1. **Install the App** on your Android device (via APK or Android Studio build).
2. **Enter text manually** or **select a PDF file** to load content.
3. Choose your **preferred language** (e.g., English, Hindi, Kannada).
4. Tap **Play** to listen, **Pause/Resume** as needed.
5. (Optional) Enable translation before playback to hear the text in a different language.

---

## 4. Project Structure

```
TextToSpeechApp/
|-- app/                     # Main Android app code
|   |-- java/com/example/... # Activities & logic
|   |-- res/                 # UI layouts, strings, icons
|-- build.gradle              # App-level Gradle config
|-- settings.gradle           # Project settings
|-- README.md                 # Project documentation
```

---

## 5. Installation / Run

* Clone the repository:

  ```bash
  git clone https://github.com/shreyarnayak/TextToSpeechApp.git
  ```
* Open in **Android Studio**.
* Sync Gradle dependencies.
* Build & run on emulator or Android device.
* Or install the APK directly on your phone.

---

## 6. Results

 Successfully converts text and PDFs to speech.
 Supports translation for multilingual playback.
 Smooth pause & resume feature for better control.

 <img width="373" height="673" alt="Screenshot 2025-08-22 190207" src="https://github.com/user-attachments/assets/ed385225-248a-4143-b69d-158fbdb3464c" />
<img width="351" height="687" alt="Screenshot 2025-08-22 190339" src="https://github.com/user-attachments/assets/145c8d37-bc49-45ae-bc0b-05a894064844" />
<img width="388" height="686" alt="Screenshot 2025-08-22 190529" src="https://github.com/user-attachments/assets/bac7a73d-2d1a-4bd3-bff2-a2d1236d38f7" />


---

## 7. Conclusion / Future Scope

Built a functional Android Text-to-Speech app with translation & PDF reading support.
Can be extended for accessibility tools, e-learning apps, or audiobook generation.

**Future Improvements:**
* Add support for DOCX and TXT files.
* Improve UI with dark mode & voice customization.
* Cloud storage integration for saving & retrieving documents.

