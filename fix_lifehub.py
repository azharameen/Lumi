with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "r") as f:
    content = f.read()

call_old = """                    0 -> ScheduleSection(
                        events = events,
                        onDeleteEvent = onDeleteEvent,
                        onAddEvent = onAddEvent,
                        dailyBriefing = dailyBriefing,
                        onSpeakBriefing = onSpeakBriefing,
                        
                        onNavigateToChat = onNavigateToChat
                    )"""
call_new = """                    0 -> ScheduleSection(
                        events = events,
                        dailyBriefing = dailyBriefing,
                        onAction = onAction
                    )"""
content = content.replace(call_old, call_new)

with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "w") as f:
    f.write(content)
