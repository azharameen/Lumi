import re

with open('app/src/main/java/com/example/ui/viewmodel/LumiViewModel.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    original_line = line
    # If line contains viewModelScope.launch { and does NOT end with } }, but it should
    if "viewModelScope.launch {" in line and "}" in line:
        # Check if it's a one-liner that was broken
        # It's a one liner if it has { before viewModelScope and only one } after.
        # Let's just count { and } in the line.
        open_count = line.count('{')
        close_count = line.count('}')
        if open_count > close_count:
            # We need to append (open_count - close_count) '}' at the end before the newline
            line = line.rstrip('\n')
            # remove trailing spaces
            line = line.rstrip()
            line += " }" * (open_count - close_count) + "\n"
    
    new_lines.append(line)

with open('app/src/main/java/com/example/ui/viewmodel/LumiViewModel.kt', 'w') as f:
    f.writelines(new_lines)
