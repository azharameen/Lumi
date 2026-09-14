import os

files = [
    'app/src/main/java/com/example/presentation/viewmodel/PetViewModel.kt',
    'app/src/main/java/com/example/presentation/viewmodel/LifeHubViewModel.kt',
    'app/src/main/java/com/example/presentation/viewmodel/ChatViewModel.kt',
    'app/src/main/java/com/example/presentation/viewmodel/WellnessViewModel.kt',
    'app/src/main/java/com/example/presentation/viewmodel/LumiViewModel.kt',
    'app/src/main/java/com/example/presentation/viewmodel/AiSettingsViewModel.kt'
]

for file in files:
    if os.path.exists(file):
        with open(file, 'r') as f:
            text = f.read()
        
        # Replace SensorsManager
        text = text.replace('import com.example.data.device.SensorsManager', 'import com.example.domain.service.DeviceSensorsService')
        text = text.replace('val sensorsManager: SensorsManager', 'val sensorsManager: DeviceSensorsService')
        text = text.replace('private val sensorsManager: SensorsManager', 'private val sensorsManager: DeviceSensorsService')
        
        # Replace LumiAnalyticsManager
        text = text.replace('import com.example.data.firebase.LumiAnalyticsManager', 'import com.example.domain.service.AnalyticsService')
        text = text.replace('private val analytics: LumiAnalyticsManager?', 'private val analytics: AnalyticsService?')
        text = text.replace('val analytics: LumiAnalyticsManager?', 'val analytics: AnalyticsService?')

        with open(file, 'w') as f:
            f.write(text)
