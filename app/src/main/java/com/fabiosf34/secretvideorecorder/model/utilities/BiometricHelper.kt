package com.fabiosf34.secretvideorecorder.model.utilities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.fabiosf34.secretvideorecorder.R
import com.fabiosf34.secretvideorecorder.model.repository.Preferences
import com.fabiosf34.secretvideorecorder.view.CamActivity

class BiometricHelper {
    companion object {
        fun isBiometricAvailable(context: Context): Boolean {
            val biometricManager = BiometricManager.from(context)
            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                BiometricManager.BIOMETRIC_SUCCESS -> return true
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> return false
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> return false
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> return false
                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                }

                BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                    return false
                }

                BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                    return false
                }
            }
            return false
        }

        //        fun biometricFragment(fragment: Fragment) {
//            val context = fragment.context
//            val executor = context?.let { ContextCompat.getMainExecutor(it.applicationContext) }
//
//            val bio = executor?.let {
//                BiometricPrompt(fragment, it, object : BiometricPrompt.AuthenticationCallback() {
//                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
//                        super.onAuthenticationSucceeded(result)
//                        context.startActivity(Intent(context, CamActivity::class.java))
//                        fragment.activity?.finish()
//                    }
//
//                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
//                        super.onAuthenticationError(errorCode, errString)
//                        Toast.makeText(
//                            context,
//                            R.string.fingerprint_auth_cancelled,
//                            Toast.LENGTH_SHORT
//                        ).show()
////                    activity?.finish()
//                    }
//                })
//            }
//
//            val info = BiometricPrompt.PromptInfo.Builder()
//                .setTitle(fragment.getString(R.string.biometric_dialog_title))
//                .setDescription(fragment.getString(R.string.biometric_dialog_description))
//                .setNegativeButtonText(fragment.getString(R.string.biometric_dialog_button_use_password))
//                .build()
//
//
//            bio?.authenticate(info)
//
//        }
        fun biometricActivitySuccessful(context: Context) {
            val preferences = Preferences(context)
            val executor = ContextCompat.getMainExecutor(context.applicationContext)

            val bio = BiometricPrompt(
                context as FragmentActivity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Toast.makeText(
                            context,
                            errString,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        val intent = Intent(context, CamActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        context.startActivity(intent)
                        preferences.store(Utils.PASSWORD.BIOMETRIC_SUCCESS, true)
                        (context as Activity).finish()
//                    context.finish()
                        Log.d(
                            "Biometric",
                            "Authentication succeeded: ${
                                preferences.retrieve(
                                    Utils.PASSWORD.BIOMETRIC_SUCCESS,
                                    false
                                )
                            }"
                        )
                    }
                })

            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle(context.getString(R.string.biometric_dialog_title))
                .setDescription(context.getString(R.string.biometric_dialog_description))
                .setNegativeButtonText(context.getString(R.string.biometric_dialog_button_use_password))
                .build()

            bio.authenticate(info)


        }
    }

}