with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for i, line in enumerate(lines):
    if line.strip() == ")" and i + 1 < len(lines) and "else -> ScheduleSection" in lines[i+1]:
        continue
    new_lines.append(line)

with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "w") as f:
    f.writelines(new_lines)
