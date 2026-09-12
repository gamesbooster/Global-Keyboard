package com.example.engine

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager

object ImeUtils {

    fun isImeEnabled(context: Context): Boolean {
        return try {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return false
            val enabledMethods = imm.enabledInputMethodList
            val packageName = context.packageName
            enabledMethods.any { it.packageName == packageName }
        } catch (e: Exception) {
            false
        }
    }

    fun isKeyboardEnabled(context: Context): Boolean = isImeEnabled(context)

    fun isImeSelected(context: Context): Boolean {
        return try {
            val currentIme = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.DEFAULT_INPUT_METHOD
            ) ?: return false
            currentIme.contains(context.packageName)
        } catch (e: Exception) {
            false
        }
    }

    fun isKeyboardSelected(context: Context): Boolean = isImeSelected(context)

    fun isKeyboardReady(context: Context): Boolean {
        return isImeEnabled(context) && isImeSelected(context)
    }

    fun openKeyboardSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showInputMethodPicker(context: Context) {
        try {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showInputMethodPicker()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
