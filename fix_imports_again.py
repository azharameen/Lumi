import os

def add_import(filepath, import_str):
    with open(filepath, 'r') as f:
        text = f.read()
    if import_str not in text:
        text = text.replace('import androidx.lifecycle.ViewModel', f'{import_str}\nimport androidx.lifecycle.ViewModel')
        with open(filepath, 'w') as f:
            f.write(text)

add_import('app/src/main/java/com/example/core/di/AppModule.kt', 'import com.example.domain.service.AnalyticsService\nimport com.example.domain.service.DeviceSensorsService\nimport com.example.data.firebase.LumiAnalyticsManager\nimport com.example.data.device.SensorsManager')
# AppModule doesn't have ViewModel import, fallback to Koin import
with open('app/src/main/java/com/example/core/di/AppModule.kt', 'r') as f:
    text = f.read()
if 'import com.example.domain.service.AnalyticsService' not in text:
    text = text.replace('import org.koin.dsl.module', 'import org.koin.dsl.module\nimport com.example.domain.service.AnalyticsService\nimport com.example.domain.service.DeviceSensorsService\nimport com.example.data.firebase.LumiAnalyticsManager\nimport com.example.data.device.SensorsManager')
    with open('app/src/main/java/com/example/core/di/AppModule.kt', 'w') as f:
        f.write(text)

add_import('app/src/main/java/com/example/presentation/viewmodel/ChatViewModel.kt', 'import com.example.domain.model.ChatMessage')
add_import('app/src/main/java/com/example/presentation/viewmodel/LifeHubViewModel.kt', 'import com.example.domain.service.DeviceSensorsService\nimport com.example.domain.service.AnalyticsService')
add_import('app/src/main/java/com/example/presentation/viewmodel/PetViewModel.kt', 'import com.example.domain.service.DeviceSensorsService\nimport com.example.domain.service.AnalyticsService')
add_import('app/src/main/java/com/example/presentation/viewmodel/LumiViewModel.kt', 'import com.example.domain.service.DeviceSensorsService\nimport com.example.domain.service.AnalyticsService')
add_import('app/src/main/java/com/example/presentation/viewmodel/WellnessViewModel.kt', 'import com.example.domain.service.DeviceSensorsService\nimport com.example.domain.service.AnalyticsService')
add_import('app/src/main/java/com/example/presentation/viewmodel/AiSettingsViewModel.kt', 'import com.example.domain.service.DeviceSensorsService\nimport com.example.domain.service.AnalyticsService')

