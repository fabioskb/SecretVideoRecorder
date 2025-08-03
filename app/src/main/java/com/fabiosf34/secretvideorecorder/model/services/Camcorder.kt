package com.fabiosf34.secretvideorecorder.model.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.fabiosf34.secretvideorecorder.model.listeners.PreviewRunningListener
import com.fabiosf34.secretvideorecorder.model.listeners.RecordingListener
import com.fabiosf34.secretvideorecorder.model.repository.Preferences
import com.fabiosf34.secretvideorecorder.model.utilities.Utils
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Camcorder(var context: Context) {
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var recordingListener: RecordingListener? = null
    private var preview: Preview? = null
    private val preferences = Preferences(context)

    fun hasBackCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {

                cameraProvider = cameraProviderFuture.get()
                val hasBackCamera =
                    cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) == true
                preferences.store(Utils.HAS_BACK_CAMERA, hasBackCamera)
            } catch (_: Exception) {
                preferences.store(Utils.HAS_BACK_CAMERA, false)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun hasFrontCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                val hasFrontCamera =
                    cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) == true
                preferences.store(Utils.HAS_FRONT_CAMERA, hasFrontCamera)
            } catch (_: Exception) {
                preferences.store(Utils.HAS_FRONT_CAMERA, false)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun startCamPreview(
        viewFinder: PreviewView,
        cameraSelector: CameraSelector,
        lifecycleOwner: LifecycleOwner,
        listener: PreviewRunningListener,
        useRecorder: Boolean = false,
    ) {
        cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            if (!useRecorder) {
                preview = Preview.Builder().build()
                preview?.surfaceProvider = viewFinder.surfaceProvider
            }

            val recorder =
                Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HIGHEST)).build()
            videoCapture = VideoCapture.withOutput(recorder)

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    if (!useRecorder) preview else videoCapture!!,
                )
                Log.d("camcorder", "camera iniciada")
                listener.onPreviewRunning(true)

//////////////////////////// Configuração do arquivo de vídeo e gravação ///////////////
                if (useRecorder) {
                    val timeStamp: String = SimpleDateFormat(
                        Utils.Companion.FILE_NAME_FORMAT, Locale.US
                    ).format(Date(System.currentTimeMillis()))
                    val videoFileName = "$timeStamp.mp4"
                    val outputDirectory = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Utils.Companion.CAM_DIR_NAME_API_Q_OR_GREATER
                        else Utils.Companion.CAM_DIR_NAME_API_P_OR_LESS
                    )

                    // Cria o diretório se não existir e cria o arquivo .nomedia
                    if (!outputDirectory.exists()) {
                        outputDirectory.mkdirs()
                    }
                    val noMediaFile = File(outputDirectory, ".nomedia")
                    if (!noMediaFile.exists()) {
                        try {
                            if (noMediaFile.createNewFile()) {
                                Log.d("camcorder", ".nomedia criado com sucesso.")
                            } else {
                                Log.e("camcorder", "Falha ao criar .nomedia.")
                            }
                        } catch (e: IOException) {
                            Log.e("camcorder", "Erro ao criar .nomedia: ${e.message}")
                            e.printStackTrace()
                        }
                    }

                    val outputFile = File(outputDirectory, videoFileName)
                    val outputOptions = FileOutputOptions.Builder(outputFile).build()

                    if (ContextCompat.checkSelfPermission(
                            context, Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.e("camcorder", "Permissão da câmera não concedida no serviço.")
                        return@addListener
                    }
                    recording = videoCapture!!.output.prepareRecording(context, outputOptions)
                        .withAudioEnabled().start(ContextCompat.getMainExecutor(context)) { event ->
                            when (event) {
                                is VideoRecordEvent.Start -> {
                                    Log.d("camcorder", "Gravação iniciada.")
//                                    notification.showRecordingNotification()
//                                showRecordingNotification()
//                                    videoFileObserver?.stopWatching() // Opcional: Desativar observador durante a gravação
                                }

                                is VideoRecordEvent.Finalize -> {
                                    if (event.hasError()) {
                                        Log.e("camcorder", "Erro na gravação: ${event.error}")
                                        Log.e(
                                            "camcorder", "Erro na gravação: ${event.cause?.cause}"
                                        )
                                    } else {
                                        Log.d(
                                            "camcorder",
                                            "Gravação finalizada. Arquivo salvo em: ${event.outputResults.outputUri}"
                                        )
                                    }
                                    listener.onPreviewStopped(false)
                                    recordingListener?.onRecordingComplete()
                                }
                            }
                        }

                }
////////////////////////////////////////////////////////////////////////////////////////
            } catch (exc: Exception) {
                listener.onPreviewStopped(false)
                Log.e("camcorder", "Falha ao vincular a câmera", exc)
            }

        }, ContextCompat.getMainExecutor(context))

    }

    fun stopCamPreview(): Boolean {
        cameraProvider?.unbindAll()
        cameraProvider = null
        return false
    }

}