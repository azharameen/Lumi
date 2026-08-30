import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Fix the incorrect imports placement
if content.startswith("import "):
    # extract the package declaration and swap
    content = re.sub(r'^(import .*?\nimport .*?\n)(package .*?\n)', r'\2\1', content, count=1)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
