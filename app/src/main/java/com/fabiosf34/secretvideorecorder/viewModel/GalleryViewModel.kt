package com.fabiosf34.secretvideorecorder.viewModel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fabiosf34.secretvideorecorder.model.Video
import com.fabiosf34.secretvideorecorder.model.repository.VideoRepository
import java.io.File

class GalleryViewModel(application: Application) : AndroidViewModel(application) {
    private val galleryRepository = VideoRepository(application)


    private val _videos = MutableLiveData<List<Video>>()
    val videos: LiveData<List<Video>> = _videos

    private val _videoDeleted = MutableLiveData<Video>()
    val videoDeleted: LiveData<Video> = _videoDeleted

    private val _isVideoDeleted = MutableLiveData<Boolean>()
    val isVideoDeleted: LiveData<Boolean> = _isVideoDeleted

    private val _isAllVideosDeleted = MutableLiveData<Boolean>()
    val isAllVideosDeleted: LiveData<Boolean> = _isAllVideosDeleted

    fun delete(videoId: Int) {
        _videoDeleted.value = galleryRepository.get(videoId)
        galleryRepository.delete(videoId)
    }

    fun getVideos() {
        _videos.value = galleryRepository.getAll()
    }


    fun playVideo(videoSourceUri: Uri, context: Context) {
        Log.d("GalleryViewModel", "Tentando reproduzir vídeo com URI original: $videoSourceUri")

        if (!"file".equals(videoSourceUri.scheme, ignoreCase = true)) {
            Log.e("GalleryViewModel", "URI de origem inesperada. Esperava 'file://', mas recebi: ${videoSourceUri.scheme}")
            // Você pode querer mostrar um erro para o usuário aqui ou lidar com isso de outra forma.
            // Toast.makeText(context, "Erro: Formato de URI de vídeo inválido", Toast.LENGTH_LONG).show()
            return
        }

        val videoPath = videoSourceUri.path
        if (videoPath == null) {
            Log.e("GalleryViewModel", "Caminho da URI do arquivo é nulo: $videoSourceUri")
            // Toast.makeText(context, "Erro: Caminho do vídeo inválido", Toast.LENGTH_LONG).show()
            return
        }

        val videoFile = File(videoPath)

        if (!videoFile.exists()) {
            Log.e("GalleryViewModel", "Arquivo de vídeo não encontrado no caminho: $videoPath")
            // Toast.makeText(context, "Erro: Arquivo de vídeo não encontrado", Toast.LENGTH_LONG).show()
            // Considere remover a entrada do banco de dados se o arquivo não existe mais.
            // Isso pode ser uma boa oportunidade para sincronizar.
            // galleryRepository.deleteVideoByUri(videoSourceUri.toString()) // Exemplo de função a ser criada
            // getVideos() // Recarregar vídeos
            return
        }

        try {
            val authority = "${context.packageName}.provider"
            // Gera uma content:// URI segura usando FileProvider
            val contentUriForVideo = FileProvider.getUriForFile(context, authority, videoFile)
//            val contentUriForVideo = videoSourceUri

            Log.d("GalleryViewModel", "URI de conteúdo gerada para reprodução: $contentUriForVideo")

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUriForVideo, "video/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Concede permissão de leitura temporária
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)      // Necessário se chamado de um contexto não-Activity (como ViewModel)
                // ou se você quer uma nova task para o player.
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Log.e("GalleryViewModel", "Nenhum aplicativo encontrado para reproduzir o vídeo: $contentUriForVideo")
                // Toast.makeText(context, "Nenhum player de vídeo encontrado", Toast.LENGTH_LONG).show()
            }

        } catch (e: IllegalArgumentException) {
            // Esta exceção pode ser lançada por FileProvider.getUriForFile se o caminho do arquivo
            // não estiver configurado corretamente no seu res/xml/file_paths.xml
            Log.e("GalleryViewModel", "Erro de FileProvider ao obter URI (caminho não configurado em file_paths.xml?): $videoFile", e)
            // Toast.makeText(context, "Erro ao preparar vídeo para reprodução", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            // Captura outras exceções (ex: ActivityNotFoundException se resolveActivity falhar de alguma forma, embora raro se verificado)
            Log.e("GalleryViewModel", "Erro ao tentar reproduzir vídeo: $videoSourceUri", e)
            // Toast.makeText(context, "Erro ao tentar reproduzir vídeo", Toast.LENGTH_LONG).show()
        }
    }


    fun deleteVideoFromStorage(videoUri: String) {
        _isVideoDeleted.value = galleryRepository.deleteVideoFromStorage(videoUri)
    }

    fun deleteAllVideos() {
        Log.d("GalleryViewModel", "deleteAllVideos called")
        galleryRepository.deleteAllVideosFromStorage()
        galleryRepository.deleteAllVideos()
        _isAllVideosDeleted.value = galleryRepository.getAll().isEmpty()
    }

}