import os
import re

with open("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt", "r") as f:
    content = f.read()

# We will just see what functions exist and how long they are.
funcs = re.findall(r'private fun ([A-Za-z0-9_]+)\(', content)
print("Private functions found:", funcs)
