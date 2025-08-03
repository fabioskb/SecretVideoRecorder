package com.fabiosf34.secretvideorecorder.model.services

//import androidx.core.content.ContextCompat.getSystemService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getString
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.LifecycleService
import com.fabiosf34.secretvideorecorder.R
import com.fabiosf34.secretvideorecorder.model.repository.Preferences
import com.fabiosf34.secretvideorecorder.model.utilities.Utils
import com.fabiosf34.secretvideorecorder.view.CamActivity
import com.fabiosf34.secretvideorecorder.view.LoginActivity

class ServiceNotifications(val context: LifecycleService) {

    private val preferences = Preferences(context)
    private val notificationManager =
        getSystemService(context, NotificationManager::class.java) as NotificationManager

    // Notificações
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getString(context, R.string.recording_service)
            val channelDescription =
                "Notificações para o serviço de gravação de vídeo em segundo plano"
            val importance = NotificationManager.IMPORTANCE_HIGH // Use LOW para minimizar a intrusão
            val channel = NotificationChannel(Utils.CHANNEL_ID, channelName, importance).apply {
                description = channelDescription
                setSound(null, null)
                vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            }
//            val notificationManager: NotificationManager =
//                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

//    fun showRecordingNotification() {
//        val notification: Notification = buildRecordingNotification()
//
////        val notificationManager =
////            getSystemService(context, NotificationManager::class.java)
//        this.notificationManager.notify(Utils.RECORDING_NOTIFICATION_ID, notification)
//    }

    /**
     * Constrói e retorna o objeto Notification para a gravação em andamento.
     * Este mét odo não exibe a notificação.
     */
    fun buildRecordingNotification(): Notification {

        val passwdEnabled = preferences.retrieve(Utils.PASSWORD.IS_PASSWD_ENABLED, false)
        // Crie um PendingIntent que abra sua Activity principal quando a notificação for clicada (opcional)
        var openIntent: Intent?
        if (!passwdEnabled) {
            openIntent = Intent(context, CamActivity::class.java)
            openIntent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        } else {
            preferences.store(Utils.STARTED_BY_NOTIFICATION, true)
            openIntent = Intent(context, LoginActivity::class.java)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            if (!passwdEnabled) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            else PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Use FLAG_IMMUTABLE se possível
        )

//        val stopIntent = Intent(context, BackgroundVideoService::class.java).apply {
//            action = Utils.ACTION_STOP_RECORDING
//        }

//        val stopPendingIntent = PendingIntent.getService(
//            context,
//            0,
//            stopIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )

        val builder = NotificationCompat.Builder(context.applicationContext, Utils.CHANNEL_ID)
            .setContentTitle(getString(context, R.string.recording_service))
            .setContentText(getString(context, R.string.recording_service_message))
            .setSmallIcon(android.R.drawable.ic_secure) // Substitua pelo seu ícone
            .setOngoing(true)  // A notificação não pode ser dispensada pelo usuário
            // Adicione a intent para que algo aconteça quando a notificação for clicada
//            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setStyle(NotificationCompat.BigTextStyle())
            .addAction(R.drawable.ic_material_open, context.getString(R.string.open), openPendingIntent)
//            .addAction(R.drawable.ic_material_stop, context.getString(R.string.stop_capture), stopPendingIntent)
            // Considere mudar para PRIORITY_LOW ou PRIORITY_DEFAULT para menos intrusão
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Exemplo com prioridade mais alta

        // Modificações específicas para controlar som e vibração:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Para Android 8.0+ (API 26+), o canal controla o som.
            // Se você já configurou setSound(null, null) no canal,
            // não precisa fazer muito aqui, a menos que queira sobrepor algo.
            // Remover setDefaults é geralmente bom se o canal está bem configurado.
        } else {
            // Para Android 7.1.1 (API 25) e inferior:
            builder.setSound(null) // <--- ESSENCIAL PARA REMOVER O SOM

            // Se você também não quer vibração nestas versões mais antigas:
            // builder.setVibrate(null) // Ou builder.setVibrate(new long[]{0});
            // Ou, se você QUER vibração mas não som:
             builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            // Mas se você só quer remover o som e potencialmente outros padrões:
//            builder.setDefaults(0) // Remove todos os padrões (som, vibração, luzes)
            // Se você usar setDefaults(0) e quiser vibração,
            // você precisaria chamar builder.setVibrate() separadamente
            // com um padrão.
            // Se você apenas chamar builder.setSound(null), ele remove o som
            // mas pode manter outros padrões se setDefaults() foi chamado
            // com eles (ex: DEFAULT_VIBRATE).
            // A maneira mais segura de remover o som e manter outros
            // comportamentos (vibração, luzes) é NÃO chamar setDefaults()
            // com DEFAULT_SOUND ou DEFAULT_ALL, e chamar setSound(null).
        }

        // Remova ou ajuste .setDefaults() para todas as versões se o canal
        // (para API 26+) ou as configurações individuais (para API < 26)
        // já cuidam do comportamento.
        // .setDefaults(NotificationCompat.DEFAULT_ALL) // <-- REMOVA OU COMENTE ESTA LINHA

        // Defina a prioridade (importante para versões < API 26)
        // Se você quer que seja menos intrusiva, mas ainda visível para um serviço ongoing:
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
//            builder.priority = NotificationCompat.PRIORITY_DEFAULT // Ou PRIORITY_LOW
//        } else {
            // Para API 26+, a importância do canal é mais significativa.
            // Mas definir a prioridade aqui não prejudica e pode ter algum efeito sutil
            // ou ser um fallback.
//            builder.priority = NotificationCompat.PRIORITY_HIGH // Ou o que for definido no canal
//        }

        return builder.build()
    }

    fun cancelNotification(notificationId: Int) {
//        val notificationManager =
//            getSystemService(context, NotificationManager::class.java) as NotificationManager
        this.notificationManager.cancel(notificationId)
        Log.d("ServiceNotifications", "Notificação $notificationId cancelada.")
    }
}