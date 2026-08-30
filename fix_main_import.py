with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

if "import androidx.paging.compose.collectAsLazyPagingItems" not in content:
    content = content.replace("import androidx.lifecycle.compose.collectAsStateWithLifecycle", "import androidx.lifecycle.compose.collectAsStateWithLifecycle\nimport androidx.paging.compose.collectAsLazyPagingItems")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
