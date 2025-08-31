package com.chirantan.permissionmanager

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

class PermissionManager private constructor(
    private val activity: Activity,
    private val builder: Builder
) {
    private lateinit var launcher: ActivityResultLauncher<Array<String>>

    fun attachLauncher(launcher: ActivityResultLauncher<Array<String>>) {
        this.launcher = launcher
    }

    fun request() {
        launcher.launch(builder.permissions.toTypedArray())
    }

    fun handleResult(result: Map<String, Boolean>) {
        val denied = result.filter { !it.value }.map { it.key }

        when {
            denied.isEmpty() -> builder.onGranted?.invoke()
            denied.any { !activity.shouldShowRequestPermissionRationale(it) } ->
                builder.onPermanentlyDenied?.invoke(denied)
            else -> {
                if (builder.rationaleMessage != null) {
                    showRationaleDialog(denied)
                } else {
                    builder.onDenied?.invoke(denied)
                }
            }
        }
    }

    private fun showRationaleDialog(denied: List<String>) {
        AlertDialog.Builder(activity)
            .setTitle(builder.rationaleTitle ?: "Permission Required")
            .setMessage(builder.rationaleMessage ?: "We need this permission for proper functionality.")
            .setPositiveButton("Allow") { _, _ ->
                launcher.launch(denied.toTypedArray()) // re-request
            }
            .setNegativeButton("Cancel") { _, _ ->
                builder.onDenied?.invoke(denied)
            }
            .show()
    }

    // --- DSL Builder ---
    class Builder {
        internal val permissions = mutableListOf<String>()
        internal var onGranted: (() -> Unit)? = null
        internal var onDenied: ((List<String>) -> Unit)? = null
        internal var onPermanentlyDenied: ((List<String>) -> Unit)? = null
        internal var rationaleTitle: String? = null
        internal var rationaleMessage: String? = null

        fun permissions(vararg perms: String) {
            permissions.addAll(perms)
        }

        fun onGranted(block: () -> Unit) {
            onGranted = block
        }

        fun onDenied(block: (List<String>) -> Unit) {
            onDenied = block
        }

        fun onPermanentlyDenied(block: (List<String>) -> Unit) {
            onPermanentlyDenied = block
        }

        fun rationale(title: String, message: String) {
            rationaleTitle = title
            rationaleMessage = message
        }
    }

    companion object {
        fun request(activity: FragmentActivity, block: Builder.() -> Unit) {
            val builder = Builder().apply(block)
            val pm = PermissionManager(activity, builder)

            val launcher = activity.activityResultRegistry.register(
                "permissions_${System.currentTimeMillis()}",
                ActivityResultContracts.RequestMultiplePermissions()
            ) { result -> pm.handleResult(result) }

            pm.attachLauncher(launcher)
            pm.request()
        }

        fun request(fragment: Fragment, block: Builder.() -> Unit) {
            val builder = Builder().apply(block)
            val pm = PermissionManager(fragment.requireActivity(), builder)

            val launcher = fragment.registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { result -> pm.handleResult(result) }

            pm.attachLauncher(launcher)
            pm.request()
        }

        fun openAppSettings(activity: Activity) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activity.packageName, null)
            }
            activity.startActivity(intent)
        }
    }
}
