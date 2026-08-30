with open("app/src/main/java/com/example/ui/screens/lifehub/ScheduleSection.kt", "r") as f:
    content = f.read()
    
content = content.replace(")\n                    showAddEventDialog = false", "))\n                    showAddEventDialog = false")

with open("app/src/main/java/com/example/ui/screens/lifehub/ScheduleSection.kt", "w") as f:
    f.write(content)
