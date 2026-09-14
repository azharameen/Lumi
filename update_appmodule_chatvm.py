import re

file_path = 'app/src/main/java/com/example/core/di/AppModule.kt'
with open(file_path, 'r') as f:
    content = f.read()

content = content.replace(
    'viewModel { ChatViewModel(get(), get(), get(), get(), get(), getOrNull()) }',
    'viewModel { ChatViewModel(get(), get(), get(), get(), get(), get(), getOrNull()) }'
)

with open(file_path, 'w') as f:
    f.write(content)

