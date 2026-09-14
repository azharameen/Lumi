import re

with open('app/src/main/java/com/example/data/firebase/LumiAnalyticsManager.kt', 'r') as f:
    text = f.read()
if 'import com.example.domain.service.AnalyticsService' not in text:
    text = text.replace('import com.google.firebase.analytics.FirebaseAnalytics', 'import com.google.firebase.analytics.FirebaseAnalytics\nimport com.example.domain.service.AnalyticsService')
    text = text.replace('class LumiAnalyticsManager(\n    private val context: Context\n) {', 'class LumiAnalyticsManager(\n    private val context: Context\n) : AnalyticsService {')
    text = text.replace('fun logPetInteraction', 'override fun logPetInteraction')
    text = text.replace('fun logPetLevelUp', 'override fun logPetLevelUp')
    text = text.replace('fun logSoundscapeSession', 'override fun logSoundscapeSession')
    text = text.replace('fun logVaultAction', 'override fun logVaultAction')
    text = text.replace('fun logRemoteConfigSync', 'override fun logRemoteConfigSync')
    text = text.replace('fun logGoalMilestone', 'override fun logGoalMilestone')
    text = text.replace('fun logWellnessSession', 'override fun logWellnessSession')
    text = text.replace('fun logAiChatMessage', 'override fun logAiChatMessage')
    text = text.replace('fun logScreenView', 'override fun logScreenView')
    text = text.replace('fun logAuthEvent', 'override fun logAuthEvent')
    text = text.replace('fun setUserProperty', 'override fun setUserProperty')
    with open('app/src/main/java/com/example/data/firebase/LumiAnalyticsManager.kt', 'w') as f:
        f.write(text)

with open('app/src/main/java/com/example/data/device/SensorsManager.kt', 'r') as f:
    text = f.read()
if 'import com.example.domain.service.DeviceSensorsService' not in text:
    text = text.replace('import kotlinx.coroutines.flow.asStateFlow', 'import kotlinx.coroutines.flow.asStateFlow\nimport com.example.domain.service.DeviceSensorsService')
    text = text.replace('class SensorsManager(private val context: Context) : SensorEventListener {', 'class SensorsManager(private val context: Context) : SensorEventListener, DeviceSensorsService {')
    text = text.replace('val ambientLux', 'override val ambientLux')
    text = text.replace('fun startListening', 'override fun startListening')
    text = text.replace('fun stopListening', 'override fun stopListening')
    text = text.replace('fun vibratePurr', 'override fun vibratePurr')
    text = text.replace('fun vibrateTap', 'override fun vibrateTap')
    text = text.replace('fun vibrateCelebration', 'override fun vibrateCelebration')
    with open('app/src/main/java/com/example/data/device/SensorsManager.kt', 'w') as f:
        f.write(text)

