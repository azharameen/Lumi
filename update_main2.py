with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    main_content = f.read()

old_account = """onNavigateToChat = { prompt ->
                            viewModel.setSelectedTab(NavDestination.Assistant.tabIndex)
                            prompt?.let { viewModel.sendMessage(it) }
                        }
                    )"""

new_account = """localModelCatalog = viewModel.localModelCatalog.collectAsState().value,
                        modelDownloadStates = viewModel.modelDownloadStates.collectAsState().value,
                        activeLocalModelId = viewModel.activeLocalModelId.collectAsState().value,
                        selectedAccelerator = viewModel.selectedAccelerator.collectAsState().value,
                        onDownloadLocalModel = { id -> viewModel.downloadLocalModel(id) },
                        onCancelModelDownload = { id -> viewModel.cancelModelDownload(id) },
                        onDeleteLocalModel = { id -> viewModel.deleteLocalModel(id) },
                        onSetActiveLocalModel = { id -> viewModel.setActiveLocalModel(id) },
                        onSetHardwareAccelerator = { acc -> viewModel.setHardwareAccelerator(acc) },
                        onRunGemmaBenchmark = { viewModel.runGemmaBenchmark() },
                        onNavigateToChat = { prompt ->
                            viewModel.setSelectedTab(NavDestination.Assistant.tabIndex)
                            prompt?.let { viewModel.sendMessage(it) }
                        }
                    )"""

main_content = main_content.replace(old_account, new_account)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(main_content)
