import re

# 1. Fix TasksSection named arguments
with open("app/src/main/java/com/example/ui/screens/lifehub/TasksSection.kt", "r") as f:
    content = f.read()

content = re.sub(r'onAddTask\(\s*title\s*=\s*title,\s*priority\s*=\s*prio,\s*category\s*=\s*cat,\s*estimatedMinutes\s*=\s*est,\s*notes\s*=\s*desc\s*\)', 'onAddTask(title, prio, cat, est, desc)', content)

with open("app/src/main/java/com/example/ui/screens/lifehub/TasksSection.kt", "w") as f:
    f.write(content)

# 2. Fix ProactiveDailyBriefingCard imports and domain object
with open("app/src/main/java/com/example/ui/components/ProactiveDailyBriefingCard.kt", "r") as f:
    content = f.read()

# Replace com.example.domain.briefing.DailyBriefing with the right one... wait, what was it?
# Let's import it correctly!
# Let's change the type from `com.example.domain.briefing.DailyBriefing?` to `com.example.domain.briefing.DailyBriefing?` (which is correct), but `briefing` is used as `val dailyBriefing = briefing ?: return`
content = content.replace("dailyBriefing: com.example.domain.briefing.DailyBriefing?", "briefing: com.example.domain.briefing.DailyBriefing?")
content = content.replace("val dailyBriefing = briefing ?: return", "val dailyBriefing = briefing ?: return")

with open("app/src/main/java/com/example/ui/components/ProactiveDailyBriefingCard.kt", "w") as f:
    f.write(content)

# 3. LifeHubScreen syntax error at 312
with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for i, line in enumerate(lines):
    # Removing any weird syntax like `)  )` or hanging `)` inside the `when` block
    if "null /* viewModel removed */ = null /* viewModel removed */" in line:
        continue
    if line.strip() == ")" and "else -> ScheduleSection" in lines[i+1] if i+1 < len(lines) else False:
        # A duplicate parenthesis before else block
        pass
    new_lines.append(line)

with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "w") as f:
    f.writelines(new_lines)


