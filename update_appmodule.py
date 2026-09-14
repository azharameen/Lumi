import re

file_path = 'app/src/main/java/com/example/core/di/AppModule.kt'
with open(file_path, 'r') as f:
    content = f.read()

# Add DeviceStateRepository
content = content.replace(
    'single<PetMemoryRepository> { PetMemoryRepositoryImpl(get()) }',
    'single<PetMemoryRepository> { PetMemoryRepositoryImpl(get()) }\n    single<DeviceStateRepository> { DeviceStateRepositoryImpl(get()) }'
)

# Remove LumiRepository
content = re.sub(
    r'    // Legacy/Facade Repository\n    single<LumiRepository> \{ LumiRepositoryImpl\.getInstance\(androidContext\(\), get\(\)\) \}\n',
    '',
    content
)

# Fix missing import
content = content.replace(
    'import com.example.data.repository.*',
    'import com.example.data.repository.*\nimport com.example.domain.repository.DeviceStateRepository'
)

with open(file_path, 'w') as f:
    f.write(content)

