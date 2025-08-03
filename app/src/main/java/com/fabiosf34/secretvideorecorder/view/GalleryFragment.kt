package com.fabiosf34.secretvideorecorder.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.fabiosf34.secretvideorecorder.R
import com.fabiosf34.secretvideorecorder.databinding.FragmentGalleryBinding
import com.fabiosf34.secretvideorecorder.model.listeners.OnVideoListener
import com.fabiosf34.secretvideorecorder.model.utilities.Utils
import com.fabiosf34.secretvideorecorder.view.adapter.GalleryAdapter
import com.fabiosf34.secretvideorecorder.viewModel.GalleryViewModel

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private lateinit var galleryViewModel: GalleryViewModel

    private val adapter = GalleryAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        galleryViewModel = ViewModelProvider(this)[GalleryViewModel::class.java]
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.recyclerVideos.layoutManager = layoutManager
        binding.recyclerVideos.adapter = adapter

        listeners()

//        activity?.window?.decorView?.background?.let { activityBackground ->
//            view?.background = activityBackground.constantState?.newDrawable()
//        }

        return binding.root

    }

    override fun onResume() {
        super.onResume()
        galleryViewModel.getVideos()
        observe()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observe() {
        galleryViewModel.videos.observe(viewLifecycleOwner) {
            adapter.updateVideos(it)
        }
        galleryViewModel.videoDeleted.observe(viewLifecycleOwner) {
            adapter.updateVideos(galleryViewModel.videos.value!!)
        }
        galleryViewModel.isVideoDeleted.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(context, R.string.video_deleted, Toast.LENGTH_SHORT).show()
            } else Toast.makeText(context, R.string.video_not_deleted, Toast.LENGTH_SHORT).show()
        }
        galleryViewModel.isAllVideosDeleted.observe(viewLifecycleOwner) {
            if (it) {
                adapter.updateVideos(galleryViewModel.videos.value!!)
                Toast.makeText(context, R.string.all_videos_deleted, Toast.LENGTH_SHORT).show()
            } else Toast.makeText(context, R.string.all_videos_not_deleted, Toast.LENGTH_SHORT)
                .show()
        }
    }

    /**
     * Verifica se o dispositivo está atualmente no modo escuro.
     *
     * @param context O contexto para acessar os serviços do sistema.
     * @return True se o dispositivo estiver no modo escuro, False caso contrário.
     */
//    private fun isDeviceInDarkMode(): Boolean {
//        val currentNightMode =
//            context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
//        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
//    }

    private fun listeners() {
        val listener = object : OnVideoListener {
            override fun onClick(uri: String) {
//                Toast.makeText(context, galleryViewModel.getVideoTitleFromStorage(uri), Toast.LENGTH_SHORT)
//                    .show()
                galleryViewModel.playVideo(uri.toUri(), requireContext())
            }

            override fun onDelete(id: Int, uri: String) {
                galleryViewModel.delete(id)
                galleryViewModel.getVideos()
                galleryViewModel.deleteVideoFromStorage(uri)
            }
        }

        binding.btnDeleteAllVideos.setOnClickListener {
            if (galleryViewModel.videos.value!!.isEmpty()) {
                Toast.makeText(context, R.string.no_videos_to_delete, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Utils.AppUtils.dialog(
                requireContext(),
                R.string.delete_all_videos,
                R.string.delete_all_videos_message,
                getString(R.string.yes),
                getString(R.string.no),
                { dialog, which ->
                    galleryViewModel.deleteAllVideos()
                    adapter.deleteAllVideos()
                    galleryViewModel.getVideos()
                }
            )
        }

        adapter.attachListener(listener)
    }
}