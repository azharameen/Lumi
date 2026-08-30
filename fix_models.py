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
        
        # Strip all incorrect package headers and re-add them correctly
        content = re.sub(r'package model\nimport.*?\npackage com\.example\.domain\.model', 'package com.example.domain.model\n\nimport androidx.compose.runtime.Immutable\nimport androidx.compose.runtime.Stable', content, flags=re.DOTALL)
        content = re.sub(r'package account\nimport.*?\npackage com\.example\.domain\.account', 'package com.example.domain.account\n\nimport androidx.compose.runtime.Immutable\nimport androidx.compose.runtime.Stable', content, flags=re.DOTALL)
        
        with open(filepath, "w") as f:
            f.write(content)
