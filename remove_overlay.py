import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Pattern to remove the HudButton for Overlay
pattern = r"\s*// Screen Pet Overlay Toggle\s*HudButton\(\s*icon = Icons.Default.PictureInPictureAlt,[\s\S]*?onClick = \{[\s\S]*?\}\s*\)\s*\}"

content = re.sub(pattern, "", content)

# Wait, `HudButton(...)` does not end with `}`. Let's write a safer regex.
