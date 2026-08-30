import re
import os

with open("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt", "r") as f:
    source = f.read()

# Make sure directory exists
os.makedirs("app/src/main/java/com/example/ui/screens/account", exist_ok=True)
os.makedirs("app/src/main/java/com/example/ui/components", exist_ok=True)

# Function to extract a component by its name
def extract_function(name):
    pattern = r'(@Composable\s+)?(private\s+)?fun\s+' + name + r'\s*\('
    match = re.search(pattern, source)
    if not match:
        return None
    start_idx = match.start()
    
    # We need to find the matching closing brace
    brace_count = 0
    in_func = False
    end_idx = -1
    for i in range(start_idx, len(source)):
        if source[i] == '{':
            brace_count += 1
            in_func = True
        elif source[i] == '}':
            brace_count -= 1
            if in_func and brace_count == 0:
                end_idx = i + 1
                break
                
    if end_idx != -1:
        return source[start_idx:end_idx]
    return None

def write_component(name, package_name, target_dir):
    func_code = extract_function(name)
    if not func_code:
        print(f"Could not find {name}")
        return False
        
    # Remove "private " from the function signature
    func_code = re.sub(r'private\s+fun\s+' + name, r'fun ' + name, func_code)
    
    # Ensure it has @Composable
    if not func_code.strip().startswith('@Composable'):
        func_code = '@Composable\n' + func_code.strip()
        
    # Get all imports from the original file
    imports = re.findall(r'^import\s+.*$', source, flags=re.MULTILINE)
    imports_str = '\n'.join(imports)
    
    file_content = f"package {package_name}\n\n{imports_str}\n\n{func_code}\n"
    
    with open(f"{target_dir}/{name}.kt", "w") as f:
        f.write(file_content)
        
    return True

components = [
    ('ProfileAndPersonaSection', 'com.example.ui.screens.account', 'app/src/main/java/com/example/ui/screens/account'),
    ('MemoryFeederSection', 'com.example.ui.screens.account', 'app/src/main/java/com/example/ui/screens/account'),
    ('ConnectorsControlSection', 'com.example.ui.screens.account', 'app/src/main/java/com/example/ui/screens/account'),
    ('LlmSettingsSection', 'com.example.ui.screens.account', 'app/src/main/java/com/example/ui/screens/account'),
    ('PrivacyAndVaultSection', 'com.example.ui.screens.account', 'app/src/main/java/com/example/ui/screens/account'),
    ('EditProfileDialog', 'com.example.ui.screens.account', 'app/src/main/java/com/example/ui/screens/account'),
    ('AddFactDialog', 'com.example.ui.screens.account', 'app/src/main/java/com/example/ui/screens/account'),
    
    ('ProfileFieldRow', 'com.example.ui.components', 'app/src/main/java/com/example/ui/components'),
    ('MetricBadge', 'com.example.ui.components', 'app/src/main/java/com/example/ui/components'),
    ('RoutineBadge', 'com.example.ui.components', 'app/src/main/java/com/example/ui/components'),
    ('ConnectorCard', 'com.example.ui.components', 'app/src/main/java/com/example/ui/components'),
    ('ToggleSettingRow', 'com.example.ui.components', 'app/src/main/java/com/example/ui/components'),
    ('DbStatPill', 'com.example.ui.components', 'app/src/main/java/com/example/ui/components')
]

for name, pkg, directory in components:
    if write_component(name, pkg, directory):
        print(f"Extracted {name}")
        # Remove it from source
        func_code = extract_function(name)
        if func_code:
            # We want to replace it with empty string, but be careful of regex limits.
            # Using simple string replace since it's exact match.
            source = source.replace(func_code, "")

# Now we need to add imports to UserAccountScreen for the new packages
imports_to_add = "import com.example.ui.screens.account.*\nimport com.example.ui.components.*\n"
source = re.sub(r'(package com\.example\.ui\.screens\n\n)', r'\1' + imports_to_add, source, count=1)

with open("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt", "w") as f:
    f.write(source)

print("Done splitting UserAccountScreen.kt")
