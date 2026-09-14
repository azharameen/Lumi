import re
file_path = 'app/src/main/java/com/example/presentation/viewmodel/PetViewModel.kt'
with open(file_path, 'r') as f:
    content = f.read()
# Remove duplicate import
content = re.sub(r'import com\.example\.domain\.repository\.PetRepository\n+', 'import com.example.domain.repository.PetRepository\n', content)
with open(file_path, 'w') as f:
    f.write(content)
