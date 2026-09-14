import re

file_path = 'app/src/main/java/com/example/framework/LumiAppWidgetProvider.kt'
with open(file_path, 'r') as f:
    content = f.read()

content = content.replace(
    'import com.example.data.repository.LumiRepositoryImpl\n',
    'import com.example.domain.repository.WellnessRepository\nimport com.example.domain.repository.PetRepository\nimport com.example.domain.repository.TaskGoalRepository\n'
)

content = content.replace(
    'val repo = org.koin.core.context.GlobalContext.get().get<com.example.domain.repository.LumiRepository>()\n                repo.logWellness(8, "Hydrated via Widget", 8, 1, "Quick widget tap")',
    'val repo = org.koin.core.context.GlobalContext.get().get<WellnessRepository>()\n                repo.logWellness(8, "Hydrated via Widget", 8, 1, "Quick widget tap")'
)

content = content.replace(
    'val repo = org.koin.core.context.GlobalContext.get().get<com.example.domain.repository.LumiRepository>()\n                repo.petTheCharacter()',
    'val repo = org.koin.core.context.GlobalContext.get().get<PetRepository>()\n                repo.petTheAnimal()'
)

content = content.replace(
    'val repo = org.koin.core.context.GlobalContext.get().get<com.example.domain.repository.LumiRepository>()\n                    val pet = repo.petStatus.firstOrNull()\n                    val tasks = repo.allTasks.firstOrNull()',
    '''val petRepo = org.koin.core.context.GlobalContext.get().get<PetRepository>()
                    val taskRepo = org.koin.core.context.GlobalContext.get().get<TaskGoalRepository>()
                    val pet = petRepo.petStatus.firstOrNull()
                    val tasks = taskRepo.allTasks.firstOrNull()'''
)

with open(file_path, 'w') as f:
    f.write(content)

