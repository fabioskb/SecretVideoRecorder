package com.fabiosf34.secretvideorecorder.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.fabiosf34.secretvideorecorder.model.utilities.Utils
import com.fabiosf34.secretvideorecorder.model.utilities.Utils.Companion.AppLifecycleManager
import com.fabiosf34.secretvideorecorder.viewModel.SettingViewModel

// Exemplo de BaseActivity
abstract class ProtectedBaseActivity : AppCompatActivity() {
    private lateinit var baseViewModel: SettingViewModel // Inicialize em onCreate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseViewModel = ViewModelProvider(this)[SettingViewModel::class.java]
    }

    override fun onResume() {
        super.onResume()
        // Checa se o dispositivo tem um cartão de memória removível e se está montado
        // para ser usado como opção de salvar os vídeos em um diretório específico dele.
        if (!AppLifecycleManager.isSettingsActivityAdapted) baseViewModel.hasSdCardVideoDir()

        val isBiometricSuccessful = baseViewModel.getPrefers(Utils.PASSWORD.BIOMETRIC_SUCCESS, false)

        // Verifica se o app acabou de voltar do background E se o login é necessário
        if (AppLifecycleManager.appJustCameFromBackground &&
            !AppLifecycleManager.isLoginActivityLaunched &&
            AppLifecycleManager.isSettingsActivityAdapted &&
            !isBiometricSuccessful &&
            needsReAuthentication()
        ) {

            AppLifecycleManager.isLoginActivityLaunched = true // Marcar que estamos lançando
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

//                putExtra(LoginActivity.EXTRA_REQUIRE_REAUTH.toString(), true)
            }
            startActivity(intent)
            // Não finalizar esta activity aqui, para que o usuário possa voltar se cancelar o login,
            // ou para que o estado seja preservado se o login for bem-sucedido e a LoginActivity for finalizada.
        }
        // Sempre resetar appJustCameFromBackground após a verificação em onResume
        AppLifecycleManager.appJustCameFromBackground = false

    }

    override fun onStop() {
        super.onStop()
        // Uma forma simplista de detectar se o app está indo para o background.
        // Se esta é a última activity visível, então o app está indo para o background.
        // Esta lógica é menos precisa que ActivityLifecycleCallbacks.
        if (!isChangingConfigurations) {
            AppLifecycleManager.appJustCameFromBackground = true
            // Uma verificação mais robusta aqui seria necessária para saber se NENHUMA
            // activity do app está visível. ActivityLifecycleCallbacks faz isso melhor.
            // Para esta abordagem simplificada, vamos assumir que se onStop é chamado
            // e não é por mudança de config, o app PODE estar indo para background.
            // A forma mais confiável de setar appJustCameFromBackground = true
            // seria através de um BroadcastReceiver para ACTION_SCREEN_OFF / ON
            // ou usando o ActivityLifecycleCallbacks como na solução anterior.

            // **** INÍCIO DA LIMITAÇÃO DESTA ABORDAGEM SIMPLIFICADA ****
            // Esta detecção de "indo para background" é o ponto fraco desta abordagem
            // sem ActivityLifecycleCallbacks.
            // Apenas para fins de ilustração, vamos assumir que você tem alguma outra forma
//             de setar
//            AppLifecycleManager.appJustCameFromBackground = true
            // quando o app de fato volta de um estado minimizado.
            // Exemplo: se você tivesse um serviço rodando que detecta isso.
            // Se não, o "onResume" acima pode disparar o login mesmo entre activities.
            // **** FIM DA LIMITAÇÃO ****
        }
    }

    private fun needsReAuthentication(): Boolean {
        // Sua lógica existente para verificar se a senha está configurada e habilitada
        val isPasswordEnabled = baseViewModel.getPrefers(
            Utils.PASSWORD.IS_PASSWD_ENABLED,
            false
        )
        return isPasswordEnabled
    }
}