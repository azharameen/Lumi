with open("app/src/main/java/com/example/data/remote/GeminiAgentEngine.kt", "r") as f:
    lines = f.readlines()

new_lines = []
skip = False
for i, line in enumerate(lines):
    if i == 163 and "return AgentExecutionResult(reply, emotion, reports)" in line:
        pass
    elif i == 164 and line.strip() == "}":
        pass
    else:
        new_lines.append(line)

with open("app/src/main/java/com/example/data/remote/GeminiAgentEngine.kt", "w") as f:
    f.writelines(new_lines)
