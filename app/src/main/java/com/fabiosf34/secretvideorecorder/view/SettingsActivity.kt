package com.fabiosf34.secretvideorecorder.view

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.style.ClickableSpan
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.fabiosf34.secretvideorecorder.R
import com.fabiosf34.secretvideorecorder.databinding.ActivitySettingsBinding
import com.fabiosf34.secretvideorecorder.model.utilities.Utils
import com.fabiosf34.secretvideorecorder.model.utilities.Utils.Companion.AppLifecycleManager
import com.fabiosf34.secretvideorecorder.viewModel.SettingViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsActivity() : ProtectedBaseActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var settingsViewModel: SettingViewModel
    private var settingsInScreenRotate = Utils.SCREEN_ROTATE_DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        settingsViewModel = ViewModelProvider(this)[SettingViewModel::class.java]

        listeners()
        setPrivacyPolicyLink()
    }

    override fun onResume() {
        super.onResume()
//        settingsViewModel.hasSdCardVideoDir()
        settingsInScreenRotate = Utils.SCREEN_ROTATE_DEFAULT
        AppLifecycleManager.isSettingsActivityAdapted = false
        loadSettingsStates()
        updateSetPasswordButtonState()
        observer()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        settingsInScreenRotate = newConfig.orientation
    }

    override fun onUserLeaveHint() {
        /**
         * Faz algo quando o usuario sair da tela sem interagir com a app.
         * Ou seja, se o usuario minimizar a app por exemplo.
         */
        super.onUserLeaveHint()
        // Finaliza a activity settings.
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Seta settings adaptada para o login da app quando ela é destruida
        if (settingsInScreenRotate == Utils.SCREEN_ROTATE_DEFAULT) {
            AppLifecycleManager.isSettingsActivityAdapted = true
        }
    }


    private fun observer() {
        settingsViewModel.isStopRecordingSwitchChecked.observe(this) {
            settingsViewModel.setSwitchesColorAndState(binding.stopRecordingSwitchSet, it)
        }
        settingsViewModel.isPasswordChecked.observe(this) {
            if (settingsViewModel.getPassword().isNotEmpty()) {
                settingsViewModel.setSwitchesColorAndState(binding.passwordSwitchSet, it)
            } else {
                binding.passwordSwitchSet.isChecked = false
                settingsViewModel.setSwitchesColorAndState(binding.passwordSwitchSet, false)
            }
        }

        settingsViewModel.appPassword.observe(this) {
            if (it.isEmpty()) binding.passwordSwitchSet.isChecked = false
        }

        settingsViewModel.videoQuality.observe(this){
            when (it) {
                Utils.VIDEO_QUALITY_LOW -> binding.videoQualityLowRadioSet.isChecked = true
                Utils.VIDEO_QUALITY_MEDIUM -> binding.videoQualityMediumRadioSet.isChecked = true
                Utils.VIDEO_QUALITY_HIGH -> binding.videoQualityHighRadioSet.isChecked = true
            }
        }

        settingsViewModel.isShowCompletedNotification.observe(this) {
            settingsViewModel.setSwitchesColorAndState(binding.showNotifySwitchSet, it)
        }
        settingsViewModel.isAppNameSwitched.observe(this) {
            settingsViewModel.setSwitchesColorAndState(binding.appNameSwitchSet, it)
        }

        settingsViewModel.isRecording.observe(this) {
            if (it) {
                binding.appNameSwitchSet.visibility = View.GONE
                binding.appNameSwitchSetMsg.visibility = View.GONE
            } else {
                binding.appNameSwitchSet.visibility = View.VISIBLE
                binding.appNameSwitchSetMsg.visibility = View.VISIBLE
            }
        }

        settingsViewModel.hasSdCardVideoDir.observe(this) {
            if (it) binding.sdCardDirSwitchSet.visibility = View.VISIBLE
            else {
                binding.sdCardDirSwitchSet.visibility = View.GONE
            }
        }

        settingsViewModel.isSdCardDirSwitchChecked.observe(this) {
            settingsViewModel.setSwitchesColorAndState(binding.sdCardDirSwitchSet, it)
        }
        settingsViewModel.startRecordingOnBoot.observe(this) {
            settingsViewModel.setSwitchesColorAndState(binding.startRecordingOnBootSwitchSet, it)
        }
    }

    private fun listeners() {

        supportFragmentManager.addOnBackStackChangedListener {
            updateSetPasswordButtonState()
        }

        binding.stopRecordingSwitchSet.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.saveStopRecordingSwitchState(isChecked)
            binding.stopRecordingSwitchSet.setOnClickListener {
                if (isChecked) {
                    Toast.makeText(this, R.string.stop_recording_in_autorotate_on, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, R.string.stop_recording_in_autorotate_off, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.showNotifySwitchSet.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.saveShowCompletedNotification(isChecked)
            binding.showNotifySwitchSet.setOnClickListener {
                if (isChecked) {
                    Toast.makeText(this, R.string.show_notification_on, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, R.string.show_notification_off, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.passwordSwitchSet.setOnCheckedChangeListener { _, isChecked ->
            if (settingsViewModel.getPassword().isNotEmpty()) {
                settingsViewModel.enablePasswdApp(isChecked)
                binding.passwordSwitchSet.setOnClickListener {
                    if (isChecked) {
                        Toast.makeText(this, R.string.password_enabled, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, R.string.password_disabled, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else if (settingsViewModel.getPassword().isEmpty()) {
                Toast.makeText(
                    this,
                    R.string.no_password_set,
                    Toast.LENGTH_SHORT
                ).show()
                binding.btnSetPasswordSettings.performClick()
            }
            settingsViewModel.loadSettingsPreferences()
        }

        binding.btnSetPasswordSettings.setOnClickListener {
            loadFragment(FormPasswordFragment())
        }

        binding.videoQualityLowRadioSet.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) settingsViewModel.saveVideoQuality(Utils.VIDEO_QUALITY_LOW)
        }
        binding.videoQualityMediumRadioSet.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) settingsViewModel.saveVideoQuality(Utils.VIDEO_QUALITY_MEDIUM)
        }
        binding.videoQualityHighRadioSet.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) settingsViewModel.saveVideoQuality(Utils.VIDEO_QUALITY_HIGH)
        }

        binding.appNameSwitchSet.setOnClickListener {

            val messageId: Int =
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) R.string.app_name_discreet_switch_alert_msg
                else R.string.app_name_discreet_switch_msg_old

            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.app_name_discreet_switch_alert_text)
                .setMessage(messageId)
                .setPositiveButton(R.string.ok) { _, _ ->
                    val toast =
                        Toast.makeText(this, R.string.app_name_switch_on_msg, Toast.LENGTH_LONG)
                    toast.setGravity(android.view.Gravity.BOTTOM, 0, 0)
                    toast.show()
                    val isChecked = binding.appNameSwitchSet.isChecked
                    if (isChecked) {
                        settingsViewModel.setAppLauncherAlias(
                            Utils.APP_NAME_DISCREET_ALIAS,
                            Utils.APP_NAME_ALIAS
                        )
                    } else {
                        settingsViewModel.setAppLauncherAlias(
                            Utils.Companion.APP_NAME_ALIAS,
                            Utils.Companion.APP_NAME_DISCREET_ALIAS
                        )
                    }
                    settingsViewModel.saveAppNameSwitchState(isChecked)
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    binding.appNameSwitchSet.isChecked = !binding.appNameSwitchSet.isChecked
                    Toast.makeText(this, R.string.app_name_switch_canceled, Toast.LENGTH_SHORT)
                        .show()
                }
                .show()
        }

        binding.appNameSwitchSet.setOnCheckedChangeListener { _, isChecked ->

        }

        binding.sdCardDirSwitchSet.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.saveSdCardDirSwitchState(isChecked)
            binding.sdCardDirSwitchSet.setOnClickListener {
                if (isChecked) {
                    Toast.makeText(this, R.string.save_in_sd_card_dir_on, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, R.string.save_in_sd_card_dir_off, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.startRecordingOnBootSwitchSet.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.saveStartRecordingOnBoot(isChecked)
            binding.startRecordingOnBootSwitchSet.setOnClickListener {
                if (isChecked) {
                    Toast.makeText(this, R.string.start_recording_on_boot_on, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, R.string.start_recording_on_boot_off, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setPrivacyPolicyLink() {
        settingsViewModel.setPrivacyPolicyLink(
            binding.privacyPolicyLink,
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val intent = Intent(Intent.ACTION_VIEW, Utils.PRIVACY_POLICY_URL.toUri())
                    // Verifica se há um navegador para lidar com a intent
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                    } else {
                        // Trate o caso em que nenhum navegador está disponível
                        // (por exemplo, mostre um Toast)
                    }
                }
                // Opcional: para remover o sublinhado padrão do link
                // override fun updateDrawState(ds: TextPaint) {
                //     super.updateDrawState(ds)
                //     ds.isUnderlineText = false
                // }
            }
        )
    }

    fun loadSettingsStates() {
        settingsViewModel.loadSettingsPreferences()
        //
    }

    private fun loadFragment(fragment: Fragment) {
        if (!fragment.isAdded) {
            // Obtém o FragmentManager para gerenciar fragments nesta activity
            val fragmentManager = supportFragmentManager

            // Inicia uma FragmentTransaction
            val fragmentTransaction = fragmentManager.beginTransaction()

            // Substitui o contêiner existente pelo novo fragment
            // Use add() se quiser adicionar em cima de algo que já está no contêiner
            fragmentTransaction.add(R.id.fragment_form_password_container, fragment)

//         Opcional: Adiciona a transação à back stack.
//         Isso permite que o usuário volte para a activity sem o fragment
//         pressionando o botão "Voltar".
            fragmentTransaction.addToBackStack(null)

            // Confirma a transação. As operações são executadas assincronamente.
            fragmentTransaction.commit()
        } else {
            Toast.makeText(this, R.string.frag_is_added, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSetPasswordButtonState() {
        // Verifica se o Fragment está visível (ou seja, se a back stack não está vazia
        // E o container do fragment tem um fragment)
        // Uma verificação mais robusta é ver se o fragment específico está visível.
        // Se você só tem um tipo de fragment nesse container, verificar se o container
        // está ocupado é suficiente.
        val fragmentInContainer =
            supportFragmentManager.findFragmentById(R.id.fragment_form_password_container)
        if (fragmentInContainer != null) {
            binding.passwordSwitchSet.isClickable = false
            binding.passwordSwitchSet.isEnabled = false
            binding.btnSetPasswordSettings.isClickable = false
            binding.btnSetPasswordSettings.isEnabled = false // Também desabilita visualmente
            // Opcional: mudar a aparência do botão para indicar que está desabilitado
            binding.btnSetPasswordSettings.alpha = 0.5f
        } else {
            binding.passwordSwitchSet.isClickable = true
            binding.passwordSwitchSet.isEnabled = true
            binding.btnSetPasswordSettings.isClickable = true
            binding.btnSetPasswordSettings.isEnabled = true
            binding.btnSetPasswordSettings.alpha = 1.0f
        }

    }

}