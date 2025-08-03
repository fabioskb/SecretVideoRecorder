package com.fabiosf34.secretvideorecorder.model.utilities

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_ALL
import androidx.core.content.ContextCompat.getString
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
import com.fabiosf34.secretvideorecorder.R
import com.fabiosf34.secretvideorecorder.view.CamActivity
import java.io.File

class Notifications(val context: Context) {

    private val notificationManager =
        getSystemService(context, NotificationManager::class.java) as NotificationManager
//    fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channelName = "Gravação de Vídeo"
//            val channelDescription =
//                "Notificações para o serviço de gravação de vídeo em segundo plano"
//            val importance = NotificationManager.IMPORTANCE_LOW // Use LOW para minimizar a intrusão
//            val channel = NotificationChannel(Utils.CHANNEL_ID, channelName, importance).apply {
//                description = channelDescription
//            }
//
////            val notificationManager: NotificationManager =
////                getSystemService(context, NotificationManager::class.java) as NotificationManager
//            this.notificationManager.createNotificationChannel(channel)
//        }
//    }
    fun showRecordingCompleteNotification(videoUri: Uri?, videoId: Int) {
        var contentIntent: Intent
        if (videoUri != null) {
            val videoPath = videoUri.path
            if (videoPath != null) {
                val videoFile = File(videoPath)
                if (videoFile.exists()) {
                    try {
                        val authority = "${context.packageName}.provider"
                        // Converter file:/// URI para content:// COLUMN_URI
                        val contentUriForNotification =
                            FileProvider.getUriForFile(context, authority, videoFile)
                        Log.d(
                            "ServiceNotifications",
                            "URI de conteúdo para notificação: $contentUriForNotification"
                        )

                        contentIntent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(contentUriForNotification, "video/*")
                            flags =
                                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                            // FLAG_ACTIVITY_CLEAR_TOP pode ser útil se você quiser que o player de vídeo
                            // não acumule múltiplas instâncias da mesma Activity.
                            // flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                    } catch (e: IllegalArgumentException) {
                        Log.e(
                            "ServiceNotifications",
                            "Erro de FileProvider ao obter URI para notificação (caminho não configurado em file_paths.xml?): $videoFile",
                            e
                        )
                        // Fallback: Abrir a activity principal se o FileProvider falhar
                        contentIntent = Intent(context, CamActivity::class.java).apply {
                            flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                    } catch (e: Exception) {
                        Log.e(
                            "ServiceNotifications",
                            "Erro inesperado ao preparar intent de notificação: $videoUri",
                            e
                        )
                        contentIntent = Intent(context, CamActivity::class.java).apply {
                            flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                    }
                } else {
                    Log.w(
                        "ServiceNotifications",
                        "Arquivo da URI não encontrado: $videoPath"
                    )
                    // Fallback: Arquivo não encontrado, abrir a activity principal
                    contentIntent = Intent(context, CamActivity::class.java).apply {
                        flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                }
            } else {
                Log.w("ServiceNotifications", "Caminho da URI do arquivo é nulo: $videoUri")
                // Fallback: Caminho nulo, abrir a activity principal
                contentIntent = Intent(context, CamActivity::class.java).apply {
                    flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
            }
        } else {
            Log.w(
                "ServiceNotifications",
                "URI do vídeo é nula ou não é uma URI de arquivo: $videoUri"
            )
            // Fallback: URI nula ou esquema inválido, abrir a activity principal
            contentIntent = Intent(context, CamActivity::class.java).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        }

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                contentIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            ) // Use FLAG_IMMUTABLE se possível


        val notification = NotificationCompat.Builder(context, Utils.CHANNEL_ID)
            .setContentTitle(videoUri?.lastPathSegment.toString())
            .setContentText(
                getString(
                    context,
                    R.string.recording_complete_message
                )
            )
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentIntent(pendingIntent)
            .setDefaults(DEFAULT_ALL)
            .setAutoCancel(true) // A notificação é removida ao ser tocada
            .build()

//        val notificationManager =
//            getSystemService(context, NotificationManager::class.java) as NotificationManager
        this.notificationManager.notify(
            videoId,
            notification
        )  // Use um ID diferente para a notificação de conclusão
    }

    fun showErrorNotification(message: String) {
        val notification = NotificationCompat.Builder(context, Utils.CHANNEL_ID)
            .setContentTitle(getString(context, R.string.error_recording_title))
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

//        val notificationManager =
//            getSystemService(context, NotificationManager::class.java) as NotificationManager
        this.notificationManager.notify(
            Utils.ERROR_NOTIFICATION_ID,
            notification
        ) // Use um ID diferente
    }
}