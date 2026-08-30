with open("app/src/main/java/com/example/ui/screens/ScheduleScreen.kt", "r") as f:
    content = f.read()
content = content.replace("viewModel: LumiViewModel,", "viewModel: com.example.ui.viewmodel.LifeHubViewModel,")
with open("app/src/main/java/com/example/ui/screens/ScheduleScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/ui/screens/TasksScreen.kt", "r") as f:
    content = f.read()
content = content.replace("viewModel: LumiViewModel,", "viewModel: com.example.ui.viewmodel.LifeHubViewModel,")
with open("app/src/main/java/com/example/ui/screens/TasksScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/ui/screens/WardrobeScreen.kt", "r") as f:
    content = f.read()
content = content.replace("viewModel: LumiViewModel", "petViewModel: com.example.ui.viewmodel.PetViewModel, wellnessViewModel: com.example.ui.viewmodel.WellnessViewModel")
content = content.replace("viewModel.petStatus", "petViewModel.petStatus")
content = content.replace("viewModel.allMemories", "wellnessViewModel.allMemories")
content = content.replace("viewModel.onPetTouched()", "petViewModel.onPetTouched()")
content = content.replace("viewModel.onPetPetted()", "petViewModel.onPetPetted()")
content = content.replace("viewModel.setBloubShape", "petViewModel.setBloubShape")
content = content.replace("viewModel.setBloubSkinColor", "petViewModel.setBloubSkinColor")
with open("app/src/main/java/com/example/ui/screens/WardrobeScreen.kt", "w") as f:
    f.write(content)
