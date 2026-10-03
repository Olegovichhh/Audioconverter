# AudioConverterMac

Native macOS SwiftUI version of Audio Converter.

## Requirements
- macOS 14+
- Xcode 16+
- Homebrew
- ffmpeg
- fluidsynth

Install engines:

```bash
brew install ffmpeg fluid-synth
```

Open `AudioConverterMac.xcodeproj`, select **My Mac**, then Run.

MIDI files are rendered by FluidSynth. Select an SF2 file in the app. Audio files are converted by FFmpeg.
