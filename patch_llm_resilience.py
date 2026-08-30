import re

with open("app/src/main/java/com/example/data/remote/ModelDownloadManager.kt", "r") as f:
    content = f.read()

# 1. Add PAUSED
content = content.replace("    ERROR\n}", "    ERROR,\n    PAUSED\n}")

# 2. Update cancelDownload to actually cancel but not delete if paused
pause_logic = """
    fun pauseDownload(modelId: String) {
        activeDownloadJobs[modelId]?.cancel()
        activeDownloadJobs.remove(modelId)
        
        val spec = catalog.find { it.id == modelId } ?: return
        val modelFile = java.io.File(getModelsDirectory(), "${spec.id}.bin")
        
        updateProgress(
            modelId,
            ModelDownloadProgress(
                modelId = modelId,
                status = ModelDownloadStatus.PAUSED,
                progress = if (spec.sizeBytes > 0) modelFile.length().toFloat() / spec.sizeBytes.toFloat() else 0f,
                bytesDownloaded = modelFile.length(),
                totalBytes = spec.sizeBytes,
                localFilePath = modelFile.absolutePath
            )
        )
    }
"""

content = content.replace("fun cancelDownload(modelId: String) {", pause_logic + "\n    fun cancelDownload(modelId: String) {")

# 3. Modify downloadModel to resume if file exists
download_start = """
        val job = scope.launch {
            try {
"""
download_resilience = """
        val job = scope.launch {
            try {
                var downloaded = if (modelFile.exists()) modelFile.length() else 0L
                val appendMode = downloaded > 0L
                val fos = java.io.FileOutputStream(modelFile, appendMode)
"""
content = content.replace(download_start, download_resilience)

# Fix fos instantiation in original
content = content.replace("val fos = FileOutputStream(modelFile)", "")
content = content.replace("var downloaded = 0L", "")

# Fix header marker writing
old_header = """                // Write valid header marker
                val headerMarker = "LUMI_LLM_INT4_TENSOR_GGUF_${spec.id}\\n".toByteArray()
                fos.write(headerMarker)
                downloaded += headerMarker.size"""

new_header = """                // Write valid header marker if fresh
                if (!appendMode) {
                    val headerMarker = "LUMI_LLM_INT4_TENSOR_GGUF_${spec.id}\\n".toByteArray()
                    fos.write(headerMarker)
                    downloaded += headerMarker.size
                }"""
content = content.replace(old_header, new_header)

with open("app/src/main/java/com/example/data/remote/ModelDownloadManager.kt", "w") as f:
    f.write(content)

