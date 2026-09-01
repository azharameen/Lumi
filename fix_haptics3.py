import re

with open('app/src/main/java/com/example/presentation/screens/UserAccountScreen.kt', 'r') as f:
    text = f.read()

target = """                        IconButton(
                            onClick = { showEditProfileDialog = true },"""
replacement = """                        IconButton(
                            onClick = { 
                                haptics.performClick()
                                showEditProfileDialog = true 
                            },"""
text = text.replace(target, replacement)

with open('app/src/main/java/com/example/presentation/screens/UserAccountScreen.kt', 'w') as f:
    f.write(text)
