with open("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt", "r") as f:
    content = f.read()

content = content.replace("import com.example.ui.components.*", "import com.example.ui.components.*\nimport com.example.data.local.entity.MemoryEntity\nimport com.example.data.local.entity.ChatMessage")

content = content.replace("viewModel = viewModel", "")

with open("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    main_content = f.read()

old_account = """NavDestination.Account.tabIndex -> UserAccountScreen(
                        viewModel = viewModel,
                        onNavigateToChat = { prompt ->
                            viewModel.setSelectedTab(NavDestination.Assistant.tabIndex)
                            prompt?.let { viewModel.sendMessage(it) }
                        }
                    )"""

new_account = """NavDestination.Account.tabIndex -> UserAccountScreen(
                        userProfile = userProfile,
                        userFacts = viewModel.userFacts.collectAsState().value,
                        petStatus = viewModel.petStatus.collectAsState().value,
                        benchmarkStatus = viewModel.benchmarkStatus.collectAsState().value ?: "",
                        tasks = viewModel.allTasks.collectAsState().value,
                        events = viewModel.allCalendarEvents.collectAsState().value,
                        memories = viewModel.allMemories.collectAsState().value,
                        messages = viewModel.chatMessages.collectAsState().value,
                        onUpdateProfile = { updated -> viewModel.updateUserProfile(updated) },
                        onAddUserFact = { cat, txt, isPinned -> viewModel.addUserFact(cat, txt, isPinned) },
                        onRemoveUserFact = { id -> viewModel.removeUserFact(id) },
                        onTogglePinFact = { id -> viewModel.togglePinFact(id) },
                        onClearAiAnalytics = { viewModel.clearAiAnalytics() },
                        onNavigateToChat = { prompt ->
                            viewModel.setSelectedTab(NavDestination.Assistant.tabIndex)
                            prompt?.let { viewModel.sendMessage(it) }
                        }
                    )"""

main_content = main_content.replace(old_account, new_account)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(main_content)

