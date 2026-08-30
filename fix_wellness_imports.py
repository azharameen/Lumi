with open("app/src/main/java/com/example/ui/screens/WellnessScreen.kt", "r") as f:
    content = f.read()

if "import com.example.data.local.entity.MemoryEntity" not in content:
    content = content.replace("import com.example.data.local.entity.WellnessLogEntity", "import com.example.data.local.entity.WellnessLogEntity\nimport com.example.data.local.entity.MemoryEntity")

with open("app/src/main/java/com/example/ui/screens/WellnessScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/ui/viewmodel/WellnessViewModel.kt", "r") as f:
    content = f.read()

if "import com.example.data.local.entity.MemoryEntity" not in content:
    content = content.replace("import com.example.data.local.entity.WellnessLogEntity", "import com.example.data.local.entity.WellnessLogEntity\nimport com.example.data.local.entity.MemoryEntity")

with open("app/src/main/java/com/example/ui/viewmodel/WellnessViewModel.kt", "w") as f:
    f.write(content)

