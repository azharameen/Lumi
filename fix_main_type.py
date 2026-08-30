with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("val type = when (briefingStr.uppercase()) {", "val type: com.example.domain.briefing.BriefingType? = when (briefingStr.uppercase()) {")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
