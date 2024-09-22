# SeonSDK: A Comprehensive Android Photo Management Library

SeonSDK is an Android library that provides a seamless way to capture, store, view, and manage photos within your app. Designed with a modern approach, the SDK uses CameraX, MVVM architecture, and Kotlin coroutines for a responsive and user-friendly experience. Key features include:

- **Capture Photos**: Easily capture photos using the device's camera with built-in error handling.
- **Biometric Authentication**: Secure access to photos with biometric authentication.
- **View and Manage Photos**: View photos in a gallery with options for full-screen viewing and easy deletion.
- **Reactive Programming**: Uses Kotlin Flows for reactive state management and asynchronous operations.
- **Extensible and Modular**: Designed with clean architecture principles, making it easy to integrate and extend.

SeonSDK helps developers quickly add robust photo management capabilities to their Android applications with minimal setup and maximum flexibility.

## Integration Guide

### Step 1: Add AAR Files to Your Project

- Copy the SeonSDK `.aar` (debug and/or release) files to your project’s `libs` folder
    - seonsdk-debug.aar
    - seonsdk-release.aar

### Step 2: Update Your AndroidManifest.xml
- Go to your project’s `AndroidManifest.xml` and add the following lines to request the necessary permissions and features:

 ```xml
 <uses-feature
     android:name="android.hardware.camera"
     android:required="false" />

 <uses-permission android:name="android.permission.CAMERA" />
 <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
 <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
 <uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
 ```

### Step 3: Add Dependencies to build.gradle.kts (App Module)
- Add the following dependencies to your app’s `build.gradle.kts` file to include required libraries:

 ```kotlin
 implementation("androidx.biometric:biometric:1.1.0")
 implementation("androidx.camera:camera-core:1.3.4")
 implementation("androidx.camera:camera-camera2:1.3.4")
 implementation("androidx.camera:camera-lifecycle:1.3.4")
 implementation("androidx.camera:camera-view:1.3.4")
 implementation("androidx.camera:camera-extensions:1.3.4")
 implementation("androidx.exifinterface:exifinterface:1.3.7")
 ```

### Step 4: Initialize the SDK in Your Application Class
- Initialize the SeonSDK inside your application class by adding the following code:

 ```kotlin
 class TestApplication : Application() {

     override fun onCreate() {
         super.onCreate()
         // Initialize PhotoSDK with the application context
         PhotoSDK.initialize(this)
     }
 }
 ```

### That’s it! SeonSDK(PhotoSDK) is now integrated into your project. 

You can start using the SDK’s powerful photo management features to enhance your Android application.


## Exposed Methods in PhotoSDK

SeonSDK(PhotoSDK) provides two main methods to interact with the camera and gallery functionalities:

### 1. `takePhoto()`
**Description**: This method launches the camera interface to capture a photo using the device’s camera. It uses a fragment-based approach, making it easy to integrate within any existing UI structure.

**Usage**:
- **Parameters**:
- `onError: (String) -> Unit`: A callback that is triggered if an error occurs during the photo capture process.
- `onSuccess: (File) -> Unit`: A callback that is triggered when the photo is successfully captured and saved.
- **Return Value**: Returns a `Fragment` (`CameraFragment`) that you can add to your UI for capturing photos.

**Example**:
```kotlin
val cameraFragment = PhotoSDK.takePhoto(
 onError = { errorMessage ->
     // Handle error (e.g., show a toast)
 },
 onSuccess = { photoFile ->
     // Handle the captured photo (e.g., display or save the file)
 }
)
```

### 2. `accessPhotos()`
**Description**: This method allows you to access the photos stored within the app’s internal storage. It includes built-in biometric authentication to ensure secure access to sensitive media files.

**Usage**:
- **Parameters**:
- `activity: FragmentActivity`: A callback that is triggered if an error occurs during the photo capture process.
- `onAuthenticated: (Fragment) -> Unit`: A callback that is triggered when the photo is successfully captured and saved.
- `onError: (AuthService.Error) -> Unit`: A callback that is triggered if authentication fails or an error occurs.

**Example**:
```kotlin
PhotoSDK.accessPhotos(
    activity = this, // Pass the current activity
    onAuthenticated = { galleryFragment ->
        // Show the gallery fragment (e.g., add it to your fragment manager)
    },
    onError = { errorMessage ->
        // Handle authentication or access error (e.g., show a toast)
    }
)
```

## Conclusion
`SeonSDK (PhotoSDK)` simplifies photo management in Android apps by offering powerful, secure, and easy-to-integrate functionalities for capturing, viewing, and managing photos. With seamless integration, modern architecture, and robust error handling, SeonSDK enhances the user experience and saves development time. Get started with SeonSDK today to bring advanced photo capabilities to your Android application effortlessly!