import os

filepath = 'presentation/screens/HomeScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    lines = f.readlines()

split_index = -1
for i, line in enumerate(lines):
    if '// SUB-COMPONENTS' in line:
        split_index = i - 1
        break

if split_index != -1:
    main_content = lines[:split_index]
    components_content = lines[split_index:]

    # For components, add package and imports
    imports = []
    for line in main_content:
        if line.startswith('package '):
            components_content.insert(0, line)
        elif line.startswith('import '):
            imports.append(line)
    
    for imp in reversed(imports):
        components_content.insert(1, imp)

    with open(filepath, 'w', encoding='utf-8') as f:
        f.writelines(main_content)

    with open('presentation/home/components/HomeScreenComponents.kt', 'w', encoding='utf-8') as f:
        f.writelines(components_content)
    
    print('Split successful')
else:
    print('Split index not found')
