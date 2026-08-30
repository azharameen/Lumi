import re
with open("app/src/main/java/com/example/ui/screens/AmbientSoundscapesScreen.kt", "r") as f:
    content = f.read()

content = content.replace("onStartSoundscape: (com.example.service.SoundscapeType) -> Unit,", "onAction: (com.example.ui.viewmodel.LumiUiAction) -> Unit,")
content = re.sub(r'onStopSoundscape: \(\) -> Unit,\n', '', content)
content = re.sub(r'onSetVolume: \(Float\) -> Unit,\n', '', content)
content = re.sub(r'onStartFocusTimer: \(Int\) -> Unit,\n', '', content)
content = re.sub(r'onStopFocusTimer: \(\) -> Unit\n', '', content)

content = content.replace("onStartSoundscape(it)", "onAction(com.example.ui.viewmodel.LumiUiAction.StartSoundscape(it))")
content = content.replace("onStopSoundscape()", "onAction(com.example.ui.viewmodel.LumiUiAction.StopSoundscape)")
content = content.replace("onSetVolume(it)", "onAction(com.example.ui.viewmodel.LumiUiAction.SetSoundscapeVolume(it))")
content = content.replace("onStartFocusTimer(m)", "onAction(com.example.ui.viewmodel.LumiUiAction.StartFocusTimer(m))")
content = content.replace("onStopFocusTimer()", "onAction(com.example.ui.viewmodel.LumiUiAction.StopFocusTimer)")

with open("app/src/main/java/com/example/ui/screens/AmbientSoundscapesScreen.kt", "w") as f:
    f.write(content)
