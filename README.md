# PermissionManager Demo App
A simple Android demo app that shows how to **handle runtime permissions** easily in Android (XML-based layouts) using a custom `PermissionManager` class.  

It supports 
- Camera permission  
- Storage permission (including Android 13+ storage permission changes)  
- Handles denied and “Don’t ask again” cases with **Settings redirect**  

---

## Features

- Minimal boilerplate for permission handling.  
- Works with **ActivityResultLauncher** (modern API).  
- Shows how to **open camera and gallery** and display the selected/captured image in an `ImageView`.  
- Handles **permanent denial** with a Snackbar redirect to app settings.  

---

## Overview: PermissionManager
`PermissionManager` is a **helper class** that simplifies runtime permission handling in Android.  

**Key features:**
- Works with **any runtime permission** (Camera, Storage, Location, Microphone, etc.)  
- Handles three cases automatically:  
  1. **Granted** → executes your action  
  2. **Denied** → user denied but can ask again  
  3. **PermanentlyDenied** → user selected “Don’t ask again”; shows a **Snackbar** with **Settings redirect**  
- Uses **ActivityResultLauncher** internally to avoid deprecated `onActivityResult`  
- Reduces boilerplate code for permission checks and handling  

---

## How to Use `PermissionManager` in Activity
### 1. Initialize `PermissionManager`
```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionManager = PermissionManager(this)
    }
}
```
### 2. Open Camera with Permission Handling
```kotlin
btnOpenCamera.setOnClickListener {
    permissionManager.requestPermission(Manifest.permission.CAMERA) { result ->
        when (result) {
            is PermissionManager.PermissionResult.Granted -> openCamera()
            is PermissionManager.PermissionResult.Denied ->
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            is PermissionManager.PermissionResult.PermanentlyDenied ->
                permissionManager.openAppSettings()
        }
    }
}

private fun openCamera() {
    // Launch camera using ActivityResultLauncher or your preferred method
}
```
### 3. Handle multiple permissions
```kotlin
btnOpenGallery.setOnClickListener {
    val storagePermission =
        if (Build.VERSION.SbtnRequestLocation.setOnClickListener {
            permissionManager.requestPermission(
              Manifest.permission.ACCESS_FINE_LOCATION,
              Manifest.permission.ACCESS_COARSE_LOCATION
            ) { result ->
              when (result) {
                is PermissionManager.PermissionResult.Granted -> {
                  Toast.makeText(this, "Location permissions granted", Toast.LENGTH_SHORT).show()
                  startLocationUpdates()
                }
                is PermissionManager.PermissionResult.Denied ->
                  Toast.makeText(this, "Location permissions denied", Toast.LENGTH_SHORT).show()
                is PermissionManager.PermissionResult.PermanentlyDenied ->
                  permissionManager.openAppSettings()
              }
            }
          }
}


```

---

## Notes
 
- PermissionManager works for any runtime permission (camera, storage, location, microphone, etc.)
- Automatically handles denied and permanently denied permissions
- Integrates easily with ActivityResultLauncher for camera or gallery operations

---
