import re

with open('app/src/main/java/com/example/presentation/home/components/SeamlessRpgPlayerHud.kt', 'r') as f:
    text = f.read()

target = """                    SciFiProgressBar(
                        label = "XP",
                        labelColor = LumiCyan,
                        fillRatio = xpRatio,
                        gradient = xpGradient,
                        height = MaterialTheme.spacing.small,
                        modifier = Modifier.weight(1f)
                    )"""

replacement = """                    SciFiProgressBar(
                        label = "XP",
                        labelColor = LumiCyan,
                        fillRatio = xpRatio,
                        gradient = xpGradient,
                        height = MaterialTheme.spacing.small,
                        modifier = Modifier.weight(1f),
                        trailingText = "${petStatus.exp}/${petStatus.expToNextLevel}",
                        trailingTextColor = LumiCyan
                    )"""

if target in text:
    new_text = text.replace(target, replacement)
    with open('app/src/main/java/com/example/presentation/home/components/SeamlessRpgPlayerHud.kt', 'w') as f:
        f.write(new_text)
    print("Replaced successfully.")
else:
    print("Target not found. Let's see what's actually there:")
    lines = text.splitlines()
    for i, line in enumerate(lines):
        if "label = \"XP\"" in line:
            for j in range(max(0, i-2), min(len(lines), i+8)):
                print(lines[j])
