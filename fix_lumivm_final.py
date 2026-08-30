import re

with open("app/src/main/java/com/example/ui/viewmodel/LumiViewModel.kt", "r") as f:
    content = f.read()

# Remove remaining lingering briefing properties
content = re.sub(r'private val _dailyBriefing.*?\n', '', content)
content = re.sub(r'val dailyBriefing.*?\n', '', content)
content = re.sub(r'private val _isBriefingGenerating.*?\n', '', content)
content = re.sub(r'val isBriefingGenerating.*?\n', '', content)

# Remove refreshDailyBriefing calls inside LumiViewModel init/functions if any
content = re.sub(r'refreshDailyBriefing\(\)', '', content)

with open("app/src/main/java/com/example/ui/viewmodel/LumiViewModel.kt", "w") as f:
    f.write(content)
