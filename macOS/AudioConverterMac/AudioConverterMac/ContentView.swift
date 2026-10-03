import SwiftUI
import UniformTypeIdentifiers

struct ContentView: View {
    @StateObject private var vm = ConverterViewModel()

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Audio Converter").font(.largeTitle.bold())
            Text(vm.inputURL?.lastPathComponent ?? "Файл не выбран").lineLimit(1)

            HStack {
                Button("♫ ВЫБРАТЬ АУДИО / MIDI") { vm.pickInput() }
                if vm.isMIDI { Button("ВЫБРАТЬ SOUNDFONT (.SF2)") { vm.pickSoundFont() } }
            }

            if vm.isMIDI {
                Text(vm.soundFontURL?.lastPathComponent ?? "Для MIDI выберите SoundFont .sf2")
                    .foregroundStyle(.secondary)
            }

            HStack {
                Picker("Формат", selection: $vm.format) {
                    ForEach(vm.formats, id: \.self) { Text($0.uppercased()) }
                }.frame(width: 180)
                Picker("Битрейт", selection: $vm.bitrate) {
                    ForEach(vm.bitrates, id: \.self) { Text($0) }
                }.frame(width: 180)
                Toggle("Mono", isOn: $vm.mono)
            }

            Button("КОНВЕРТИРОВАТЬ") { vm.convert() }
                .buttonStyle(.borderedProminent)
                .disabled(vm.inputURL == nil || vm.working)

            if vm.working { ProgressView() }
            Text(vm.status).foregroundStyle(.secondary)
        }
        .padding(24).frame(width: 620)
    }
}
