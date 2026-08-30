filepath = 'presentation/home/HomeScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    lines = f.readlines()

lines.insert(1, 'import com.example.presentation.home.components.*\n')

with open(filepath, 'w', encoding='utf-8') as f:
    f.writelines(lines)
print('Fixed imports')
