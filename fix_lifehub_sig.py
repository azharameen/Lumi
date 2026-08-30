import re

with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "r") as f:
    content = f.read()

old_sig = """fun LifeHubScreen(
    viewModel: LumiViewModel,
    onNavigateToChat: (prompt: String?) -> Unit
) {"""

new_sig = """fun LifeHubScreen(
    uiState: com.example.ui.viewmodel.LumiUiState,
    onSetSubTab: (Int) -> Unit,
    onNavigateToChat: (String?) -> Unit
) {"""

content = re.sub(r'fun LifeHubScreen\s*\([^)]*viewModel:\s*LumiViewModel[^)]*\)\s*\{', new_sig, content, flags=re.MULTILINE)

content = re.sub(r'\s*val uiState by viewModel\.uiState\.collectAsState\(\)', "", content)
content = re.sub(r'import com\.example\.ui\.viewmodel\.LumiViewModel', "", content)

content = content.replace("viewModel.setLifeHubSubTab(", "onSetSubTab(")
content = content.replace("viewModel", "null /* viewModel removed */")

with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    main_content = f.read()

old_lifehub = """NavDestination.LifeHub.tabIndex -> LifeHubScreen(
                        viewModel = viewModel,
                        onNavigateToChat = { prompt ->
                            viewModel.setSelectedTab(NavDestination.Assistant.tabIndex)
                            prompt?.let { viewModel.sendMessage(it) }
                        }
                    )"""

new_lifehub = """NavDestination.LifeHub.tabIndex -> LifeHubScreen(
                        uiState = uiState,
                        onSetSubTab = { viewModel.setLifeHubSubTab(it) },
                        onNavigateToChat = { prompt ->
                            viewModel.setSelectedTab(NavDestination.Assistant.tabIndex)
                            prompt?.let { viewModel.sendMessage(it) }
                        }
                    )"""

main_content = main_content.replace(old_lifehub, new_lifehub)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(main_content)

