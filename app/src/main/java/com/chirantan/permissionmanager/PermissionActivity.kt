package com.chirantan.permissionmanager

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import android.Manifest
import android.os.Build
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.chirantan.permissionmanager.databinding.ActivityPermissionBinding
import com.google.android.material.snackbar.Snackbar

class PermissionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPermissionBinding
    private lateinit var permissionManager: PermissionManager
    private var cameraImageUri: Uri? = null
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize permission manager
        permissionManager = PermissionManager(this)

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                binding.imageView.setImageURI(cameraImageUri)
            }
        }

        // Gallery launcher
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                binding.imageView.setImageURI(it)
            }
        }

        binding.btnOpenCamera.setOnClickListener {
            permissionManager.requestPermission(Manifest.permission.CAMERA) { result ->
                when (result) {
                    is PermissionManager.PermissionResult.Granted -> openCamera()
                    is PermissionManager.PermissionResult.Denied ->
                        Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                    is PermissionManager.PermissionResult.PermanentlyDenied -> {
                        Snackbar.make(binding.root, "Camera permission permanently denied", Snackbar.LENGTH_LONG)
                            .setAction("Settings") {
                                permissionManager.openAppSettings()
                            }.show()
                    }
                }
            }
        }


        binding.btnOpenGallery.setOnClickListener {
            val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

            permissionManager.requestPermission(storagePermission) { result ->
                when (result) {
                    is PermissionManager.PermissionResult.Granted -> openGallery()
                    is PermissionManager.PermissionResult.Denied ->
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    is PermissionManager.PermissionResult.PermanentlyDenied -> {
                        Snackbar.make(binding.root, "Permission permanently denied", Snackbar.LENGTH_LONG)
                            .setAction("Settings") {
                                permissionManager.openAppSettings()
                            }.show()
                    }
                }
            }
        }

    }

    private fun openCamera() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
        }
        cameraImageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        cameraImageUri?.let { cameraLauncher.launch(it) }
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }
}