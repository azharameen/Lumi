import re

with open('app/src/main/java/com/example/presentation/screens/account/LlmSettingsSection.kt', 'r') as f:
    lines = f.readlines()

for i in range(len(lines)):
    # Fix the missing brace for Surface
    if "modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)" in lines[i]:
        if "}" not in lines[i+1] and "}" not in lines[i+2]:
            lines[i] = lines[i] + "                                }\n"
            
    # Fix the extra comma for when
    if "                        }," in lines[i] and "aiRoutingMode ==" in lines[i-1]:
        lines[i] = lines[i].replace("},", "}")

with open('app/src/main/java/com/example/presentation/screens/account/LlmSettingsSection.kt', 'w') as f:
    f.writelines(lines)
