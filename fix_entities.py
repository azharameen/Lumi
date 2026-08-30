with open("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt", "r") as f:
    content = f.read()

content = content.replace("MemoryEntity", "PetMemoryEntity")
content = content.replace("ChatMessage", "ChatMessageEntity")
content = content.replace("List<com.example.data.local.entity.ChatMessageEntityEntity>", "List<com.example.data.local.entity.ChatMessageEntity>")
content = content.replace("import com.example.data.local.entity.ChatMessageEntityEntity", "import com.example.data.local.entity.ChatMessageEntity")

with open("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("MemoryEntity", "PetMemoryEntity")
content = content.replace("ChatMessage", "ChatMessageEntity")
content = content.replace("List<com.example.data.local.entity.ChatMessageEntityEntity>", "List<com.example.data.local.entity.ChatMessageEntity>")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
