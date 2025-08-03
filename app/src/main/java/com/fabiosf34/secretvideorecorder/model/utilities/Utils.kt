package com.fabiosf34.secretvideorecorder.model.utilities

import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources.getColorStateList
import androidx.appcompat.widget.SwitchCompat
import com.fabiosf34.secretvideorecorder.BuildConfig
import com.fabiosf34.secretvideorecorder.R
import com.fabiosf34.secretvideorecorder.model.utilities.Utils.Companion.AppLifecycleManager.appJustCameFromBackground
import com.fabiosf34.secretvideorecorder.model.utilities.Utils.Companion.AppLifecycleManager.isLoginActivityLaunched
import com.fabiosf34.secretvideorecorder.model.utilities.Utils.Companion.AppLifecycleManager.isSettingsActivityAdapted
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.system.exitProcess

class Utils {
    companion object {
        const val CAM_DIR_NAME_API_P_OR_LESS = ".BVR"
        const val CAM_DIR_NAME_API_Q_OR_GREATER = "BVR" // Ou "BVR_Recordings"
        const val FILE_NAME_FORMAT: String = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val FILE_NAME: String = "video"
        const val FILE_URI: String = "uri"
        const val ERROR_NOTIFICATION_ID: Int = 1
        const val RECORDING_NOTIFICATION_ID: Int = 3
        const val CHANNEL_ID: String = "BVR_CHANNEL"
        const val HAS_BACK_CAMERA: String = "has_back_camera"
        const val HAS_FRONT_CAMERA: String = "has_front_camera"
        const val DEFAULT_CAMERA: String = "default_camera"
        const val STOP_RECORDING_SWITCH_SETTINGS: String = "stop_recording_checkbox_settings"
        const val STOPPED_RECORDING_BY_LANDSCAPE: String = "stop_recording_by_landscape"
        const val IS_RECORDING: String = "is_recording"
        const val APP_NAME_ALIAS: String =
            "com.fabiosf34.secretvideorecorder.PermissionsCheckActivityAlias"
        const val APP_NAME_DISCREET_ALIAS: String =
            "com.fabiosf34.secretvideorecorder.PermissionsCheckActivityAliasDiscreet"
        const val APP_NAME_DEFAULT_VALUE: String =
            "com.fabiosf34.secretvideorecorder.view.PermissionsCheckActivity"
        const val STARTED_BY_NOTIFICATION: String = "started_by_notification"
        const val IS_FIRST_RUN_CAM: String = "is_first_run_cam"
        const val SHOW_VIDEO_RECORDING_COMPLETE_NOTIFICATIONS: String =
            "show_video_recording_complete_notifications"
        const val APP_NAME_SWITCH_SETTINGS: String = "app_name_switch_settings"
        const val SCREEN_ROTATE_DEFAULT: Int = 0
        const val HAS_SD_CARD_VIDEO_DIR: String = "has_sd_card_video_dir"
        const val SAVE_VIDEO_ON_SD_CARD: String = "save_video_on_sd_card"
        const val VIDEO_QUALITY: String = "video_quality"
        const val VIDEO_QUALITY_LOW: String = "low"
        const val VIDEO_QUALITY_MEDIUM: String = "medium"
        const val VIDEO_QUALITY_HIGH: String = "high"
        const val VIDEO_QUALITY_DEFAULT: String = VIDEO_QUALITY_MEDIUM
        const val START_RECORDING_ON_BOOT: String = "start_recording_on_boot"
        const val ACTION_STOP_RECORDING = "com.fabiosf34.secretvideorecorder.ACTION_STOP_RECORDING"
        const val ACTION_FINISH_ACTIVITIES = "com.fabiosf34.secretvideorecorder.FINISH_ACTIVITIES"
        const val VIDEO_INTERSTITIAL_AD_UNIT_ID = BuildConfig.ADMOB_INTERSTITIAL_ID
        const val BANNER_AD_UNIT_ID = BuildConfig.ADMOB_BANNER_ID
        const val PRIVACY_POLICY_URL =
            "https://sites.google.com/view/fabiosf34-secretvideorecorder/início/privacy-policy"

        // Objeto companion ou em um singleton acessível globalmente
        object AppLifecycleManager {
            /**
             * Manejador de ciclo de vida da aplicação.
             *
             * @param appJustCameFromBackground Se o app acabou de voltar do background,
             * @param isLoginActivityLaunched Se a LoginActivity já foi lançada,
             * @param isSettingsActivityAdapted Se a SettingsActivity foi adaptada para o login.
             */
            var appJustCameFromBackground = false
            var isLoginActivityLaunched = false // Para evitar loops
            var isSettingsActivityAdapted = true
        }

        fun setSwitchesColorAndState(context: Context, switch: SwitchCompat, isChecked: Boolean) {
            if (isChecked) {
                switch.isChecked = true
                switch.thumbTintList = getColorStateList(context, R.color.switch_thumb_color_on)
                switch.trackTintList = getColorStateList(context, R.color.switch_track_color_on)
            } else {
                switch.isChecked = false
                switch.thumbTintList = getColorStateList(context, R.color.switch_thumb_color_off)
                switch.trackTintList = getColorStateList(context, R.color.switch_track_color_off)
            }
        }

    }

