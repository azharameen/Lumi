with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("com.example.ui.screens.WardrobeScreen(viewModel = viewModel, onClose = { viewModel.setShowWardrobeScreen(false) })", "com.example.ui.screens.WardrobeScreen(petViewModel = petViewModel, wellnessViewModel = wellnessViewModel, onClose = { viewModel.setShowWardrobeScreen(false) })")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
