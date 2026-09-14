import xml.etree.ElementTree as ET

def add_string(root, name, value):
    # Check if exists
    for child in root.findall('string'):
        if child.get('name') == name:
            return
    elem = ET.SubElement(root, 'string', name=name)
    elem.text = value

tree = ET.parse('app/src/main/res/values/strings.xml')
root = tree.getroot()

strings_to_add = {
    'text_sync_config': 'Sync Config',
    'text_test_fcm_push': 'Test FCM Push',
    'text_welcome_greeting': 'Welcome Greeting:',
    'text_tip_of_the_day': 'Tip of the Day:',
    'text_ai_creativity_temp': 'AI Creativity Temp:',
    'text_proactive_nudge_interval': 'Proactive Nudge Interval:',
    'text_seasonal_theme': 'Seasonal Theme:',
    'text_hours': 'hours',
    'text_active_caps': 'Active',
    'text_activate': 'Activate',
    'text_download': 'Download',
    'text_retry': 'Retry',
    'text_search_tools': 'Search tools by name, ID, or description...',
    'text_all_tools': 'All',
    'text_category_label': 'Category:',
    'text_execution_engine': 'Execution Engine:',
    'text_execution_engine_desc': 'On-Device Gemma & Cloud Gemini',
    'text_no_parameters_required': 'No parameters required (Autonomous zero-arg execution)',
    'text_sign_out': 'Sign Out',
    'text_sign_in_with_google': 'Sign in with Google',
    'text_scan_hardware_models': 'Scan Hardware & Models',
    'text_complete_setup_start_lumi': 'Complete Setup & Start Lumi',
    'text_delete_message_title': 'Delete Message',
    'text_delete_message_confirmation': 'Are you sure you want to permanently delete this message?',
    'text_delete': 'Delete',
    'text_vision_payload_analyzed': 'Vision payload analyzed by Gemini',
    'text_no_local_models': 'No local models downloaded',
    'text_rename_companion': 'Rename Companion',
    'text_companion_name': 'Companion Name',
    'text_save': 'Save',
    'text_cancel': 'Cancel',
    'text_new_messages': 'New Messages',
    'text_active_count': 'Active'
}

for k, v in strings_to_add.items():
    add_string(root, k, v)

ET.indent(tree, space="    ", level=0)
tree.write('app/src/main/res/values/strings.xml', encoding='utf-8', xml_declaration=True)