    object PASSWORD {
        const val PASSWORD: String = "password"
        const val IS_PASSWD_ENABLED: String = "isChecked"
        const val BIOMETRIC_SETTINGS: String = "biometric_settings"
        const val BIOMETRIC_SUCCESS: String = "biometric_success"
    }

    object AppUtils { // Ou coloque em uma classe utilitária de sua preferência
        /**
         * Reinicia completamente a aplicação.
         *
         * IMPORTANTE: Use com extrema cautela. Isso interrompe abruptamente o processo
         * atual e pode levar à perda de estado não salvo se não for manuseado corretamente
         * antes de chamar esta função. É geralmente uma má experiência para o usuário
         * se não for claramente comunicado ou esperado.
         *
         * @param context Contexto para obter o PackageManager e o nome do pacote.
         */
        fun triggerRebirth(context: Context) {
            val packageManager: PackageManager = context.packageManager
            // Tenta obter o intent de lançamento padrão para o pacote atual
            val intent: Intent? = packageManager.getLaunchIntentForPackage(context.packageName)

            if (intent == null) {
                // Não foi possível encontrar o intent de lançamento, talvez o app esteja em um estado estranho
                // Você pode querer logar um erro aqui
                // Como fallback, apenas encerre o processo
                exitProcess(0)
            }

            val componentName = intent.component
            if (componentName == null) {
                // O intent não tem um componente específico, o que é incomum para um intent de lançamento
                // Você pode querer logar um erro aqui
                exitProcess(0)
            }


            // Cria um intent que irá reiniciar a tarefa
            val restartIntent = Intent.makeRestartActivityTask(componentName)

            // Inicia a nova instância da activity
            context.startActivity(restartIntent)

            // Encerra o processo atual para garantir uma reinicialização limpa
            exitProcess(0)
        }

