import SwiftUI
import AppKit

@MainActor
final class ConverterViewModel: ObservableObject {
    @Published var inputURL: URL?
    @Published var soundFontURL: URL?
    @Published var format = "mp3"
    @Published var bitrate = "192k"
    @Published var mono = false
    @Published var status = "Готово к работе"
    @Published var working = false

    let formats = ["mp3","m4a","aac","wav","flac","ogg","opus"]
    let bitrates = ["64k","96k","128k","192k","256k","320k"]
    var isMIDI: Bool {
        guard let e=inputURL?.pathExtension.lowercased() else { return false }
        return e == "mid" || e == "midi"
    }

    func pickInput() {
        let p=NSOpenPanel(); p.canChooseDirectories=false; p.allowsMultipleSelection=false
        if p.runModal() == .OK { inputURL=p.url; status="Файл выбран" }
    }

    func pickSoundFont() {
        let p=NSOpenPanel(); p.allowedContentTypes=[UTType(filenameExtension:"sf2") ?? .data]
        if p.runModal() == .OK { soundFontURL=p.url; status="SoundFont выбран" }
    }

    func convert() {
        guard let input=inputURL else { return }
        if isMIDI && soundFontURL == nil { status="Выберите SoundFont .sf2"; return }

        let save=NSSavePanel()
        save.nameFieldStringValue=input.deletingPathExtension().lastPathComponent+"."+format
        guard save.runModal() == .OK, let output=save.url else { return }

        working=true; status=isMIDI ? "Рендер MIDI…" : "Конвертация…"
        let sf=soundFontURL, fmt=format, br=bitrate, makeMono=mono

        Task.detached {
            do {
                let source: URL
                var temp: URL?
                if ["mid","midi"].contains(input.pathExtension.lowercased()) {
                    let wav=FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString+".wav")
                    try ProcessRunner.run(executable: ProcessRunner.fluidSynthPath(),
                        arguments:["-ni", sf!.path, input.path, "-F", wav.path, "-r", "44100"])
                    source=wav; temp=wav
                    await MainActor.run { self.status="MIDI отрендерен. Кодирование…" }
                } else { source=input }

                var args=["-y","-i",source.path,"-vn"]
                switch fmt {
                case "mp3": args += ["-c:a","libmp3lame"]
                case "m4a","aac": args += ["-c:a","aac"]
                case "flac": args += ["-c:a","flac"]
                case "ogg": args += ["-c:a","libvorbis"]
                case "opus": args += ["-c:a","libopus"]
                case "wav": args += ["-c:a","pcm_s16le"]
                default: break
                }
                if !["wav","flac"].contains(fmt) { args += ["-b:a",br] }
                if makeMono { args += ["-ac","1"] }
                args.append(output.path)
                try ProcessRunner.run(executable: ProcessRunner.ffmpegPath(), arguments:args)
                if let temp { try? FileManager.default.removeItem(at: temp) }
                await MainActor.run { self.working=false; self.status="Готово: "+output.lastPathComponent }
            } catch {
                await MainActor.run { self.working=false; self.status="Ошибка: "+error.localizedDescription }
            }
        }
    }
}
