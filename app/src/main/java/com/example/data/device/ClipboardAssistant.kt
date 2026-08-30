package com.example.data.device

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * System Clipboard listener that detects when user copies text or URLs
 * to offer instant floating actions (summarize, extract task, schedule meeting).
 */
class ClipboardAssistant(private val context: Context) {

    private val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    private val _latestCopiedSnippet = MutableStateFlow<String?>(null)
    val latestCopiedSnippet: StateFlow<String?> = _latestCopiedSnippet.asStateFlow()

    private var onClipListener: ((String) -> Unit)? = null

    private val primaryClipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
        checkClipboard()
    }

    fun startListening(onNewClip: ((String) -> Unit)? = null) {
        this.onClipListener = onNewClip
        clipboard?.addPrimaryClipChangedListener(primaryClipChangedListener)
    }

    fun stopListening() {
        clipboard?.removePrimaryClipChangedListener(primaryClipChangedListener)
    }

    private fun checkClipboard() {
        try {
            if (clipboard?.hasPrimaryClip() == true) {
                val clipData = clipboard.primaryClip
                if (clipData != null && clipData.itemCount > 0) {
                    val item = clipData.getItemAt(0)
                    val text = item.text?.toString()?.trim()
                    if (!text.isNullOrBlank() && text.length > 3 && text != _latestCopiedSnippet.value) {
                        _latestCopiedSnippet.value = text
                        onClipListener?.invoke(text)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignored on restricted platforms
        }
    }

    fun copyToClipboard(label: String, text: String) {
        val clip = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
    }

    fun clearSnippet() {
        _latestCopiedSnippet.value = null
    }
}
