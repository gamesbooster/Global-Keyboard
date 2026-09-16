package com.example.data

import android.content.Context
import com.example.model.ClipboardItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class ClipboardRepository(context: Context) {
    private val prefs = context.getSharedPreferences("lingokey_clipboard_prefs", Context.MODE_PRIVATE)
    private val _items = MutableStateFlow<List<ClipboardItem>>(emptyList())
    val items: StateFlow<List<ClipboardItem>> = _items.asStateFlow()

    private val _latestClip = MutableStateFlow<String?>(null)
    val latestClip: StateFlow<String?> = _latestClip.asStateFlow()

    init {
        loadClips()
    }

    private fun loadClips() {
        val raw = prefs.getString("saved_clips", null)
        if (raw.isNullOrBlank()) {
            _items.value = listOf(
                ClipboardItem(1, "Welcome to LingoKey AI Keyboard!", System.currentTimeMillis(), isPinned = true),
                ClipboardItem(2, "Type. Translate. Speak. Create.", System.currentTimeMillis(), isPinned = false)
            )
            saveClips()
            return
        }
        try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<ClipboardItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    ClipboardItem(
                        id = obj.optLong("id", System.currentTimeMillis()),
                        text = obj.getString("text"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        isPinned = obj.optBoolean("isPinned", false)
                    )
                )
            }
            _items.value = list
        } catch (e: Exception) {
            _items.value = emptyList()
        }
    }

    private fun saveClips() {
        try {
            val jsonArray = JSONArray()
            for (item in _items.value) {
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("text", item.text)
                obj.put("timestamp", item.timestamp)
                obj.put("isPinned", item.isPinned)
                jsonArray.put(obj)
            }
            prefs.edit().putString("saved_clips", jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addClip(text: String) {
        if (text.isBlank()) return
        val current = _items.value.toMutableList()
        // Remove if duplicate text exists
        current.removeAll { it.text == text && !it.isPinned }
        current.add(0, ClipboardItem(text = text.trim()))
        // Keep max 50 items, preserving pinned
        val pinned = current.filter { it.isPinned }
        val unpinned = current.filter { !it.isPinned }.take(50)
        _items.value = pinned + unpinned
        _latestClip.value = text.trim()
        saveClips()
    }

    /**
     * Safely receives clipboard item from Android system IME clipboard listener.
     * Compliant with Google Play policies: strictly stored locally, never transmitted externally.
     */
    fun addSystemClip(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        val firstExisting = _items.value.firstOrNull()?.text
        if (firstExisting != trimmed) {
            addClip(trimmed)
        } else {
            _latestClip.value = trimmed
        }
    }

    fun dismissLatestClip() {
        _latestClip.value = null
    }

    fun togglePin(id: Long) {
        _items.value = _items.value.map {
            if (it.id == id) it.copy(isPinned = !it.isPinned) else it
        }
        saveClips()
    }

    fun deleteClip(id: Long) {
        _items.value = _items.value.filter { it.id != id }
        saveClips()
    }

    fun clearAll(keepPinned: Boolean = true) {
        _items.value = if (keepPinned) _items.value.filter { it.isPinned } else emptyList()
        saveClips()
    }

    companion object {
        @Volatile
        private var instance: ClipboardRepository? = null

        fun getInstance(context: Context): ClipboardRepository {
            return instance ?: synchronized(this) {
                instance ?: ClipboardRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
