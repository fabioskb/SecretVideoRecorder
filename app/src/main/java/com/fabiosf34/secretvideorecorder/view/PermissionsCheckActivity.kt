package com.fabiosf34.secretvideorecorder.view

// Importe suas strings de R.string
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.fabiosf34.secretvideorecorder.R
import com.fabiosf34.secretvideorecorder.databinding.ActivityPermissionsCheckBinding
import com.fabiosf34.secretvideorecorder.model.utilities.Utils
import com.fabiosf34.secretvideorecorder.viewModel.SettingViewModel
// TODO: Implementar na produção
//import com.google.android.gms.ads.MobileAds

// Remova a dependência do CamViewModel aqui se ele é apenas para checar permissões,
// a Activity pode fazer isso diretamente. Se o ViewModel tiver mais lógica de permissão, mantenha.

class PermissionsCheckActivity : AppCompatActivity() {
    private lateinit var permissionsCheckViewModel: SettingViewModel

    // Defina as permissões necessárias aqui
    private val requiredPermissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS
        ) else arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            arrayOf(
//                Manifest.permission.CAMERA,
//                Manifest.permission.RECORD_AUDIO,
//                Manifest.permission.POST_NOTIFICATIONS,
//                // Adicione outras que seu app precisa:
////                Manifest.permission.READ_MEDIA_VIDEO,
//            )
//        } else {
//            mutableListOf(
//                Manifest.permission.CAMERA,
//                Manifest.permission.RECORD_AUDIO
//            ).apply {
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) { // Para salvar/ler arquivos antes do Android 10
//                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
//                }
//                // Se precisar de acesso a arquivos de mídia em Android 10-12
//                else {
//                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
//                }
//            }.toTypedArray()
//        }

    private lateinit var binding: ActivityPermissionsCheckBinding

    // Launcher para solicitar múltiplas permissões
    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Este callback é chamado APÓS o usuário responder ao diálogo de permissão
            handlePermissionsResult(permissions)
        }

    private var isDisclaimerAccepted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPermissionsCheckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        permissionsCheckViewModel = ViewModelProvider(this)[SettingViewModel::class.java]

        isDisclaimerAccepted = permissionsCheckViewModel.isDisclaimerAccepted()

        // TODO: Implementar na produção
