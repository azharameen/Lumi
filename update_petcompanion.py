import re

file_path = 'app/src/main/java/com/example/data/repository/PetCompanionRepositoryImpl.kt'
with open(file_path, 'r') as f:
    content = f.read()

content = content.replace('import com.example.domain.repository.LumiRepository\n', '')
content = content.replace('private val lumiRepository: LumiRepository,', '')

with open(file_path, 'w') as f:
    f.write(content)

