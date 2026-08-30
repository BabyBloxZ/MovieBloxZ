# 🎬 MovieBloxZ — Xposed / LSPosed / LSPatch / NPatch Module


![Version](https://img.shields.io/badge/Version-0.0.1-blue.svg)
![Android](https://img.shields.io/badge/Android-7.0%2B-green.svg)
![Xposed](https://img.shields.io/badge/Xposed-Supported-orange.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)
[![Telegram](https://img.shields.io/badge/Telegram-Join%20Channel-26A5E4?logo=telegram)](https://t.me/BabyBloxZ01)

---

## 🔥 Overview

**MovieBloxZ** is an Xposed module that hooks into the MovieBox app to unlock VIP features.  
It uses class-existence detection, retry logic with fresh-install awareness, and a generic reflection-based fallback for cases where the target app's classes get obfuscated or renamed between updates.

**Supported packages:**
- `com.community.oneroom`
- `com.community.mbox.in`

---

## 📦 Installation

### 🟢 Root — LSPosed

**Requirements:**
- Rooted Android device
- LSPosed installed
- MovieBox app installed

**Steps:**
1. Install the module APK
2. Open LSPosed Manager
3. Enable the module
4. Set scope to `com.community.oneroom` or `com.community.mbox.in`
5. Reboot
6. Open the app — VIP unlocked

---

### 🟣 Non-Root — LSPatch

**Option A — Patch an already-installed app:**
1. Open **LSPatch**
2. Go to **Manage Apps**
3. Select the target app
4. Tap **Patch App**
5. Add the **MovieBloxZ** module
6. Patch → Install

**Option B — Patch an external APK (recommended):**
1. Download the original app APK
2. Open **LSPatch → Patch APK**
3. Select the APK
4. Add the module
5. Choose **Portable Mode**
6. Patch → Install the output APK

---

### 🔵 Non-Root — NPatch

1. Download the original app APK
2. Open **NPatch**
3. Select the APK
4. Add the **MovieBloxZ** module
5. Patch → Install

---

## ⚠️ Notes

- LSPatch/NPatch require re-patching after every app update
- Root/LSPosed method doesn't need re-patching after updates
- Some security or anti-tamper checks in the target app may detect the hook

---

## 👨‍💻 Author

- **Developer:** BabyBloxZ
- **Telegram:** [@BabyBloxZ01](https://t.me/BabyBloxZ01)

---

## 📜 License

This project is licensed under the **MIT License**.

---

## ⭐ Support

If this project is useful to you:
- 🌟 Star the repo
- 🐞 Report issues
- 📣 Share it with the community