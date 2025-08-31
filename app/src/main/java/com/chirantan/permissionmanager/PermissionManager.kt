package com.chirantan.permissionmanager

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

class PermissionManager(private val activity: ComponentActivity) {

    sealed class PermissionResult {
        object Granted : PermissionResult()
        object Denied : PermissionResult()
        object PermanentlyDenied : PermissionResult()
    }

    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var callback: ((PermissionResult) -> Unit)? = null

    init {
        permissionLauncher =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                val permission = currentPermission ?: return@registerForActivityResult
                if (isGranted) {
                    callback?.invoke(PermissionResult.Granted)
                } else {
                    val showRationale =
                        ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
                    if (showRationale) {
                        callback?.invoke(PermissionResult.Denied)
                    } else {
                        // User selected "Don't ask again"
                        callback?.invoke(PermissionResult.PermanentlyDenied)
                    }
                }
            }
    }

    private var currentPermission: String? = null

    fun requestPermission(
        permission: String,
        onResult: (PermissionResult) -> Unit
    ) {
        currentPermission = permission
        callback = onResult
        permissionLauncher.launch(permission)
    }

    fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", activity.packageName, null)
        )
        activity.startActivity(intent)
    }
}
