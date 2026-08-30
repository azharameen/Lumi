import re

with open("app/src/main/java/com/example/ui/components/OverlayPermissionDialog.kt", "r") as f:
    content = f.read()

old_confirm_btn = """        confirmButton = {
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = LumiCyan, contentColor = ObsidianDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Open Settings", fontWeight = FontWeight.Bold)
            }
        },"""

new_confirm_btn = """        confirmButton = {
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    } else {
                        onGranted()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LumiCyan, contentColor = ObsidianDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Continue", fontWeight = FontWeight.Bold)
            }
        },"""

content = content.replace(old_confirm_btn, new_confirm_btn)

with open("app/src/main/java/com/example/ui/components/OverlayPermissionDialog.kt", "w") as f:
    f.write(content)
