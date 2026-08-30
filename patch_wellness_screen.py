import re

with open("app/src/main/java/com/example/ui/screens/WellnessScreen.kt", "r") as f:
    content = f.read()

if "androidx.paging.compose.LazyPagingItems" not in content:
    content = content.replace("import androidx.compose.foundation.lazy.LazyColumn", "import androidx.compose.foundation.lazy.LazyColumn\nimport androidx.paging.compose.LazyPagingItems\nimport androidx.paging.compose.collectAsLazyPagingItems\nimport androidx.paging.compose.itemContentType\nimport androidx.paging.compose.itemKey")

# WellnessScreen uses Logs directly? Let's check how it gets logs.
# val logs by viewModel.allWellnessLogs.collectAsStateWithLifecycle()
# we will change this to collectAsLazyPagingItems

content = content.replace("val logs by viewModel.allWellnessLogs.collectAsStateWithLifecycle()", "val logs = viewModel.pagedWellnessLogs.collectAsLazyPagingItems()")

# Update LazyColumn rendering
old_items = """                    items(logs) { log ->
                        WellnessHistoryItem(
                            log = log,
                            onIncrementHydration = { viewModel.incrementHydration(log.id) }
                        )
                    }"""

new_items = """                    items(
                        count = logs.itemCount,
                        key = logs.itemKey { it.id },
                        contentType = logs.itemContentType { "wellness_log" }
                    ) { index ->
                        val log = logs[index]
                        if (log != null) {
                            WellnessHistoryItem(
                                log = log,
                                onIncrementHydration = { viewModel.incrementHydration(log.id) }
                            )
                        }
                    }"""

if old_items in content:
    content = content.replace(old_items, new_items)
elif "items(logs.size)" in content:
    pass # Needs manual inspection if pattern differs

with open("app/src/main/java/com/example/ui/screens/WellnessScreen.kt", "w") as f:
    f.write(content)
