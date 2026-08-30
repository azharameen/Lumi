import re
import os

with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "r") as f:
    source = f.read()

os.makedirs("app/src/main/java/com/example/ui/screens/lifehub", exist_ok=True)
os.makedirs("app/src/main/java/com/example/ui/components", exist_ok=True)

def extract_function(name):
    pattern = r'(@Composable\s+)?(private\s+)?fun\s+' + name + r'\s*\('
    match = re.search(pattern, source)
    if not match:
        return None
    start_idx = match.start()
    
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
        
    func_code = re.sub(r'private\s+fun\s+' + name, r'fun ' + name, func_code)
    
    if not func_code.strip().startswith('@Composable'):
        func_code = '@Composable\n' + func_code.strip()
        
    imports = re.findall(r'^import\s+.*$', source, flags=re.MULTILINE)
    imports_str = '\n'.join(imports)
    
    file_content = f"package {package_name}\n\n{imports_str}\n\n{func_code}\n"
    
    with open(f"{target_dir}/{name}.kt", "w") as f:
        f.write(file_content)
        
    return True

components = [
    ('ScheduleSection', 'com.example.ui.screens.lifehub', 'app/src/main/java/com/example/ui/screens/lifehub'),
    ('TasksSection', 'com.example.ui.screens.lifehub', 'app/src/main/java/com/example/ui/screens/lifehub'),
    ('WellnessVaultSection', 'com.example.ui.screens.lifehub', 'app/src/main/java/com/example/ui/screens/lifehub'),
    ('AddEventDialog', 'com.example.ui.screens.lifehub', 'app/src/main/java/com/example/ui/screens/lifehub'),
    ('AddTaskDialog', 'com.example.ui.screens.lifehub', 'app/src/main/java/com/example/ui/screens/lifehub')
]

for name, pkg, directory in components:
    if write_component(name, pkg, directory):
        print(f"Extracted {name}")
        func_code = extract_function(name)
        if func_code:
            source = source.replace(func_code, "")

imports_to_add = "import com.example.ui.screens.lifehub.*\nimport com.example.ui.components.*\n"
source = re.sub(r'(package com\.example\.ui\.screens\n\n)', r'\1' + imports_to_add, source, count=1)

with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "w") as f:
    f.write(source)

print("Done splitting LifeHubScreen.kt")