//        MobileAds.initialize(this) {}

        // Não faça a navegação no onCreate diretamente após a verificação inicial.
        // A lógica de permissão é assíncrona.
        // Apenas inicie o processo de verificação/solicitação.
        listeners()
        observer()
        permissionsCheckViewModel.getScreenResolutionDp(windowManager, resources)
    }

    override fun onResume() {
        super.onResume()
        Log.d("PermissionsCheckActivity", "onResume() called: PermissionsCheckActivity")
        // A verificação e solicitação devem acontecer aqui ou em um ponto que
        // garanta que o usuário veja a solicitação se necessário.
        // Se as permissões já foram concedidas (ex: usuário voltou das configurações),
        // ou se esta é a primeira vez, inicie o fluxo.
        checkAndRequestPermissions()
    }

    private fun listeners() {
        binding.disclaimerAcceptedCheckbox.setOnCheckedChangeListener { _, isChecked ->
            permissionsCheckViewModel.saveDisclaimerAcceptedCheckBox(isChecked)
        }

        binding.disclaimerAcceptedButton.setOnClickListener {
            permissionsCheckViewModel.saveDisclaimerAccepted(true)
            checkAndRequestPermissions()
        }
    }

    private fun observer() {
        permissionsCheckViewModel.isDisclaimerAcceptedCheckedBox.observe(this) { isChecked ->
            if (isChecked) {
                binding.disclaimerAcceptedButton.visibility = View.VISIBLE
                binding.disclaimerAcceptedCheckbox.text = ""
            } else {
                binding.disclaimerAcceptedButton.visibility = View.GONE
                binding.disclaimerAcceptedCheckbox.text = getString(R.string.button_agree_and_continue)
            }
        }
        permissionsCheckViewModel.isDisclaimerAccepted.observe(this) { isChecked ->
            isDisclaimerAccepted = isChecked
        }
    }
    private fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun checkAndRequestPermissions() {
        if (allPermissionsGranted() && isDisclaimerAccepted) {
            // Se todas as permissões já estão concedidas, navegue

            navigateToNextActivity()
        } else {
            // Se alguma permissão está faltando, solicite
            // Você pode querer mostrar um diálogo de explicação antes de solicitar,
            // especialmente se shouldShowRequestPermissionRationale retornar true para alguma permissão.
            var shouldShowRationale = false
            for (permission in requiredPermissions) {
                if (shouldShowRequestPermissionRationale(permission)) {
                    shouldShowRationale = true
                    break
                }
            }

            if (shouldShowRationale) {
                // Mostrar um diálogo explicando por que as permissões são necessárias
                // e então, no botão positivo do diálogo, chamar requestPermissionsLauncher.launch()
                showRationaleDialog()
            } else {
                // Solicitar permissões diretamente
                requestPermissionsLauncher.launch(requiredPermissions)
            }
        }
    }

    private fun handlePermissionsResult(grantedPermissions: Map<String, Boolean>) {
        if (grantedPermissions.all { it.value }) {
            // Todas as permissões foram concedidas e o usuário aceitou o disclaimer
            if (isDisclaimerAccepted) navigateToNextActivity()
        } else {
            // Pelo menos uma permissão foi negada
            // Verifique se alguma foi negada permanentemente
            var permanentlyDenied = false
            for (permission in requiredPermissions) {
                if (grantedPermissions[permission] == false && !shouldShowRequestPermissionRationale(permission)) {
                    permanentlyDenied = true
                    break
                }
            }

            if (permanentlyDenied) {
                showSettingsDialog()
            } else {
                // O usuário negou, mas não permanentemente. Você pode explicar novamente e tentar de novo,
                // ou apenas informar que o app não pode funcionar e fechar.
                showPermissionDeniedDialog()
            }
        }
    }

    private fun navigateToNextActivity() {
        permissionsCheckViewModel.savePrefers(Utils.PASSWORD.BIOMETRIC_SUCCESS, false)
        startActivity(Intent(this, LoginActivity::class.java)) // Ou CamActivity, conforme seu fluxo
        finish() // Finaliza esta activity para que o usuário não volte para ela
    }

    private fun showRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_required_title)) // Crie essas strings
            .setMessage(getString(R.string.permission_rationale_message)) // "Este app precisa das seguintes permissões para funcionar..."
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                requestPermissionsLauncher.launch(requiredPermissions)
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                Toast.makeText(this, getString(R.string.permissions_not_granted_will_close), Toast.LENGTH_LONG).show()
                finish() // O app não pode funcionar sem as permissões
            }
            .setCancelable(false)
            .show()
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_required_title))
            .setMessage(getString(R.string.permission_permanently_denied_message))
            .setPositiveButton(getString(R.string.settings)) { _, _ ->
                // Abre as configurações do aplicativo
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
                // Não chame finish() aqui imediatamente. O onResume() cuidará da re-verificação
                // quando o usuário retornar das configurações.
            }
            .setNegativeButton(getString(R.string.exit_app)) { _, _ ->
                Toast.makeText(this, getString(R.string.permissions_not_granted_will_close), Toast.LENGTH_LONG).show()
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_required_title))
            .setMessage(getString(R.string.permission_denied_message))
            .setPositiveButton(getString(R.string.retry)) { _, _ ->
                // Tenta solicitar as permissões novamente
                requestPermissionsLauncher.launch(requiredPermissions)
            }
            .setNegativeButton(getString(R.string.exit_app)) { _, _ ->
                Toast.makeText(this, getString(R.string.permissions_not_granted_will_close), Toast.LENGTH_LONG).show()
                finish()
            }
            .setCancelable(false)
            .show()
    }
}