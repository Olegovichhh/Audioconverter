# AudioConverter Android
Офлайн-конвертер и компрессор аудио для Android.

Поддержка: MP3, M4A/AAC, WAV, FLAC, OGG, OPUS; битрейт 64–320 kbps; mono; локальная обработка без сервера.

## APK
Откройте **Actions → Build Android APK → Artifacts → AudioConverter-debug-apk**.

## MIDI
MIDI содержит события нот, а не готовый звук. Поэтому MIDI→MP3 требует синтезатора и SoundFont (.sf2). В v1.0 обычное аудио уже конвертируется; MIDI renderer добавляется отдельным модулем, чтобы MIDI не обрабатывался некорректно как аудиопоток.

FFmpeg engine: dev.ffmpegkit-maintained:ffmpeg-kit-audio:8.1.7 (LGPL variant).

## Build status
GitHub Actions builds the debug APK on pushes to main using JDK 17 and Gradle 8.9.
