import re

with open('app/src/main/java/com/example/presentation/screens/account/LlmSettingsSection.kt', 'r') as f:
    lines = f.readlines()

def fix_line(i, line, expected_spaces, replacement):
    if line.strip() == '' and len(line) - 1 == expected_spaces: # -1 for \n
        lines[i] = replacement + '\n'

for i, line in enumerate(lines):
    # Fix the `                                    }` replaced by `        `
    if line == '        \n' and '}' not in line:
        lines[i] = '                                    }\n'
    # Fix the `                                }` replaced by `    `
    if line == '    \n' and '}' not in line:
        lines[i] = '                                }\n'
    # Fix the missing `                        },` that became `\n` or empty spaces
    if i == 283 and line == '                        \n':
        lines[i] = '                        },\n'
    # Let's check for other obvious ones based on empty space length
    if line == '            \n':
        lines[i] = '                                        }\n'
    if line == '                \n':
        lines[i] = '                                            }\n'
    if line == '                    \n':
        lines[i] = '                                                }\n'

with open('app/src/main/java/com/example/presentation/screens/account/LlmSettingsSection.kt', 'w') as f:
    f.writelines(lines)
