import os
import glob
import re

# Some data classes or other private stuff might not have been extracted.
# AiModelInfo was probably defined in UserAccountScreen.kt but outside a function, or inside the file.
with open("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt", "r") as f:
    content = f.read()

print("Is AiModelInfo in UserAccountScreen?", "AiModelInfo" in content)
