package com.fabiosf34.secretvideorecorder.model.services

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.fabiosf34.secretvideorecorder.R
import com.fabiosf34.secretvideorecorder.model.listeners.RecordingListener
import com.fabiosf34.secretvideorecorder.model.repository.Preferences
import com.fabiosf34.secretvideorecorder.model.utilities.StorageHelper
import com.fabiosf34.secretvideorecorder.model.utilities.Utils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class BackgroundVideoService : LifecycleService() {

    inner class LocalBinder : Binder() {
        fun getService(): BackgroundVideoService = this@BackgroundVideoService
    }

    private lateinit var preferences: Preferences

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private val executor = Executors.newSingleThreadExecutor()

    private val binder = LocalBinder()
    private var recordingListener: RecordingListener? = null

    private var defaultCameraSelector: CameraSelector? = null

    private var stopRecordingInLandscapeMode = false

    private lateinit var serviceNotificationsHelper: ServiceNotifications
    private lateinit var storageHelper: StorageHelper

    private var cameraProvider: ProcessCameraProvider? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("BackgroundVideoService", "onCreate() chamado")
        serviceNotificationsHelper = ServiceNotifications(this)
        storageHelper = StorageHelper(this)
        serviceNotificationsHelper.createNotificationChannel()
        preferences = Preferences(this)

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        stopRecordingInLandscapeMode =
            preferences.retrieve(Utils.STOP_RECORDING_SWITCH_SETTINGS, false)
        if ((newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
                    || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
            && stopRecordingInLandscapeMode
        ) {
            stopRecording()
            preferences.store(Utils.STOPPED_RECORDING_BY_LANDSCAPE, true)
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        Log.d("BackgroundVideoService", "onStartCommand() chamado")
        val defaultCameraSelectorString =
            preferences.retrieve(Utils.DEFAULT_CAMERA, getString(R.string.back_cam))

        defaultCameraSelector = if (defaultCameraSelectorString == getString(R.string.back_cam)) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }
        preferences.store(Utils.STOPPED_RECORDING_BY_LANDSCAPE, false)
        settingsAndStartRecording()

        when (intent?.action) {
            Utils.ACTION_STOP_RECORDING -> {
                stopRecording()
                // Enviar um broadcast para as Activities se finalizarem
                val intent = Intent(Utils.ACTION_FINISH_ACTIVITIES)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent) // Ou use LocalBroadcastManager para broadcasts dentro do app
                Log.d("BackgroundVideoService", "Gravação parada, broadcast enviado para finalizar Activities, e serviço encerrado.")
                return START_NOT_STICKY
            }
        }

        return START_REDELIVER_INTENT  // Use START_STICKY para garantir que o serviço reinicie se for interrompido pelo sistema
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d("BackgroundVideoService", "onDestroy() chamado")
        stopRecording()
        preferences.store(Utils.IS_RECORDING, false)
    }


    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return binder
    }


    private fun showRecordingNotification() {
        // Crie a notificação para o foreground service
        val notification =
            serviceNotificationsHelper.buildRecordingNotification() // Você pode precisar
        // modificar sua classe ServiceNotifications para ter um mét_odo que apenas retorne a notificação

        // Inicie o serviço em foreground e associe a notificação
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // API 29+
            // Para Android 14 (API 34+), adicione o tipo de serviço
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceCompat.startForeground(
                    this,
                    Utils.RECORDING_NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA // Tipo apropriado para gravação de vídeo
                )
            } else {
                ServiceCompat.startForeground(
                    this,
                    Utils.RECORDING_NOTIFICATION_ID,
                    notification,
                    0 // Flags adicionais, 0 geralmente é suficiente
                )
            }
        } else {
            ServiceCompat.startForeground(
                this, Utils.RECORDING_NOTIFICATION_ID, notification, 0
            )
        }
    }

    fun setRecordingCompleteListener(listener: RecordingListener) {
        this.recordingListener = listener
    }


    private fun settingsAndStartRecording() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("BackgroundVideo", "Permissão da câmera não concedida no serviço.")
            recordingListener?.onRecordingError(getString(R.string.cam_permission_required_error))
            stopSelf()
            return
        }

        // Obtenha o diretório de vídeo específico do aplicativo
        val outputAppSpecificVideoDir =
            storageHelper.getPrioritizedAppSpecificVideoStorageDirectory()

        if (outputAppSpecificVideoDir == null) {
            Log.e(
                "BackgroundVideoService",
                "Não foi possível obter/criar o diretório de gravação específico do app."
            )
            recordingListener?.onRecordingError(getString(R.string.storage_access_error))
            stopSelf()
            return // Modificado para retornar void se a função for void
        }


        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener( {
            cameraProvider = cameraProviderFuture.get()

//            val preview = Preview.Builder().build()
//            preview.surfaceProvider = viewFinder.surfaceProvider

            val videoQualityPrefers = preferences.retrieve(Utils.VIDEO_QUALITY, Utils.VIDEO_QUALITY_DEFAULT)
            val qualitySelector = when (videoQualityPrefers) {
                Utils.VIDEO_QUALITY_HIGH -> {
                    QualitySelector.from(Quality.HIGHEST)
                }
                Utils.VIDEO_QUALITY_MEDIUM -> {
                    QualitySelector.from(Quality.HD)
                }
                else -> {
                    QualitySelector.from(Quality.LOWEST)
                }
            }
            val recorder =
                Recorder.Builder().setQualitySelector(qualitySelector).build()
            videoCapture = VideoCapture.withOutput(recorder)
            Log.d("BackgroundVideoService", "VideoCapture configurado: Qualidade: ${videoCapture?.output?.qualitySelector}")

            val cameraSelector = defaultCameraSelector

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    this, cameraSelector!!, videoCapture!!
                )

                val timeStamp: String = SimpleDateFormat(
                    Utils.Companion.FILE_NAME_FORMAT, Locale.US
                ).format(Date(System.currentTimeMillis()))

                val videoFileName = "Video-$timeStamp.mp4"
                // Use o diretório específico do aplicativo obtido anteriormente
                val outputFile = File(outputAppSpecificVideoDir, videoFileName)

                Log.d(
                    "BackgroundVideoService",
                    "Salvando vídeo em (app-specific external): ${outputFile.absolutePath}"
                )

                // Sempre use FileOutputOptions para este cenário
                val fileOutputOptions = FileOutputOptions.Builder(outputFile).build()

                // Inicie a gravação
                recording = videoCapture!!.output.prepareRecording(this, fileOutputOptions)
                    .withAudioEnabled().start(ContextCompat.getMainExecutor(this)) { event ->
                        when (event) {
                            is VideoRecordEvent.Start -> {
                                Log.d("BackgroundVideo", "Gravação iniciada.")
                                showRecordingNotification()
                                recordingListener?.onRecording()
                            }

                            is VideoRecordEvent.Finalize -> {

                                if (event.hasError()) {
                                    Log.e(
                                        "BackgroundVideoService",
                                        "Erro na gravação: ${event.error}\nCausa: ${event.cause?.cause}"
                                    )
                                    if (event.error != 8) recordingListener?.onRecordingError(getString(R.string.error_recording_msg))
                                } else {
                                    Log.d(
                                        "BackgroundVideoService",
                                        "Gravação finalizada. Arquivo salvo em: ${event.outputResults.outputUri}"
                                    )
                                    val contentUri =
                                        event.outputResults.outputUri // URI do MediaStore
                                    preferences.store(Utils.FILE_NAME, "Video-$timeStamp.mp4")
                                    preferences.store(Utils.FILE_URI, contentUri.toString())
                                    recordingListener?.onRecordingComplete()
                                }
                                serviceNotificationsHelper.cancelNotification(Utils.RECORDING_NOTIFICATION_ID)
                                stopForeground(STOP_FOREGROUND_REMOVE)
                                stopSelf()
                            }
                        }
                    }
            } catch (exc: Exception) {
                Log.e("BackgroundVideo", "Falha ao iniciar a gravação", exc)
                recordingListener?.onRecordingError("${getString(R.string.error_start_recording)}: ${exc.cause}")
                stopSelf()
            }
        }, ContextCompat.getMainExecutor(this))
    }


    fun stopRecording() {
        recording?.stop()
        recording = null
        executor.shutdown()
        if (cameraProvider != null) cameraProvider?.unbindAll()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

}