with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if "null /* viewModel removed */ = null /* viewModel removed */" in line:
        continue
    new_lines.append(line)

with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "w") as f:
    f.writelines(new_lines)
