with open('app/src/main/java/com/example/core/di/AppModule.kt', 'r') as f:
    text = f.read()

text = text.replace('import com.example.data.firebase.LumiAnalyticsManager', 'import com.example.data.firebase.LumiAnalyticsManager\nimport com.example.domain.service.AnalyticsService\nimport com.example.domain.service.DeviceSensorsService')

text = text.replace('single { LumiAnalyticsManager(androidContext()) }', 'single<AnalyticsService> { LumiAnalyticsManager(androidContext()) }')
text = text.replace('single { SensorsManager(androidContext()) }', 'single<DeviceSensorsService> { SensorsManager(androidContext()) }')

with open('app/src/main/java/com/example/core/di/AppModule.kt', 'w') as f:
    f.write(text)
