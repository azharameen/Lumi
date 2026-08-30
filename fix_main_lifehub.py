with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    lines = f.readlines()

out = []
in_lifehub = False
for line in lines:
    if "NavDestination.LifeHub.tabIndex -> LifeHubScreen(" in line:
        in_lifehub = True
        out.append(line)
        out.append("                        uiState = uiState,\n")
        out.append("                        tasks = viewModel.allTasks.collectAsStateWithLifecycle().value,\n")
        out.append("                        events = viewModel.allCalendarEvents.collectAsStateWithLifecycle().value,\n")
        out.append("                        wellnessLogs = viewModel.allWellnessLogs.collectAsStateWithLifecycle().value,\n")
        out.append("                        memories = viewModel.allMemories.collectAsStateWithLifecycle().value,\n")
        out.append("                        dailyBriefing = viewModel.dailyBriefing.collectAsStateWithLifecycle().value,\n")
        out.append("                        goalPlans = viewModel.allGoalPlans.collectAsStateWithLifecycle().value,\n")
        out.append("                        getMilestonesForGoal = { id -> viewModel.repository.getMilestonesForGoal(id) },\n")
        out.append("                        soundState = viewModel.soundscapeState.collectAsStateWithLifecycle().value,\n")
        out.append("                        onAction = handleLifeHubAction\n")
        out.append("                    )\n")
        continue
    
    if in_lifehub:
        if "NavDestination.Wellness.tabIndex -> WellnessScreen(" in line:
            in_lifehub = False
            out.append(line)
        continue

    out.append(line)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.writelines(out)

