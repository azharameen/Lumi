import os

for root, _, files in os.walk("app/src/main/java/com/example"):
    for file in files:
        if file.endswith(".kt"):
            filepath = os.path.join(root, file)
            with open(filepath, "r") as f:
                content = f.read()
            if ".collectAsStateWithLifecycle(initial =" in content:
                content = content.replace(".collectAsStateWithLifecycle(initial =", ".collectAsStateWithLifecycle(initialValue =")
                with open(filepath, "w") as f:
                    f.write(content)

