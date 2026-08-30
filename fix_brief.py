with open("app/src/main/java/com/example/ui/components/ProactiveDailyBriefingCard.kt", "r") as f:
    content = f.read()

content = content.replace("dailyBriefing: com.example.domain.dailyBriefing.DailyBriefing?,", "briefing: com.example.domain.briefing.DailyBriefing?,")

with open("app/src/main/java/com/example/ui/components/ProactiveDailyBriefingCard.kt", "w") as f:
    f.write(content)
