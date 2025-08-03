package com.fabiosf34.secretvideorecorder.view

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraSelector
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.fabiosf34.secretvideorecorder.R
import com.fabiosf34.secretvideorecorder.databinding.ActivityCamBinding
import com.fabiosf34.secretvideorecorder.model.Video
import com.fabiosf34.secretvideorecorder.model.listeners.RecordingListener
import com.fabiosf34.secretvideorecorder.model.services.BackgroundVideoService
import com.fabiosf34.secretvideorecorder.model.utilities.Utils
import com.fabiosf34.secretvideorecorder.model.utilities.Utils.Companion.AppLifecycleManager
import com.fabiosf34.secretvideorecorder.viewModel.CamViewModel
// TODO: Implementar na produção
//import com.google.android.gms.ads.AdError
//import com.google.android.gms.ads.AdView
//import com.google.android.gms.ads.FullScreenContentCallback
//import com.google.android.gms.ads.LoadAdError
//import com.google.android.gms.ads.interstitial.InterstitialAd
//import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class CamActivity : ProtectedBaseActivity(), RecordingListener {
    private lateinit var binding: ActivityCamBinding
    private lateinit var camViewModel: CamViewModel
    private lateinit var serviceIntent: Intent

    private var showVideoRecordingCompleteNotifications = false

    lateinit var defaultCamera: CameraSelector
    private var isPreviewRunning = false
    private var backgroundVideoService: BackgroundVideoService? = null

    private var isFirstRumCamActivity = false
    private var isRecording = false

//    TODO: Implementar na produção
//    private lateinit var adViewBanner: AdView
//    private var mInterstitialAd: InterstitialAd? = null
    private var deviceScreenWidthPx = 0
    private var deviceScreenHeightPx = 0

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as BackgroundVideoService.LocalBinder // Agora usando LocalBinder
            backgroundVideoService = binder.getService()
            backgroundVideoService?.setRecordingCompleteListener(this@CamActivity)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            backgroundVideoService = null
        }
    }

    private val finishActivityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Utils.ACTION_FINISH_ACTIVITIES) {
                binding.videoStopButton.performClick()
                finish() // Finaliza esta Activity
                Log.d("CamActivity", "Activity finalizada via broadcast do serviço.")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCamBinding.inflate(layoutInflater)

        // Previne a aparição do conteúdo da activity em recents e screeshots
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE
        )
        /////

        setContentView(binding.root)
        camViewModel = ViewModelProvider(this)[CamViewModel::class.java]

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (camViewModel.retrievePreference(
                Utils.DEFAULT_CAMERA,
                getString(R.string.back_cam)
            ) == getString(R.string.back_cam)
        ) {
            defaultCamera = CameraSelector.DEFAULT_BACK_CAMERA
            setDefaultCamSettings()
        } else {
            defaultCamera = CameraSelector.DEFAULT_FRONT_CAMERA
            setDefaultCamSettings()
        }

        isFirstRumCamActivity = camViewModel.retrievePreference(Utils.IS_FIRST_RUN_CAM, true)

        binding.appBarMain.toolbar.title = ""
        binding.appBarMain.galleryToolbar.title = ""
        setSupportActionBar(binding.appBarMain.toolbar)
        setSupportActionBar(binding.appBarMain.galleryToolbar)
        listeners()

        serviceIntent = Intent(this, BackgroundVideoService::class.java)

        if (isFirstRumCamActivity) {
            camViewModel.storePreference(Utils.IS_FIRST_RUN_CAM, false)
            camViewModel.restartApp(this, lifecycleScope)
        }

        Intent(this, BackgroundVideoService::class.java).also { intent ->
            bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        }

        val intentFilter = IntentFilter(Utils.ACTION_FINISH_ACTIVITIES)
        // Use registerReceiver(receiver, filter, RECEIVER_EXPORTED) ou RECEIVER_NOT_EXPORTED
        // Para Android 13+ se não for usar LocalBroadcastManager.
        // Se usar LocalBroadcastManager, o registro é diferente.
        LocalBroadcastManager.getInstance(this).registerReceiver(finishActivityReceiver, intentFilter)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33 (Android 13) e superior
//            registerReceiver(finishActivityReceiver, intentFilter, RECEIVER_NOT_EXPORTED)
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // API 31 (Android 12) e API 32 (Android 12L)
//            // Para receptores que não escutam broadcasts protegidos por permissão de sistema,
//            // a flag é necessária a partir da API 31.
//            // Se Utils.ACTION_FINISH_ACTIVITIES não é uma ação de sistema protegida,
//            // então RECEIVER_NOT_EXPORTED é o correto.
//            registerReceiver(finishActivityReceiver, intentFilter, RECEIVER_NOT_EXPORTED)
//        }
//        else { // Abaixo da API 31 (Android 11 e inferior)
//            registerReceiver(finishActivityReceiver, intentFilter)
//        }
        // Alternativa: LocalBroadcastManager.getInstance(this).registerReceiver(finishActivityReceiver, intentFilter)


        // Admob
        // TODO: Inicialize o AdMob Interstitial
