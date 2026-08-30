with open("app/src/main/java/com/example/ui/screens/AmbientSoundscapesScreen.kt", "r") as f:
    content = f.read()

content = content.replace("onStartSoundscape(soundState.activeType)", "onAction(com.example.ui.viewmodel.LumiUiAction.StartSoundscape(soundState.activeType))")
content = content.replace("onStartFocusTimer(minutes)", "onAction(com.example.ui.viewmodel.LumiUiAction.StartFocusTimer(minutes))")
content = content.replace("onStartSoundscape(type)", "onAction(com.example.ui.viewmodel.LumiUiAction.StartSoundscape(type))")
content = content.replace("onAction(com.example.ui.viewmodel.LumiUiAction.SetSoundscapeVolume(it))) },", "onAction(com.example.ui.viewmodel.LumiUiAction.SetSoundscapeVolume(it)) },")

with open("app/src/main/java/com/example/ui/screens/AmbientSoundscapesScreen.kt", "w") as f:
    f.write(content)

