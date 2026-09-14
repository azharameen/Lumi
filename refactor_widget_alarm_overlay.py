import re

file_path = 'app/src/main/java/com/example/framework/LumiAlarmReceiver.kt'
with open(file_path, 'r') as f:
    content = f.read()
content = content.replace('import com.example.data.repository.LumiRepositoryImpl\n', '')
content = content.replace('val repository = org.koin.core.context.GlobalContext.get().get<com.example.domain.repository.LumiRepository>()', '// Alarms rescheduled')
with open(file_path, 'w') as f:
    f.write(content)


file_path = 'app/src/main/java/com/example/framework/LumiAppWidgetProvider.kt'
with open(file_path, 'r') as f:
    content = f.read()
content = content.replace('import com.example.data.repository.LumiRepositoryImpl\n', '')
content = content.replace(
    'val repo = org.koin.core.context.GlobalContext.get().get<com.example.domain.repository.LumiRepository>()\n                repo.logWellness(8, "Hydrated via Widget", 8, 1, "Quick widget tap")',
    'val repo = org.koin.core.context.GlobalContext.get().get<com.example.domain.repository.WellnessRepository>()\n                repo.logWellness(8, "Hydrated via Widget", 8, 1, "Quick widget tap")'
)
content = content.replace(
    'val repo = org.koin.core.context.GlobalContext.get().get<com.example.domain.repository.LumiRepository>()\n                repo.petTheCharacter()',
    'val repo = org.koin.core.context.GlobalContext.get().get<com.example.domain.repository.PetRepository>()\n                repo.petTheAnimal()'
)
content = content.replace(
    'val repo = org.koin.core.context.GlobalContext.get().get<com.example.domain.repository.LumiRepository>()',
    'val petRepo = org.koin.core.context.GlobalContext.get().get<com.example.domain.repository.PetRepository>()\n                    val taskRepo = org.koin.core.context.GlobalContext.get().get<com.example.domain.repository.TaskGoalRepository>()'
)
content = content.replace('repo.petStatus.firstOrNull()', 'petRepo.petStatus.firstOrNull()')
content = content.replace('repo.allTasks.firstOrNull()', 'taskRepo.allTasks.firstOrNull()')
with open(file_path, 'w') as f:
    f.write(content)


file_path = 'app/src/main/java/com/example/framework/PetOverlayService.kt'
with open(file_path, 'r') as f:
    content = f.read()
content = content.replace('import com.example.domain.repository.LumiRepository', 'import com.example.domain.repository.PetRepository')
content = content.replace('private lateinit var repository: LumiRepository', 'private lateinit var repository: PetRepository')
content = content.replace('repository = koin.get<LumiRepository>()', 'repository = koin.get<PetRepository>()')
content = content.replace('repository.petTheCharacter()', 'repository.petTheAnimal()')
with open(file_path, 'w') as f:
    f.write(content)

