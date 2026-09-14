import re

file_path = 'app/src/main/java/com/example/presentation/viewmodel/WellnessViewModel.kt'
with open(file_path, 'r') as f:
    content = f.read()

content = content.replace('import com.example.data.repository.LumiRepositoryImpl\n', '')
content = content.replace('import com.example.domain.repository.LumiRepository\n', '')

with open(file_path, 'w') as f:
    f.write(content)

