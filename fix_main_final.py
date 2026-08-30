with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if "audioLevel = viewModel.voiceEngine.audioWaveformLevel.collectAsState().value," in line:
        continue
    if "localModelCatalog = viewModel.localModelCatalog.collectAsState().value," in line:
        line = line.replace(".collectAsState().value", "")
    new_lines.append(line)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.writelines(new_lines)

