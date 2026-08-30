import os
import glob
import re

screens_path = "app/src/main/java/com/example/ui/screens/*.kt"

for filepath in glob.glob(screens_path):
    with open(filepath, "r") as f:
        content = f.read()

    # We want to replace LumiCyan and LumiViolet with MaterialTheme.colorScheme.primary
    # We should add import if missing: import androidx.compose.material3.MaterialTheme
    
    if "LumiCyan" in content or "LumiViolet" in content:
        content = re.sub(r'\bLumiCyan\b', 'androidx.compose.material3.MaterialTheme.colorScheme.primary', content)
        content = re.sub(r'\bLumiViolet\b', 'androidx.compose.material3.MaterialTheme.colorScheme.primary', content)
        
        with open(filepath, "w") as f:
            f.write(content)

