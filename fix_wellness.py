with open("app/src/main/java/com/example/ui/screens/WellnessScreen.kt", "r") as f:
    content = f.read()

content = content.replace("if (logs.isEmpty()) {", "if (logs.itemCount == 0) {")

old_items = """            items(logs) { log ->
                Card("""

new_items = """            items(
                count = logs.itemCount,
                key = logs.itemKey { it.id },
                contentType = logs.itemContentType { "wellness_log" }
            ) { index ->
                val log = logs[index]
                if (log != null) {
                Card("""

content = content.replace(old_items, new_items)

# Add closing brace for the `if (log != null)` check
content = content.replace("                }\n            }\n        }\n    }\n}\n", "                }\n            }\n            }\n        }\n    }\n}\n") # Just replace the end if needed. Wait, it's safer to use regex.

with open("app/src/main/java/com/example/ui/screens/WellnessScreen.kt", "w") as f:
    f.write(content)
