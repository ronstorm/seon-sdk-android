# SeonSDK (Android)

### Capture, Store, and View Photos with Ease 📸

**SeonSDK** is a lightweight, easy-to-integrate library for Android that provides seamless functionalities to capture photos, store them securely, and view them in a beautifully designed gallery. With built-in biometric authentication, you can ensure that only authorized users can access stored photos.

## Objective

The objective of **SeonSDK** is to provide a simplified and unified interface for photo management within Android applications, allowing developers to quickly integrate camera functionalities, secure photo storage, and access with user authentication using the device's built-in biometric capabilities.

## How to Integrate in Your Application

To integrate **SeonSDK** into your Android application, follow these simple steps:

1. **Add AAR Files to Your Project**: 
   - Download the [SeonSDK Libraries](https://github.com/ronstorm/seonsdk-libraries/tree/master/Android) and place them in your project’s `libs` folder.

2. **Update `AndroidManifest.xml` for Camera and Media Permissions**:
   - Add the following lines to request permissions for camera and media access:
     ```xml
     <uses-feature
         android:name="android.hardware.camera"
         android:required="false" />

     <uses-permission android:name="android.permission.CAMERA" />
     <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
     <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
     <uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
     ```

3. **Add Dependencies to `build.gradle.kts` (App Module)**:
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

4. **Initialize the SDK in Your Application Class**:
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

## Usage

Below is a quick guide on how to use **SeonSDK** in your application:

```kotlin
// Import the SDK
import com.example.seonsdk.main.PhotoSDK

// Use the takePhoto() method to capture photos
val cameraFragment = PhotoSDK.takePhoto(
    onError = { errorMessage ->
        // Handle error (e.g., show a toast)
        println("Error capturing photo: $errorMessage")
    },
    onSuccess = { photoFile ->
        // Handle the captured photo (e.g., display or save the file)
        println("Photo captured: ${photoFile.path}")
    }
)

// Use the accessPhotos() method to access photos securely
PhotoSDK.accessPhotos(
    activity = this, // Pass the current activity
    onAuthenticated = { galleryFragment ->
        // Display the gallery fragment
        println("Gallery accessed successfully.")
    },
    onError = { errorMessage ->
        // Handle authentication or access error (e.g., show a toast)
        println("Error accessing gallery: $errorMessage")
    }
)
```

## Sample Application

To see **SeonSDK** in action, check out the [sample application](https://github.com/ronstorm/seon-test-app-android) that demonstrates the full capabilities of the framework. This sample app shows how to integrate and utilize the SDK for photo capture, storage, and gallery access.


## Conclusion

**SeonSDK** aims to simplify photo management in Android applications, providing a clean and intuitive interface for developers. We welcome feedback and contributions to help improve the library. If you encounter any issues or have suggestions, feel free to open an issue or submit a pull request on GitHub.

Thank you for using **SeonSDK**! 🚀