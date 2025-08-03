package com.fabiosf34.secretvideorecorder.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.fabiosf34.secretvideorecorder.R
import com.fabiosf34.secretvideorecorder.databinding.ActivityLoginBinding
import com.fabiosf34.secretvideorecorder.model.utilities.Utils
import com.fabiosf34.secretvideorecorder.model.utilities.Utils.Companion.AppLifecycleManager
import com.fabiosf34.secretvideorecorder.viewModel.SettingViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: SettingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loginViewModel = ViewModelProvider(this)[SettingViewModel::class.java]

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        listeners()

    }

    override fun onResume() {
        super.onResume()
        Log.d("LoginActivity", "onResume: LoginActivity")
        loginViewModel.loadSettingsPreferences()
        loginViewModel.checkBiometricDisposable()
        observer()
    }

    fun listeners() {
        binding.btnEnter.setOnClickListener {

            if (binding.passwordEditText.text.toString() == loginViewModel.getPassword()
            ) {
                AppLifecycleManager.isLoginActivityLaunched = true
                val intent = Intent(
                    this,
                    CamActivity::class.java
                )
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, R.string.invalid_password_text, Toast.LENGTH_SHORT).show()
            }
        }
        binding.biometricSwitch.setOnCheckedChangeListener { _, isChecked ->
            loginViewModel.saveBiometricSwitchState(isChecked)
            if (loginViewModel.isPasswordEnabled()) {
                loginViewModel.biometric(this, isChecked)
            }
        }
    }

    fun observer() {
        loginViewModel.isPasswordChecked.observe(this) {
            if (!it) {
                val intent = Intent(applicationContext, CamActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            }
        }
        loginViewModel.isBiometricChecked.observe(this) {
            Utils.setSwitchesColorAndState(this, binding.biometricSwitch, it)
        }

        loginViewModel.isBiometricAvailable.observe(this) {
            binding.biometricSwitch.isVisible = it
        }

        loginViewModel.isRecording.observe(this) {
        }
    }
}