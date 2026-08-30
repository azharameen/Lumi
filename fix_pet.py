import re

with open("app/src/main/java/com/example/ui/pet/LumiPetView.kt", "r") as f:
    content = f.read()

# I will replace the detectDragGestures block with empty space or just delete it.
# The block is inside `.pointerInput(Unit) { detectDragGestures( ... ) }`
pattern = r"\.pointerInput\(Unit\)\s*\{\s*detectDragGestures\([\s\S]*?onDragEnd\s*=\s*\{[\s\S]*?\}\s*\)\s*\}"
content = re.sub(pattern, "", content)

with open("app/src/main/java/com/example/ui/pet/LumiPetView.kt", "w") as f:
    f.write(content)
