import Foundation

enum RunnerError: LocalizedError {
    case missing(String), failed(String)
    var errorDescription: String? {
        switch self {
        case .missing(let x): return "Не найден \(x). Установите: brew install ffmpeg fluid-synth"
        case .failed(let x): return x
        }
    }
}

enum ProcessRunner {
    static func locate(_ names:[String]) -> String? {
        let roots=["/opt/homebrew/bin","/usr/local/bin","/usr/bin"]
        for r in roots { for n in names {
            let p="\(r)/\(n)"; if FileManager.default.isExecutableFile(atPath:p) { return p }
        }}
        return nil
    }
    static func ffmpegPath() throws -> String { guard let p=locate(["ffmpeg"]) else { throw RunnerError.missing("FFmpeg") }; return p }
    static func fluidSynthPath() throws -> String { guard let p=locate(["fluidsynth"]) else { throw RunnerError.missing("FluidSynth") }; return p }

    @discardableResult
    static func run(executable:String, arguments:[String]) throws -> String {
        let p=Process(); let pipe=Pipe()
        p.executableURL=URL(fileURLWithPath:executable); p.arguments=arguments
        p.standardOutput=pipe; p.standardError=pipe
        try p.run(); p.waitUntilExit()
        let out=String(data:pipe.fileHandleForReading.readDataToEndOfFile(),encoding:.utf8) ?? ""
        if p.terminationStatus != 0 { throw RunnerError.failed(out.isEmpty ? "Команда завершилась с ошибкой" : out) }
        return out
    }
}
