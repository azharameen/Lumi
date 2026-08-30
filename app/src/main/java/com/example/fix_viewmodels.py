import os
import glob

vms = glob.glob('presentation/viewmodel/*ViewModel.kt')

for vm in vms:
    with open(vm, 'r', encoding='utf-8') as f:
        lines = f.readlines()
        
    new_lines = []
    has_container = False
    
    for i, line in enumerate(lines):
        if 'class ' in line and 'ViewModel(application: Application)' in line:
            new_lines.append(line)
            if 'val container = (application as com.example.LumiApplication).container' not in "".join(lines):
                new_lines.append("    private val container = (application as com.example.LumiApplication).container\n")
            continue
            
        # Replace direct instantions
        modified_line = line
        if 'LumiRepositoryImpl.getInstance(application)' in line:
            modified_line = modified_line.replace('LumiRepositoryImpl.getInstance(application)', 'container.repository')
        elif 'UserProfileManager.getInstance(application)' in line:
            modified_line = modified_line.replace('UserProfileManager.getInstance(application)', 'container.userProfileManager')
            modified_line = modified_line.replace('com.example.domain.account.', '') # Clean up fully qualified names
        elif 'VoiceEngine(application)' in line:
            modified_line = modified_line.replace('VoiceEngine(application)', 'container.voiceEngine')
        elif 'SensorsManager(application)' in line:
            modified_line = modified_line.replace('SensorsManager(application)', 'container.sensorsManager')
        elif 'AutonomousBriefingEngine(application)' in line:
            modified_line = modified_line.replace('AutonomousBriefingEngine(application)', 'container.briefingEngine')
        elif 'ModelDownloadManager.getInstance(application)' in line:
            modified_line = modified_line.replace('ModelDownloadManager.getInstance(application)', 'container.modelDownloadManager')
            
        new_lines.append(modified_line)
        
    with open(vm, 'w', encoding='utf-8') as f:
        f.writelines(new_lines)
    
print("ViewModels fixed")
