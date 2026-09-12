package com.example.data.remote

import android.content.Context
import android.os.StatFs
import androidx.datastore.preferences.core.*
import com.example.data.preferences.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

enum class ModelDownloadStatus {
    NOT_DOWNLOADED,
    DOWNLOADING,
    VERIFYING,
    DOWNLOADED,
    ERROR,
    PAUSED
}

enum class HardwareAccelerator(val displayName: String, val description: String) {
    GPU_OPENCL("GPU (OpenCL / Vulkan)", "Fastest graphics shader token generation"),
    NPU_NNAPI("NPU (Android NNAPI)", "Ultra energy-efficient dedicated AI tensor cores"),
    CPU_MULTITHREAD("CPU (4-Thread NEON)", "Compatible across all Android ARM processors")
}

data class LocalLlmModelSpec(
    val id: String,
    val name: String,
    val publisher: String,
    val parameterCount: String,
    val quantization: String,
    val sizeBytes: Long,
    val sizeDisplay: String,
    val contextWindowTokens: Int,
    val memoryRequiredRam: String,
    val requiredRamBytes: Long,
    val downloadUrl: String,
    val description: String,
    val recommendedFor: String,
    val sha256Checksum: String = "",
    val isDeviceCompatible: Boolean = true,
    val compatibilityReason: String = ""
)

data class ModelDownloadProgress(
    val modelId: String,
    val status: ModelDownloadStatus = ModelDownloadStatus.NOT_DOWNLOADED,
    val progress: Float = 0f,
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L,
    val speedMegaBytesPerSec: Double = 0.0,
    val etaSeconds: Long = 0L,
    val errorMessage: String? = null,
    val localFilePath: String? = null
)

/**
 * Enterprise-grade local model download and artifact lifecycle manager.
 * Uses public direct 100% ungated mirror URLs for 1-click model downloads without authentication.
 */
class ModelDownloadManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var instance: ModelDownloadManager? = null

        fun getInstance(context: Context): ModelDownloadManager {
            return instance ?: synchronized(this) {
                instance ?: ModelDownloadManager(context.applicationContext).also { instance = it }
            }
        }
        
        private val ACTIVE_MODEL = stringPreferencesKey("active_local_model")
        private val HARDWARE_ACCEL = stringPreferencesKey("hardware_accelerator")
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeDownloadJobs = ConcurrentHashMap<String, Job>()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    val catalog: List<LocalLlmModelSpec> by lazy {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val memoryInfo = android.app.ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalDeviceRam = memoryInfo.totalMem
        val osBufferBytes = 1_600_000_000L
        
        fun evaluateCompatibility(requiredBytes: Long): Pair<Boolean, String> {
            val availableForApp = totalDeviceRam - osBufferBytes
            return if (availableForApp >= requiredBytes) {
                Pair(true, "Perfect for your device.")
            } else {
                val reqGb = String.format("%.1f", requiredBytes / 1_000_000_000.0)
                val totalGb = String.format("%.1f", totalDeviceRam / 1_000_000_000.0)
                Pair(false, "Requires ${reqGb}GB free RAM. Your device total is ${totalGb}GB.")
            }
        }

        listOf(
            run {
                val reqRam = 2_200_000_000L
                val (isCompat, reason) = evaluateCompatibility(reqRam)
                LocalLlmModelSpec(
                    id = "gemma-2b-it-cpu-int4",
                    name = "Google Gemma 2B IT (CPU INT4)",
                    publisher = "Google DeepMind / Community",
                    parameterCount = "2.0 Billion",
                    quantization = "INT4 (MediaPipe TFL3)",
                    sizeBytes = 1_346_559_040L,
                    sizeDisplay = "1.25 GB",
                    contextWindowTokens = 2048,
                    memoryRequiredRam = "2.2 GB VRAM/RAM",
                    requiredRamBytes = reqRam,
                    downloadUrl = "https://huggingface.co/a8nova/gemma-2b-it-cpu-int4/resolve/main/gemma-2b-it-cpu-int4.bin",
                    description = "Official Google Gemma 2B CPU model for ultra-low memory on-device offline inference.",
                    recommendedFor = "Recommended for all Android devices",
                    isDeviceCompatible = isCompat,
                    compatibilityReason = reason
                )
            },
            run {
                val reqRam = 2_200_000_000L
                val (isCompat, reason) = evaluateCompatibility(reqRam)
                LocalLlmModelSpec(
                    id = "gemma-2b-it-gpu-int4",
                    name = "Google Gemma 2B IT (GPU OpenCL)",
                    publisher = "Google DeepMind / Community",
                    parameterCount = "2.0 Billion",
                    quantization = "INT4 (MediaPipe TFL3)",
                    sizeBytes = 1_354_301_440L,
                    sizeDisplay = "1.26 GB",
                    contextWindowTokens = 2048,
                    memoryRequiredRam = "2.2 GB VRAM/RAM",
                    requiredRamBytes = reqRam,
                    downloadUrl = "https://huggingface.co/jardpound/gemma-2b-it-gpu-int4/resolve/main/gemma-2b-it-gpu-int4.bin",
                    description = "Official Google Gemma 2B GPU model optimized for high-speed OpenCL hardware acceleration.",
                    recommendedFor = "Best for devices with Adreno/Mali GPUs",
                    isDeviceCompatible = isCompat,
                    compatibilityReason = reason
                )
            },
            run {
                val reqRam = 2_800_000_000L
                val (isCompat, reason) = evaluateCompatibility(reqRam)
                LocalLlmModelSpec(
                    id = "gemma2-2b-it-gpu-int8",
                    name = "Google Gemma 2 2B IT (GPU INT8)",
                    publisher = "Google DeepMind / Community",
                    parameterCount = "2.6 Billion",
                    quantization = "INT8 (MediaPipe TFL3)",
                    sizeBytes = 2_627_141_632L,
                    sizeDisplay = "2.44 GB",
                    contextWindowTokens = 4096,
                    memoryRequiredRam = "2.8 GB VRAM/RAM",
                    requiredRamBytes = reqRam,
                    downloadUrl = "https://huggingface.co/alexdlov/gemma2-2b-it-gpu-int8.bin/resolve/main/gemma2-2b-it-gpu-int8.bin",
                    description = "Next-generation Google Gemma 2 model with 4K context and state-of-the-art conversational quality.",
                    recommendedFor = "Recommended for phones with 8GB+ RAM",
                    isDeviceCompatible = isCompat,
                    compatibilityReason = reason
                )
            }
        )
    }

    private val _downloadStates = MutableStateFlow<Map<String, ModelDownloadProgress>>(emptyMap())
    val downloadStates: StateFlow<Map<String, ModelDownloadProgress>> = _downloadStates.asStateFlow()

    private val _activeModelId = MutableStateFlow("gemma-2b-it-cpu-int4")
    val activeModelId: StateFlow<String> = _activeModelId.asStateFlow()

    private val _selectedAccelerator = MutableStateFlow(HardwareAccelerator.GPU_OPENCL)
    val selectedAccelerator: StateFlow<HardwareAccelerator> = _selectedAccelerator.asStateFlow()

    init {
        scope.launch {
            val p = context.dataStore.data.first()
            _activeModelId.value = p[ACTIVE_MODEL] ?: "gemma-2b-it-cpu-int4"
            
            val accelName = p[HARDWARE_ACCEL] ?: HardwareAccelerator.GPU_OPENCL.name
            _selectedAccelerator.value = try {
                HardwareAccelerator.valueOf(accelName)
            } catch (_: Exception) {
                HardwareAccelerator.GPU_OPENCL
            }
        }
    }

    init {
        cleanupOrphanedTempFiles()
        checkExistingModelFiles()
    }

    fun getModelsDirectory(): File {
        val dir = File(context.filesDir, "llm_models")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getModelFile(modelId: String): File {
        return File(getModelsDirectory(), "$modelId.bin")
    }

    private fun getTempModelFile(modelId: String): File {
        return File(getModelsDirectory(), "$modelId.bin.part")
    }

    fun getAvailableStorageBytes(): Long {
        return try {
            val stat = StatFs(context.filesDir.path)
            stat.availableBlocksLong * stat.blockSizeLong
        } catch (_: Exception) {
            4_000_000_000L
        }
    }

    private fun cleanupOrphanedTempFiles() {
        try {
            val dir = getModelsDirectory()
            dir.listFiles()?.forEach { file ->
                if (file.name.endsWith(".part") || file.name.endsWith(".tmp")) {
                    val modelId = file.name.removeSuffix(".part").removeSuffix(".tmp")
                    if (!activeDownloadJobs.containsKey(modelId)) {
                        file.delete()
                    }
                }
            }
        } catch (_: Exception) {}
    }

    fun checkExistingModelFiles() {
        val dir = getModelsDirectory()
        val currentStates = mutableMapOf<String, ModelDownloadProgress>()

        catalog.forEach { spec ->
            val modelFile = File(dir, "${spec.id}.bin")
            val tempFile = File(dir, "${spec.id}.bin.part")

            if (modelFile.exists() && modelFile.length() > 0) {
                currentStates[spec.id] = ModelDownloadProgress(
                    modelId = spec.id,
                    status = ModelDownloadStatus.DOWNLOADED,
                    progress = 1.0f,
                    bytesDownloaded = modelFile.length(),
                    totalBytes = spec.sizeBytes,
                    localFilePath = modelFile.absolutePath
                )
            } else if (tempFile.exists() && tempFile.length() > 0) {
                val downloaded = tempFile.length()
                val progress = (downloaded.toFloat() / spec.sizeBytes.toFloat()).coerceIn(0f, 0.99f)
                currentStates[spec.id] = ModelDownloadProgress(
                    modelId = spec.id,
                    status = ModelDownloadStatus.PAUSED,
                    progress = progress,
                    bytesDownloaded = downloaded,
                    totalBytes = spec.sizeBytes,
                    localFilePath = tempFile.absolutePath
                )
            } else {
                currentStates[spec.id] = ModelDownloadProgress(
                    modelId = spec.id,
                    status = ModelDownloadStatus.NOT_DOWNLOADED,
                    progress = 0f,
                    bytesDownloaded = 0L,
                    totalBytes = spec.sizeBytes
                )
            }
        }
        _downloadStates.value = currentStates
    }

    fun setActiveModel(modelId: String) {
        _activeModelId.value = modelId
        scope.launch {
            context.dataStore.edit { prefs ->
                prefs[ACTIVE_MODEL] = modelId
            }
        }
    }

    fun setAccelerator(accelerator: HardwareAccelerator) {
        _selectedAccelerator.value = accelerator
        scope.launch {
            context.dataStore.edit { prefs ->
                prefs[HARDWARE_ACCEL] = accelerator.name
            }
        }
    }

    fun isModelDownloaded(modelId: String): Boolean {
        val modelFile = getModelFile(modelId)
        val state = _downloadStates.value[modelId]
        return modelFile.exists() && modelFile.length() > 0 && state?.status == ModelDownloadStatus.DOWNLOADED
    }

    fun getActiveModelSpec(): LocalLlmModelSpec {
        val currentId = _activeModelId.value
        return catalog.find { it.id == currentId } ?: catalog.first()
    }

    fun downloadModel(modelId: String) {
        val spec = catalog.find { it.id == modelId } ?: return
        if (activeDownloadJobs.containsKey(modelId)) return

        val targetFile = getModelFile(spec.id)
        val tempFile = getTempModelFile(spec.id)

        if (getAvailableStorageBytes() < (spec.sizeBytes + 100_000_000L)) {
            updateProgress(
                modelId,
                ModelDownloadProgress(
                    modelId = modelId,
                    status = ModelDownloadStatus.ERROR,
                    errorMessage = "Insufficient storage space. Need at least ${spec.sizeDisplay}.",
                    totalBytes = spec.sizeBytes
                )
            )
            return
        }

        val job = scope.launch {
            try {
                if (tempFile.exists()) {
                    tempFile.delete()
                }

                updateProgress(
                    modelId,
                    ModelDownloadProgress(
                        modelId = modelId,
                        status = ModelDownloadStatus.DOWNLOADING,
                        progress = 0.01f,
                        bytesDownloaded = 0L,
                        totalBytes = spec.sizeBytes,
                        localFilePath = tempFile.absolutePath
                    )
                )

                val request = Request.Builder()
                    .url(spec.downloadUrl)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36")
                    .header("Accept", "*/*")
                    .header("Connection", "keep-alive")
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw Exception("Download failed HTTP ${response.code}: ${response.message}")
                    val body = response.body ?: throw Exception("Empty response body")
                    
                    val serverLength = body.contentLength()
                    val targetBytes = if (serverLength > 0L) serverLength else spec.sizeBytes
                    var downloaded = 0L
                    val startTime = System.currentTimeMillis()

                    body.byteStream().use { inputStream ->
                        FileOutputStream(tempFile).use { fos ->
                            val buffer = ByteArray(32 * 1024)
                            var bytesRead: Int
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                fos.write(buffer, 0, bytesRead)
                                downloaded += bytesRead

                                val elapsedSec = (System.currentTimeMillis() - startTime) / 1000.0
                                val speed = if (elapsedSec > 0.1) (downloaded / (1024.0 * 1024.0)) / elapsedSec else 0.0
                                val remainingBytes = targetBytes - downloaded
                                val eta = if (speed > 0) (remainingBytes / (speed * 1024.0 * 1024.0)).toLong() else 0L
                                val progressFloat = (downloaded.toFloat() / targetBytes.toFloat()).coerceIn(0.01f, 0.99f)

                                updateProgress(
                                    modelId,
                                    ModelDownloadProgress(
                                        modelId = modelId,
                                        status = ModelDownloadStatus.DOWNLOADING,
                                        progress = progressFloat,
                                        bytesDownloaded = downloaded,
                                        totalBytes = targetBytes,
                                        speedMegaBytesPerSec = speed,
                                        etaSeconds = eta,
                                        localFilePath = tempFile.absolutePath
                                    )
                                )
                            }
                        }
                    }
                }

                updateProgress(
                    modelId,
                    ModelDownloadProgress(
                        modelId = modelId,
                        status = ModelDownloadStatus.VERIFYING,
                        progress = 0.99f,
                        bytesDownloaded = tempFile.length(),
                        totalBytes = spec.sizeBytes,
                        localFilePath = tempFile.absolutePath
                    )
                )

                val isChecksumValid = verifyFileIntegrity(tempFile, spec)
                if (!isChecksumValid) {
                    tempFile.delete()
                    throw IllegalStateException("Verification failed for ${spec.name}.")
                }

                if (targetFile.exists()) {
                    targetFile.delete()
                }

                val renameSuccess = tempFile.renameTo(targetFile)
                if (!renameSuccess) {
                    tempFile.copyTo(targetFile, overwrite = true)
                    tempFile.delete()
                }

                updateProgress(
                    modelId,
                    ModelDownloadProgress(
                        modelId = modelId,
                        status = ModelDownloadStatus.DOWNLOADED,
                        progress = 1.0f,
                        bytesDownloaded = targetFile.length(),
                        totalBytes = targetFile.length(),
                        localFilePath = targetFile.absolutePath
                    )
                )

                if (_activeModelId.value == modelId || !isModelDownloaded(_activeModelId.value)) {
                    setActiveModel(modelId)
                }

            } catch (e: Exception) {
                if (tempFile.exists() && e !is kotlinx.coroutines.CancellationException) {
                    tempFile.delete()
                }
                updateProgress(
                    modelId,
                    ModelDownloadProgress(
                        modelId = modelId,
                        status = ModelDownloadStatus.ERROR,
                        errorMessage = e.message ?: "Download failed",
                        totalBytes = spec.sizeBytes
                    )
                )
            } finally {
                activeDownloadJobs.remove(modelId)
            }
        }

        activeDownloadJobs[modelId] = job
    }

    fun notifyCorruptedOrDeleted(modelId: String) {
        val spec = catalog.find { it.id == modelId || modelId.contains(it.id) } ?: catalog.first()
        val targetFile = getModelFile(spec.id)
        val tempFile = getTempModelFile(spec.id)
        if (targetFile.exists()) targetFile.delete()
        if (tempFile.exists()) tempFile.delete()

        updateProgress(
            spec.id,
            ModelDownloadProgress(
                modelId = spec.id,
                status = ModelDownloadStatus.NOT_DOWNLOADED,
                progress = 0f,
                bytesDownloaded = 0L,
                totalBytes = spec.sizeBytes
            )
        )
    }

    private suspend fun verifyFileIntegrity(file: File, spec: LocalLlmModelSpec): Boolean = withContext(Dispatchers.IO) {
        if (!file.exists() || file.length() < 100_000L) return@withContext false
        try {
            FileInputStream(file).use { fis ->
                val header = ByteArray(8)
                val read = fis.read(header)
                if (read >= 4) {
                    val headerStr = String(header, 0, read, Charsets.US_ASCII)
                    if (!headerStr.contains("TFL3") && !headerStr.contains("TFL")) {
                        return@withContext false
                    }
                }
            }

            if (spec.sha256Checksum.isNotBlank()) {
                val digest = MessageDigest.getInstance("SHA-256")
                val buffer = ByteArray(64 * 1024)
                val fis = FileInputStream(file)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
                fis.close()
                val computedHash = digest.digest().joinToString("") { "%02x".format(it) }
                return@withContext computedHash.equals(spec.sha256Checksum, ignoreCase = true)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun verifyAllDownloadedModels(): List<String> = withContext(Dispatchers.IO) {
        val corruptedModels = mutableListOf<String>()
        val dir = getModelsDirectory()
        catalog.forEach { spec ->
            val modelFile = File(dir, "${spec.id}.bin")
            if (modelFile.exists()) {
                val isValid = verifyFileIntegrity(modelFile, spec)
                if (!isValid) {
                    corruptedModels.add(spec.name)
                    modelFile.delete()
                }
            }
        }
        if (corruptedModels.isNotEmpty()) {
            checkExistingModelFiles()
        }
        corruptedModels
    }

    fun pauseDownload(modelId: String) {
        activeDownloadJobs[modelId]?.cancel()
        activeDownloadJobs.remove(modelId)

        val spec = catalog.find { it.id == modelId } ?: return
        val tempFile = getTempModelFile(spec.id)

        updateProgress(
            modelId,
            ModelDownloadProgress(
                modelId = modelId,
                status = ModelDownloadStatus.PAUSED,
                progress = if (tempFile.exists() && spec.sizeBytes > 0) {
                    (tempFile.length().toFloat() / spec.sizeBytes.toFloat()).coerceIn(0f, 0.99f)
                } else 0f,
                bytesDownloaded = if (tempFile.exists()) tempFile.length() else 0L,
                totalBytes = spec.sizeBytes,
                localFilePath = tempFile.absolutePath
            )
        )
    }

    fun cancelDownload(modelId: String) {
        activeDownloadJobs[modelId]?.cancel()
        activeDownloadJobs.remove(modelId)

        val spec = catalog.find { it.id == modelId } ?: return
        val tempFile = getTempModelFile(spec.id)
        if (tempFile.exists()) {
            tempFile.delete()
        }

        updateProgress(
            modelId,
            ModelDownloadProgress(
                modelId = modelId,
                status = ModelDownloadStatus.NOT_DOWNLOADED,
                progress = 0f,
                bytesDownloaded = 0L,
                totalBytes = spec.sizeBytes
            )
        )
    }

    fun deleteModel(modelId: String) {
        cancelDownload(modelId)
        val spec = catalog.find { it.id == modelId } ?: return
        val targetFile = getModelFile(spec.id)
        val tempFile = getTempModelFile(spec.id)
        if (targetFile.exists()) {
            targetFile.delete()
        }
        if (tempFile.exists()) {
            tempFile.delete()
        }

        updateProgress(
            modelId,
            ModelDownloadProgress(
                modelId = modelId,
                status = ModelDownloadStatus.NOT_DOWNLOADED,
                progress = 0f,
                bytesDownloaded = 0L,
                totalBytes = spec.sizeBytes
            )
        )
    }

    private fun updateProgress(modelId: String, progress: ModelDownloadProgress) {
        val updated = _downloadStates.value.toMutableMap()
        updated[modelId] = progress
        _downloadStates.value = updated
    }
}
