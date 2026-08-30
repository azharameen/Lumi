import os
import glob
import re

def process_file(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    if "collectAsState(" not in content and "collectAsState()" not in content:
        return

    # Replace import
    content = content.replace("import androidx.compose.runtime.collectAsState", "import androidx.lifecycle.compose.collectAsStateWithLifecycle")
    
    # Replace usages
    content = content.replace(".collectAsState(", ".collectAsStateWithLifecycle(")
    content = content.replace(".collectAsState()", ".collectAsStateWithLifecycle()")

    with open(filepath, "w") as f:
        f.write(content)

for root, _, files in os.walk("app/src/main/java/com/example"):
    for file in files:
        if file.endswith(".kt"):
            process_file(os.path.join(root, file))

print("Lifecycle updates applied.")
