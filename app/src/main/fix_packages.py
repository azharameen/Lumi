import os

framework_classes = [
    'AppShortcutsManager',
    'LumiAlarmReceiver',
    'LumiAppWidgetProvider',
    'PetOverlayService',
    'OverlayLifecycleOwner'
]

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    new_content = content
    # Handle presentation
    new_content = new_content.replace('com.example.ui.theme', 'com.example.core.theme')
    new_content = new_content.replace('com.example.ui.navigation', 'com.example.core.navigation')
    new_content = new_content.replace('com.example.ui', 'com.example.presentation')
    
    # Handle framework classes
    for cls in framework_classes:
        new_content = new_content.replace(f'com.example.service.{cls}', f'com.example.framework.{cls}')
        # also handle case where it's a package declaration
        if 'PetOverlayService' in filepath or 'LumiAlarmReceiver' in filepath or 'LumiAppWidgetProvider' in filepath or 'AppShortcutsManager' in filepath or 'OverlayLifecycleOwner' in filepath:
            new_content = new_content.replace('package com.example.service', 'package com.example.framework')
    
    # Handle the rest of service -> data.device
    new_content = new_content.replace('package com.example.service', 'package com.example.data.device')
    new_content = new_content.replace('com.example.service.', 'com.example.data.device.')

    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Updated {filepath}")

for root, _, files in os.walk('.'):
    for file in files:
        if file.endswith('.kt') or file.endswith('.xml'):
            process_file(os.path.join(root, file))
