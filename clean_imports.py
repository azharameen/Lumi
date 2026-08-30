import re

def clean_imports_in_file(filepath):
    with open(filepath, "r") as f:
        lines = f.readlines()
    
    new_lines = []
    seen = set()
    for line in lines:
        if line.startswith("import "):
            # We don't need to import these twice!
            if "LocalLlmModelSpec" in line or "ModelDownload" in line or "HardwareAccelerator" in line:
                if line in seen:
                    continue
                seen.add(line)
        new_lines.append(line)
        
    with open(filepath, "w") as f:
        f.writelines(new_lines)

clean_imports_in_file("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt")
clean_imports_in_file("app/src/main/java/com/example/ui/screens/account/LlmSettingsSection.kt")
