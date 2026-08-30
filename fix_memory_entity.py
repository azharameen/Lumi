import re

with open("app/src/main/java/com/example/ui/screens/WellnessScreen.kt", "r") as f:
    content = f.read()

content = content.replace("MemoryEntity", "PetMemoryEntity")
content = content.replace("memory.memoryText", "memory.memoryText")

with open("app/src/main/java/com/example/ui/screens/WellnessScreen.kt", "w") as f:
    f.write(content)


with open("app/src/main/java/com/example/ui/viewmodel/WellnessViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("MemoryEntity", "PetMemoryEntity")

with open("app/src/main/java/com/example/ui/viewmodel/WellnessViewModel.kt", "w") as f:
    f.write(content)
