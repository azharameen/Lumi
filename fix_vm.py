import re

with open('app/src/main/java/com/example/ui/viewmodel/LumiViewModel.kt', 'r') as f:
    content = f.read()

content = re.sub(
    r'onSuccess = \{.*?\},.*?onError = \{ err ->.*?\}',
    r'''onResult = { success, err ->
                if (success) {
                    _uiState.value = _uiState.value.copy(isMemoryVaultUnlocked = true, vaultAuthError = null)
                } else {
                    _uiState.value = _uiState.value.copy(vaultAuthError = err ?: "Authentication failed")
                }
            }''',
    content, flags=re.DOTALL
)

content = content.replace('locationEngine.refreshLocation()', '// locationEngine.refreshLocation()')

content = content.replace(
    'val briefing = briefingEngine.generateBriefing(type ?: BriefingType.MORNING)',
    '''val briefing = briefingEngine.generateBriefing(
                type = type ?: BriefingType.MORNING,
                petStatus = petStatus.value,
                petEvolution = petEvolution.value,
                tasks = allTasks.value,
                events = allCalendarEvents.value,
                wellnessLogs = allWellnessLogs.value
            )'''
)

with open('app/src/main/java/com/example/ui/viewmodel/LumiViewModel.kt', 'w') as f:
    f.write(content)

