package com.chirantan.permissionmanager

import android.content.Intent
import android.content.pm.PackageManager
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

    private lateinit var singlePermissionLauncher: ActivityResultLauncher<String>
    private lateinit var multiplePermissionLauncher: ActivityResultLauncher<Array<String>>
    private var callback: ((PermissionResult) -> Unit)? = null

    init {
        // Single permission request
        singlePermissionLauncher =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                val permission = currentPermissions?.firstOrNull() ?: return@registerForActivityResult
                if (isGranted) {
                    deliverResult(PermissionResult.Granted)
                } else {
                    val showRationale =
                        ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
                    if (showRationale) {
                        deliverResult(PermissionResult.Denied)
                    } else {
                        deliverResult(PermissionResult.PermanentlyDenied)
                    }
                }
            }

        // Multiple permissions request
        multiplePermissionLauncher =
            activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
                val denied = results.filterValues { !it }.keys
                when {
                    denied.isEmpty() -> deliverResult(PermissionResult.Granted)
                    denied.any { !ActivityCompat.shouldShowRequestPermissionRationale(activity, it) } ->
                        deliverResult(PermissionResult.PermanentlyDenied)
                    else -> deliverResult(PermissionResult.Denied)
                }
            }
    }

    private var currentPermissions: Array<out String>? = null

    fun requestPermission(
        vararg permissions: String,
        onResult: (PermissionResult) -> Unit
    ) {
        currentPermissions = permissions
        callback = onResult

        // If all are already granted → shortcut
        val allGranted = permissions.all {
            ActivityCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            deliverResult(PermissionResult.Granted)
            return
        }

        if (permissions.size == 1) {
            singlePermissionLauncher.launch(permissions[0])
        } else {
            multiplePermissionLauncher.launch(arrayOf(*permissions)) // ✅ Corrected
        }
    }



    fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", activity.packageName, null)
        )
        activity.startActivity(intent)
    }

    private fun deliverResult(result: PermissionResult) {
        callback?.invoke(result)
        callback = null
        currentPermissions = null
    }
}
