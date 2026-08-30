import os

files = {
    'presentation/home/HomeScreen.kt': 'package com.example.presentation.home\n',
    'presentation/home/components/HomeScreenComponents.kt': 'package com.example.presentation.home.components\n'
}

for filepath, new_pkg in files.items():
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    if lines[0].startswith('package '):
        lines[0] = new_pkg
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.writelines(lines)

# Update MainActivity.kt
ma = 'MainActivity.kt'
with open(ma, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('com.example.presentation.screens.HomeScreen', 'com.example.presentation.home.HomeScreen')

with open(ma, 'w', encoding='utf-8') as f:
    f.write(content)
print('Fixed packages')
