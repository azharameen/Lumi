with open("app/src/main/java/com/example/ui/viewmodel/LifeHubViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("import com.example.domain.model.SoundscapeType", "import com.example.service.SoundscapeType")
content = content.replace("petEvolution: com.example.domain.model.PetEvolution?,", "petEvolution: com.example.data.local.entity.PetEvolutionEntity?,")

with open("app/src/main/java/com/example/ui/viewmodel/LifeHubViewModel.kt", "w") as f:
    f.write(content)
