package com.example.seonsdk.services

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * AuthService handles biometric authentication for the SeonSDK. It utilizes the Android BiometricPrompt API
 * to authenticate users using the device's biometric capabilities (e.g., fingerprint, face recognition).
 *
 * The class follows an asynchronous approach using Kotlin Coroutines and Flows to provide a reactive way
 * to manage authentication states and results.
 *
 * @param activity The FragmentActivity required for displaying biometric prompts.
 */
class AuthService(private val activity: FragmentActivity) {

    // Executor used to run the BiometricPrompt callbacks on the main thread.
    private val executor = ContextCompat.getMainExecutor(activity)

    /**
     * Starts the authentication process using biometric authentication.
     *
     * This method returns a Flow that emits the result of the authentication process.
     * It uses a suspend function internally to handle the authentication process asynchronously.
     *
     * @return A Flow emitting the Result of the authentication attempt.
     */
    fun authenticate(): Flow<Result> = flow {
        // Call the suspend function to perform authentication and emit the result.
        val result = authenticateBiometric()
        emit(result) // Emit the result to the Flow collector.
    }

    /**
     * Handles the biometric authentication process asynchronously using Kotlin Coroutines.
     *
     * This method suspends until the authentication is complete, providing a reactive
     * and cancellable authentication flow.
     *
     * @return A Result indicating the outcome of the authentication attempt (Success, Failed, or Error).
     */
    private suspend fun authenticateBiometric(): Result = suspendCancellableCoroutine { continuation ->
        // Configure the BiometricPrompt with necessary UI elements and behavior.
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication") // Title shown on the biometric prompt.
            .setSubtitle("Authenticate to access your photos") // Subtitle shown on the prompt.
            .setNegativeButtonText("Cancel") // Text for the negative/cancel button.
            .build()

        // Initialize BiometricPrompt with the activity context, executor, and callback handlers.
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                // Called when the authentication is successful.
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    continuation.resume(Result.Success) // Resume coroutine with Success result.
                }

                // Called when the biometric authentication fails (e.g., unmatched fingerprint).
                override fun onAuthenticationFailed() {
                    continuation.resume(Result.Failed) // Resume coroutine with Failed result.
                }

                // Called when an error occurs during authentication (e.g., user cancellation).
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    continuation.resume(Result.Error(Error.AuthFailure(Exception(errString.toString()))))
                }
            }
        )

        // Start the authentication process by showing the prompt to the user.
        biometricPrompt.authenticate(promptInfo)

        // Handle cancellation of the coroutine; cancels biometric prompt if the operation is cancelled.
        continuation.invokeOnCancellation {
            biometricPrompt.cancelAuthentication()
        }
    }

    /**
     * Represents the result of the authentication attempt. This sealed class categorizes
     * the possible outcomes of biometric authentication:
     * - Success: Authentication was successful.
     * - Failed: Authentication was unsuccessful but without critical errors.
     * - Error: An error occurred during authentication (e.g., user cancelled or system error).
     */
    sealed class Result {
        // Authentication succeeded.
        data object Success : Result()

        // Authentication failed due to user failing biometric check.
        data object Failed : Result()

        // An error occurred during authentication, with a specific exception wrapped.
        data class Error(val e: AuthService.Error) : Result()
    }

    /**
     * Represents errors that can occur during authentication. Extends Throwable for error handling purposes.
     * - AuthFailure: Specific error related to the failure of authentication, wrapping an exception.
     */
    sealed class Error(e: Exception) : Throwable(e.message) {
        data class AuthFailure(val e: Exception) : Error(e)
    }
}
