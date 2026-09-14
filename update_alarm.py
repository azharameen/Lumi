import re

file_path = 'app/src/main/java/com/example/framework/LumiAlarmReceiver.kt'
with open(file_path, 'r') as f:
    content = f.read()

content = content.replace('import com.example.data.repository.LumiRepositoryImpl\n', '')
content = re.sub(r'val repository = org.koin.core.context.GlobalContext.get\(\).get<com.example.domain.repository.LumiRepository>\(\)\n\s+', '', content)

with open(file_path, 'w') as f:
    f.write(content)