        fun setAppLauncherAlias(
            context: Context,
            aliasToEnableName: String,
            aliasToDisableName: String,
        ) {
            // 'this' aqui se refere à instância de SettingsActivity, que é um Context.
            // Usar applicationContext é geralmente mais seguro para evitar memory leaks se o contexto
            // fosse armazenado em algum lugar por mais tempo, mas para uma chamada imediata ao PackageManager,
            // 'this' (Activity context) também funciona.
            // Para consistência e robustez, applicationContext ainda é uma boa prática.
            val currentContext: Context = context
            val packageManager = currentContext.packageManager
            val currentPackageName = currentContext.packageName // ou BuildConfig.APPLICATION_ID

            val tag = "AliasSwitcher"

            Log.d(tag, "------------------------------------------------------------")
            Log.d(tag, "Iniciando a troca de aliases do launcher (dentro de SettingsActivity)...")
            Log.d(tag, "PackageName do Contexto Atual: '$currentPackageName'")
            Log.d(
                tag,
                "String (NOME COMPLETO) do Alias para HABILITAR (recebida): '$aliasToEnableName'"
            )
            Log.d(
                tag,
                "String (NOME COMPLETO) do Alias para DESABILITAR (recebida): '$aliasToDisableName'"
            )

            if (aliasToEnableName.isBlank() || aliasToDisableName.isBlank()) {
                Log.e(
                    tag,
                    "ERRO: Um ou ambos os nomes de alias (completos) estão vazios. Abortando."
                )
                Log.d(tag, "------------------------------------------------------------")
                return
            }

            // Criar ComponentName usando o nome do pacote e o nome completo da classe do alias.
            // 'aliasToEnableName' e 'aliasToDisableName' DEVEM ser os nomes completos agora.
            val componentToEnable = ComponentName(currentPackageName, aliasToEnableName)
            val componentToDisable = ComponentName(currentPackageName, aliasToDisableName)
            ComponentName(currentPackageName, APP_NAME_DEFAULT_VALUE)

            Log.d(tag, "Objeto ComponentName para HABILITAR: $componentToEnable")
            Log.d(tag, "  -> Pacote Resolvido (Enable): '${componentToEnable.packageName}'")
            Log.d(tag, "  -> Classe Resolvida (Enable): '${componentToEnable.className}'")
            Log.d(tag, "  -> Nome Achatado (Enable): '${componentToEnable.flattenToString()}'")

            Log.d(tag, "Objeto ComponentName para DESABILITAR: $componentToDisable")
            Log.d(tag, "  -> Pacote Resolvido (Disable): '${componentToDisable.packageName}'")
            Log.d(tag, "  -> Classe Resolvida (Disable): '${componentToDisable.className}'")
            Log.d(tag, "  -> Nome Achatado (Disable): '${componentToDisable.flattenToString()}'")

            try {
                Log.i(
                    tag,
                    "Tentando HABILITAR o componente: '${componentToEnable.flattenToString()}'"
                )
                packageManager.setComponentEnabledSetting(
                    componentToEnable,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                Log.i(tag, "SUCESSO ao HABILITAR: '${componentToEnable.flattenToString()}'")

                Log.i(
                    tag,
                    "Tentando DESABILITAR o componente: '${componentToDisable.flattenToString()}'"
                )
                packageManager.setComponentEnabledSetting(
                    componentToDisable,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
//                packageManager.setComponentEnabledSetting(
//                        componentDefaultToDisable,
//                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//                PackageManager.DONT_KILL_APP
//                )
                Log.i(tag, "SUCESSO ao DESABILITAR: '${componentToDisable.flattenToString()}'")
//                Log.i(TAG, "SUCESSO ao DESABILITAR (padrão): '${componentDefaultToDisable.flattenToString()}'")

                Log.d(tag, "Troca de aliases concluída com sucesso.")

            } catch (e: IllegalArgumentException) {
                Log.e(
                    tag,
                    "ERRO GRAVE (IllegalArgumentException) ao tentar definir o estado do componente: ${e.message}"
                )
                Log.e(
                    tag,
                    "  Componente problemático provável (verifique se o nome completo está correto e existe no Manifest): '${
                        if (e.message != null && e.message!!.contains(componentToEnable.shortClassName ?: "")) componentToEnable.flattenToString() else componentToDisable.flattenToString()
                    }'"
                )
                Log.e(tag, "  Stack Trace:", e)
            } catch (e: Exception) {
                Log.e(tag, "ERRO GENÉRICO ao tentar definir o estado do componente: ${e.message}")
                Log.e(tag, "  Stack Trace:", e)
            } finally {
                Log.d(tag, "------------------------------------------------------------")
            }
        }

        /**
         * Exibe um diálogo de alerta com as opções fornecidas.
         * @param context O contexto para exibir o diálogo.
         * @param title O título do diálogo.
         * @param message A mensagem a ser exibida no diálogo.
         * @param positiveButtonText O texto do botão positivo.
         * @param negativeButtonText O texto do botão negativo.
         * @param listener O listener para o botão positivo.
         * @param cancelable Se o diálogo pode ser cancelado pelo usuário. O padrão é true.
         */
        fun dialog(
            context: Context,
            title: Int,
            message: Int,
            positiveButtonText: String,
            negativeButtonText: String,
            listener: DialogInterface.OnClickListener,
            cancelable: Boolean = true
        ) {
            MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, listener)
                .setNegativeButton(negativeButtonText, null)
                .setCancelable(cancelable)
                .show()
        }
    }
}