import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Let's completely remove `.align(Alignment.BottomCenter)` from the Column because we can just place it
content = content.replace("                        .align(Alignment.BottomCenter)\n", "")

# And we have an extra or missing brace.
# Let's count them or properly format it
lines = content.split('\n')
with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    for line in lines:
        if line.strip() == "} // End of Top Left Row":
            f.write(line + "\n")
        else:
            f.write(line + "\n")

