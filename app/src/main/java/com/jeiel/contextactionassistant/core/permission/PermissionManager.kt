package com.jeiel.contextactionassistant.core.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun requiredRuntimePermissions(): List<String> {
        val result = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= 33) {
            result += Manifest.permission.POST_NOTIFICATIONS
            result += Manifest.permission.READ_MEDIA_IMAGES
        } else {
            result += Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return result
    }

    fun missingRuntimePermissions(): List<String> {
        return requiredRuntimePermissions().filterNot { isGranted(it) }
    }

    fun isGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
