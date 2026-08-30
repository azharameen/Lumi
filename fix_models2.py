import re

def fix_file(filepath, real_pkg):
    with open(filepath, "r") as f:
        content = f.read()
    
    lines = content.split('\n')
    out_lines = []
    
    in_bad_header = True
    for line in lines:
        if line.startswith("package com.example.domain"):
            in_bad_header = False
            out_lines.append(line)
            out_lines.append("import androidx.compose.runtime.Immutable")
            out_lines.append("import androidx.compose.runtime.Stable")
        elif not in_bad_header:
            out_lines.append(line)
            
    with open(filepath, "w") as f:
        f.write('\n'.join(out_lines))

fix_file("app/src/main/java/com/example/domain/model/PetModels.kt", "com.example.domain.model")
fix_file("app/src/main/java/com/example/domain/account/UserProfileModels.kt", "com.example.domain.account")

