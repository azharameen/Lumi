with open("app/src/main/java/com/example/ui/overlay/PetOverlayRoot.kt", "r") as f:
    content = f.read()

content = content.replace("initial = PetStatus(", "initialValue = PetStatus(")

with open("app/src/main/java/com/example/ui/overlay/PetOverlayRoot.kt", "w") as f:
    f.write(content)
