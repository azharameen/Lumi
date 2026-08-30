import os
import glob
import re

model_files = [
    "app/src/main/java/com/example/domain/model/PetModels.kt",
    "app/src/main/java/com/example/domain/account/UserProfileModels.kt"
]

for filepath in model_files:
    if os.path.exists(filepath):
        with open(filepath, "r") as f:
            content = f.read()
        
        if "androidx.compose.runtime.Immutable" not in content:
            content = content.replace("package ", "package " + filepath.split("/")[-2] + "\n\nimport androidx.compose.runtime.Immutable\nimport androidx.compose.runtime.Stable\n\npackage ", 1)
            # Actually better to just replace the first import
            content = re.sub(r'^(package .*?\n)', r'\1\nimport androidx.compose.runtime.Immutable\nimport androidx.compose.runtime.Stable\n', content, count=1)
            
            # Add @Immutable to data classes
            content = re.sub(r'(data class )', r'@Immutable\n\1', content)
            
            with open(filepath, "w") as f:
                f.write(content)
