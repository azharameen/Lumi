import os
import re

replacements = {
    'app/src/main/java/com/example/presentation/screens/account/ConnectorsControlSection.kt': [
        ('Text("Welcome Greeting:"', 'Text(stringResource(R.string.text_welcome_greeting)'),
        ('Text("Tip of the Day:"', 'Text(stringResource(R.string.text_tip_of_the_day)'),
        ('Text("AI Creativity Temp:"', 'Text(stringResource(R.string.text_ai_creativity_temp)'),
        ('Text("Proactive Nudge Interval:"', 'Text(stringResource(R.string.text_proactive_nudge_interval)'),
        ('Text("Seasonal Theme:"', 'Text(stringResource(R.string.text_seasonal_theme)'),
        ('Text("${remoteConfig.proactiveNudgeIntervalHours} hours"', 'Text("${remoteConfig.proactiveNudgeIntervalHours} ${stringResource(R.string.text_hours)}"'),
        ('Text("Sync Config"', 'Text(stringResource(R.string.text_sync_config)'),
        ('Text("Test FCM Push"', 'Text(stringResource(R.string.text_test_fcm_push)')
    ],
    'app/src/main/java/com/example/presentation/screens/account/LlmSettingsSection.kt': [
        ('Text("Active",', 'Text(stringResource(R.string.text_active_caps),'),
        ('Text("Activate",', 'Text(stringResource(R.string.text_activate),'),
        ('Text("Download",', 'Text(stringResource(R.string.text_download),'),
        ('Text("Retry",', 'Text(stringResource(R.string.text_retry),')
    ],
    'app/src/main/java/com/example/presentation/screens/account/ToolsAndConnectorsSection.kt': [
        ('Text("Search tools by name, ID, or description..."', 'Text(stringResource(R.string.text_search_tools)'),
        ('Text("All (${registeredTools.size})"', 'Text("${stringResource(R.string.text_all_tools)} (${registeredTools.size})"'),
        ('Text("Category:"', 'Text(stringResource(R.string.text_category_label)'),
        ('Text("Execution Engine:"', 'Text(stringResource(R.string.text_execution_engine)'),
        ('Text("On-Device Gemma & Cloud Gemini"', 'Text(stringResource(R.string.text_execution_engine_desc)'),
        ('Text("No parameters required (Autonomous zero-arg execution)"', 'Text(stringResource(R.string.text_no_parameters_required)'),
        ('Text("Welcome Greeting:"', 'Text(stringResource(R.string.text_welcome_greeting)'),
        ('Text("Tip of the Day:"', 'Text(stringResource(R.string.text_tip_of_the_day)'),
        ('Text("AI Creativity Temp:"', 'Text(stringResource(R.string.text_ai_creativity_temp)'),
        ('Text("Proactive Nudge Interval:"', 'Text(stringResource(R.string.text_proactive_nudge_interval)'),
        ('Text("${remoteConfig.proactiveNudgeIntervalHours} hours"', 'Text("${remoteConfig.proactiveNudgeIntervalHours} ${stringResource(R.string.text_hours)}"'),
        ('Text("Seasonal Theme:"', 'Text(stringResource(R.string.text_seasonal_theme)'),
        ('Text("Sync Config"', 'Text(stringResource(R.string.text_sync_config)'),
        ('Text("Test FCM Push"', 'Text(stringResource(R.string.text_test_fcm_push)')
    ],
    'app/src/main/java/com/example/presentation/screens/account/ProfileAndPersonaSection.kt': [
        ('Text("Sign Out"', 'Text(stringResource(R.string.text_sign_out)'),
        ('Text("Sign in with Google"', 'Text(stringResource(R.string.text_sign_in_with_google)')
    ],
    'app/src/main/java/com/example/presentation/screens/onboarding/NameAndGoalStep.kt': [
        ('Text("Scan Hardware & Models"', 'Text(stringResource(R.string.text_scan_hardware_models)')
    ],
    'app/src/main/java/com/example/presentation/screens/onboarding/ModelDownloadStep.kt': [
        ('Text("Complete Setup & Start Lumi"', 'Text(stringResource(R.string.text_complete_setup_start_lumi)')
    ],
    'app/src/main/java/com/example/presentation/screens/chat/ChatDialogsAndOverlays.kt': [
        ('Text("Delete Message"', 'Text(stringResource(R.string.text_delete_message_title)'),
        ('Text("Are you sure you want to permanently delete this message?"', 'Text(stringResource(R.string.text_delete_message_confirmation)'),
        ('Text("Delete"', 'Text(stringResource(R.string.text_delete)'),
        ('Text("Vision payload analyzed by Gemini"', 'Text(stringResource(R.string.text_vision_payload_analyzed)')
    ],
    'app/src/main/java/com/example/presentation/screens/chat/ModelSelectionSheet.kt': [
        ('Text("No local models downloaded"', 'Text(stringResource(R.string.text_no_local_models)')
    ],
    'app/src/main/java/com/example/presentation/screens/WardrobeScreen.kt': [
        ('Text("Rename Companion"', 'Text(stringResource(R.string.text_rename_companion)'),
        ('Text("Companion Name"', 'Text(stringResource(R.string.text_companion_name)'),
        ('Text("Save"', 'Text(stringResource(R.string.text_save)'),
        ('Text("Cancel"', 'Text(stringResource(R.string.text_cancel)')
    ],
    'app/src/main/java/com/example/presentation/screens/ChatScreen.kt': [
        ('Text("New Messages"', 'Text(stringResource(R.string.text_new_messages)')
    ],
    'app/src/main/java/com/example/presentation/home/HomeScreen.kt': [
        ('Text("${tasks.count { !it.isCompleted }} Active"', 'Text("${tasks.count { !it.isCompleted }} ${stringResource(R.string.text_active_count)}"'),
        ('Text("${events.size} Active"', 'Text("${events.size} ${stringResource(R.string.text_active_count)}"'),
        ('Text("${reminders.size} Active"', 'Text("${reminders.size} ${stringResource(R.string.text_active_count)}")')
    ]
}

for file_path, edits in replacements.items():
    if not os.path.exists(file_path):
        continue
    with open(file_path, 'r') as f:
        content = f.read()
    
    modified = False
    for target, replacement in edits:
        if target in content:
            content = content.replace(target, replacement)
            modified = True
            
    if modified:
        # ensure stringResource import if missing
        if 'androidx.compose.ui.res.stringResource' not in content and 'import com.example.R' not in content:
            content = content.replace('import androidx.compose.ui.Modifier', 'import androidx.compose.ui.Modifier\nimport androidx.compose.ui.res.stringResource\nimport com.example.R')
        
        with open(file_path, 'w') as f:
            f.write(content)
        print(f"Updated {file_path}")
