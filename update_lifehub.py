import re

file_path = 'app/src/main/java/com/example/presentation/viewmodel/LifeHubViewModel.kt'
with open(file_path, 'r') as f:
    content = f.read()

content = content.replace(
    'import com.example.data.repository.LumiRepositoryImpl\n',
    ''
)
content = content.replace(
    'import com.example.domain.repository.LumiRepository\n',
    'import com.example.domain.repository.DeviceStateRepository\n'
)

content = content.replace(
    'val repository: LumiRepository, // Still needed for Soundscape for now',
    'val deviceStateRepository: DeviceStateRepository,'
)

content = content.replace('repository.soundscapeState', 'deviceStateRepository.soundscapeState')
content = content.replace('repository.startSoundscape', 'deviceStateRepository.startSoundscape')
content = content.replace('repository.stopSoundscape', 'deviceStateRepository.stopSoundscape')
content = content.replace('repository.setSoundscapeVolume', 'deviceStateRepository.setSoundscapeVolume')
content = content.replace('repository.startFocusTimerWithSoundscape', 'deviceStateRepository.startFocusTimerWithSoundscape')
content = content.replace('repository.stopFocusTimerWithSoundscape', 'deviceStateRepository.stopFocusTimerWithSoundscape')

with open(file_path, 'w') as f:
    f.write(content)

