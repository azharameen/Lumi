with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("onCancelModelDownload = { id -> viewModel.cancelModelDownload(id) },", "onCancelModelDownload = { id -> viewModel.cancelModelDownload(id) },\n                        onPauseModelDownload = { id -> viewModel.pauseModelDownload(id) },")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)

