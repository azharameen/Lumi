def remove_dupe_imports(filepath):
    with open(filepath, "r") as f:
        lines = f.readlines()
    
    seen = set()
    new_lines = []
    for line in lines:
        if line.startswith("import "):
            if line in seen:
                continue
            seen.add(line)
        new_lines.append(line)
    
    with open(filepath, "w") as f:
        f.writelines(new_lines)

remove_dupe_imports("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt")
remove_dupe_imports("app/src/main/java/com/example/ui/screens/account/LlmSettingsSection.kt")