//        camViewModel.adMobInterstitialLoader(loadInterstitialCallBack())
        ///////

        camViewModel.getRecordingOnBootStatus()
        createObserver()
    }


    override fun onResume() {
        super.onResume()
        isRecording = camViewModel.getRecordingStatus()
        observer()
        setLayout(resources.configuration.orientation)

//
        if (camViewModel.retrievePreference(
                Utils.STOPPED_RECORDING_BY_LANDSCAPE,
                false
            ) && binding.videoStopButton.isVisible
        ) {
            binding.videoStopButton.performClick()
        }

        showVideoRecordingCompleteNotifications = camViewModel.retrievePreference(
            Utils.SHOW_VIDEO_RECORDING_COMPLETE_NOTIFICATIONS,
            false
        )

        AppLifecycleManager.isLoginActivityLaunched = false
        // TODO: Inicialize o AdMob Banner
//        showAdBanner()
    }

    override fun onPause() {
//        adViewBanner.pause()
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        camViewModel.storePreference(Utils.PASSWORD.BIOMETRIC_SUCCESS, false)
    }

    override fun onDestroy() {
//        adViewBanner.destroy()
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(finishActivityReceiver)
        Log.d("CamActivity", "onDestroy")
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (isRecording) onStop()
    }


    // Implmenta o listener que notifica
    override fun onRecordingComplete() {
        val video = Video().apply {
            this.title = camViewModel.retrievePreference(Utils.FILE_NAME, "Video").toString()
            this.uri = camViewModel.retrievePreference(Utils.FILE_URI, "").toString()
        }
        camViewModel.saveVideo(video)
        if (showVideoRecordingCompleteNotifications) {
            camViewModel.completeVideoRecordingNotification(
                video.uri.toUri(),
                camViewModel.getVideoIdFromUri(video.uri)
            )
        }
        Toast.makeText(this, R.string.video_saved, Toast.LENGTH_SHORT).show()
        //TODO: Mostrar AdMob Interstitial
//        showAdMobInterstitial()
    }

    override fun onRecording() {
        showOnRecording()
        camViewModel.saveRecordingStatus(true)
    }

    override fun onRecordingError(msg: String) {
        binding.videoStopButton.performClick()

        if (showVideoRecordingCompleteNotifications) {
            camViewModel.errorVideoRecordingNotification(msg)
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        camViewModel.saveRecordingStatus(false)
        camViewModel.checkBothCameraAvailable()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setLayout(newConfig.orientation)
        if ((newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
                    || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
            && camViewModel.retrievePreference(Utils.STOP_RECORDING_SWITCH_SETTINGS, false)
        ) {
            binding.videoStopButton.performClick()
        }
    }

//    TODO: Implementar na produção
//    private fun showAdBanner() {
//        binding.adViewBanner.removeAllViews()
//        this.adViewBanner = camViewModel.adMobBanner()
//        binding.adViewBanner.addView(this.adViewBanner)
//    }

    private fun setLayout(orientation: Int) {
        val deviceResolution = camViewModel.getScreenResolution(windowManager, resources)
        deviceScreenWidthPx = deviceResolution[0]
        deviceScreenHeightPx = deviceResolution[1]

        val newHeight: Int
        val multi: Double = if (deviceScreenHeightPx <= 1280) {
            0.35
        } else 0.5

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            newHeight = (deviceScreenWidthPx * 0.2).toInt()
            binding.viewFinder.setPadding(8)
        } else {
            newHeight = (deviceScreenHeightPx * multi).toInt()
            binding.viewFinder.setPadding(16)
        }

        binding.viewFinder.layoutParams.height = newHeight
        binding.viewFinder.requestLayout()

        if (!isRecording) {
            setCams()

        } else showOnRecording()

        // Aqui corrigimos o bug de quando a aplicação fecha inesperadamente ou forçada a isso com
        // gravação em andamento quebra o layout na proxima inicialização
        if (isRecording && binding.previewCamButton.isVisible && !binding.videoStopButton.isVisible) {
            camViewModel.saveRecordingStatus(false)
            showOnNotRecording()
        }

        if (isPreviewRunning) {
            camViewModel.stopCamPreview()
            camViewModel.startCamPreview(binding.viewFinder, defaultCamera, this)
        }

    }

    private fun showOnRecording() {
        binding.recordingStatus.visibility = View.VISIBLE
        hiddenAllButtons()
        binding.videoStopButton.visibility = View.VISIBLE
        binding.videoCaptureButton.visibility = View.GONE
    }

    private fun showOnNotRecording() {
        binding.recordingStatus.visibility = View.GONE
        setCams()
        binding.videoStopButton.visibility = View.GONE
        binding.videoCaptureButton.visibility = View.VISIBLE
        binding.previewCamButton.visibility = View.VISIBLE
    }

    private fun observer() {
        camViewModel.isPreview.observe(this) {
            isPreviewRunning = it
            if (it) {
                binding.viewFinder.visibility = View.VISIBLE
                binding.previewCamButton.setBackgroundColor(getColor(R.color.buttonsOn))
                binding.previewCamButton.text = getString(R.string.hide_preview)
            } else {
                binding.viewFinder.visibility = View.GONE
                binding.previewCamButton.setBackgroundColor(getColor(R.color.buttonsOff))
                binding.previewCamButton.text = getString(R.string.preview_cam)
            }
        }

        camViewModel.isRecording.observe(this) {
            isRecording = it
        }

        camViewModel.hasBothCamera.observe(this) {
            if (it && !isRecording) {
                binding.backCamButton.visibility = View.VISIBLE
                binding.frontCamButton.visibility = View.VISIBLE
                setDefaultCamSettings()
            }
        }
    }

    private fun createObserver() {
        camViewModel.startRecordingOnBoot.observe(this) {
            if (it) {
                binding.videoCaptureButton.performClick()

                // Minimiza a activity (simula o botão Home)
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_HOME)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)

            }
        }
    }

    private fun listeners() {
        binding.videoCaptureButton.setOnClickListener {
            if (isPreviewRunning) camViewModel.stopCamPreview()
            binding.videoCaptureButton.text = getString(R.string.starting_recording)
            binding.videoCaptureButton.isEnabled = false
            binding.videoCaptureButton.isClickable = false

            if (!isRecording) {
//                camViewModel.startCamPreview(binding.viewFinder, defaultCamera, this)
                startBackgroundVideoService()
            }
        }

        binding.videoStopButton.setOnClickListener {
            if (backgroundVideoService != null) {
                binding.videoCaptureButton.text = getString(R.string.start_capture)
                binding.videoCaptureButton.isEnabled = true
                binding.videoCaptureButton.isClickable = true
                backgroundVideoService!!.stopRecording()
                stopBackgroundVideoService()
            } else {
                Toast.makeText(this, "Nenhuma gravação ativa", Toast.LENGTH_SHORT).show()
            }
            binding.previewCamButton.visibility = View.VISIBLE
            binding.recordingStatus.visibility = View.GONE
            binding.videoStopButton.visibility = View.GONE
            binding.videoCaptureButton.visibility = View.VISIBLE
            camViewModel.saveRecordingStatus(false)
            camViewModel.checkBothCameraAvailable()
        }

        binding.backCamButton.setOnClickListener {
            setDefaultCamSettings(true)
        }

        binding.frontCamButton.setOnClickListener {
            setDefaultCamSettings(true)
        }

        binding.previewCamButton.setOnClickListener {
            if (!isPreviewRunning) {
                camViewModel.startCamPreview(binding.viewFinder, defaultCamera, this)
                binding.previewCamButton.setBackgroundColor(getColor(R.color.black))
            } else {
                camViewModel.stopCamPreview()
                binding.previewCamButton.setBackgroundColor(getColor(R.color.buttonsOff))
            }
        }

        binding.appBarMain.toolbar.setNavigationOnClickListener {
            // Aqui setamos a settings activity para não ser apta para o login ao ser iniciada
            // dentro da CamActivity
            AppLifecycleManager.isSettingsActivityAdapted = false
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.appBarMain.galleryToolbar.setNavigationOnClickListener {
            loadFragment(GalleryFragment())
        }
    }
// TODO: Implementar na produção
//    private fun loadInterstitialCallBack(): InterstitialAdLoadCallback {
//        return object : InterstitialAdLoadCallback() {
//            override fun onAdFailedToLoad(adError: LoadAdError) {
//                Log.d("CamActivity", "Ad failed to load: $adError")
//                mInterstitialAd = null
//            }
//            override fun onAdLoaded(interstitialAd: InterstitialAd) {
//                Log.d("CamActivity", "Ad was loaded.")
//                mInterstitialAd = interstitialAd
//            }
//        }
//    }
//
//
//    private fun loadInterstitialFullScreenContentCallback(): FullScreenContentCallback {
//        return object : FullScreenContentCallback() {
//            override fun onAdDismissedFullScreenContent() {
//                super.onAdDismissedFullScreenContent()
//                Log.d("CamActivity", "Ad was dismissed.")
//                mInterstitialAd = null
//                camViewModel.adMobInterstitialLoader(loadInterstitialCallBack())
//            }
//
//            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
//                super.onAdFailedToShowFullScreenContent(p0)
//                Log.d("CamActivity", "Ad failed to show.")
//                mInterstitialAd = null
//                camViewModel.adMobInterstitialLoader(loadInterstitialCallBack())
//            }
//
//            override fun onAdShowedFullScreenContent() {
//                super.onAdShowedFullScreenContent()
//                Log.d("CamActivity", "Ad showed fullscreen content.")
//            }
//
//            override fun onAdImpression() {
//                super.onAdImpression()
//                Log.d("CamActivity", "Ad impression.")
//            }
//
//            override fun onAdClicked() {
//                super.onAdClicked()
//                Log.d("CamActivity", "Ad clicked.")
//            }
//        }.also { mInterstitialAd?.fullScreenContentCallback = it }
//    }
//
//    private fun showAdMobInterstitial() {
//        Log.d(
//            "CamActivity",
//            "My interstitial is ${if (mInterstitialAd != null) "not " else ""}null."
//        )
//        if (mInterstitialAd != null) {
//            loadInterstitialFullScreenContentCallback()
//            mInterstitialAd?.show(this)
//        } else {
//            Log.d("CamActivity", "The interstitial ad wasn't ready yet.")
//            camViewModel.adMobInterstitialLoader(loadInterstitialCallBack())
//            loadInterstitialFullScreenContentCallback()
//            mInterstitialAd?.show(this)
//        }
//    }

    private fun setCams() {
        camViewModel.hasBackCamera()
        camViewModel.hasFrontCamera()
        camViewModel.checkBothCameraAvailable()

    }

    private fun setDefaultCamSettings(inOnClick: Boolean = false) {

        if (if (inOnClick) defaultCamera != CameraSelector.DEFAULT_BACK_CAMERA
            else defaultCamera == CameraSelector.DEFAULT_BACK_CAMERA
        ) {
            defaultCamera = CameraSelector.DEFAULT_BACK_CAMERA
            if (isPreviewRunning) {
                camViewModel.stopCamPreview()
                camViewModel.startCamPreview(binding.viewFinder, defaultCamera, this)
            }
            camViewModel.storePreference(Utils.DEFAULT_CAMERA, getString(R.string.back_cam))
            binding.backCamButton.setBackgroundColor(getColor(R.color.buttonsOn))
            binding.frontCamButton.setBackgroundColor(getColor(R.color.buttonsOff))
            binding.backCamButton.isClickable = false
            binding.frontCamButton.isClickable = true
        } else if (if (inOnClick) defaultCamera != CameraSelector.DEFAULT_FRONT_CAMERA
            else defaultCamera == CameraSelector.DEFAULT_FRONT_CAMERA
        ) {
            defaultCamera = CameraSelector.DEFAULT_FRONT_CAMERA
            if (isPreviewRunning) {
                camViewModel.stopCamPreview()
                camViewModel.startCamPreview(binding.viewFinder, defaultCamera, this)
            }
            camViewModel.storePreference(Utils.DEFAULT_CAMERA, getString(R.string.front_cam))
            binding.frontCamButton.setBackgroundColor(getColor(R.color.buttonsOn))
            binding.backCamButton.setBackgroundColor(getColor(R.color.buttonsOff))
            binding.frontCamButton.isClickable = false
            binding.backCamButton.isClickable = true
        }
    }

    private fun hiddenAllButtons() {
        binding.backCamButton.visibility = View.GONE
        binding.frontCamButton.visibility = View.GONE
        binding.previewCamButton.visibility = View.GONE
    }

    private fun startBackgroundVideoService() {
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)
        serviceIntent.action = "START_SERVICE"
        startService(serviceIntent)
    }

    private fun stopBackgroundVideoService() {
        serviceIntent.action = "STOP_SERVICE"
        stopService(serviceIntent)
//        unbindService(serviceConnection)
    }

    private fun loadFragment(fragment: Fragment) {
        // Obtém o FragmentManager para gerenciar fragments nesta activity
        val fragmentManager = supportFragmentManager

        // Inicia uma FragmentTransaction
        val fragmentTransaction = fragmentManager.beginTransaction()

        // Substitui o contêiner existente pelo novo fragment
        // Use add() se quiser adicionar em cima de algo que já está no contêiner
        fragmentTransaction.add(R.id.fragment_container, fragment)

//         Opcional: Adiciona a transação à back stack.
//         Isso permite que o usuário volte para a activity sem o fragment
//         pressionando o botão "Voltar".
        fragmentTransaction.addToBackStack(null)

        // Confirma a transação. As operações são executadas assincronamente.
        fragmentTransaction.commit()
    }
}

