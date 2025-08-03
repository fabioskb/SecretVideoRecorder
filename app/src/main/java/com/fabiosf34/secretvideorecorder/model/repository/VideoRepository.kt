package com.fabiosf34.secretvideorecorder.model.repository

import android.content.Context
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.net.toUri
import com.fabiosf34.secretvideorecorder.model.Video
import java.io.File

class VideoRepository(val context: Context) {
    private val videoDB = VideoDB.getDB(context).videoDao()

    fun insert(video: Video): Boolean = videoDB.insert(video) > 0

    fun delete(videoId: Int) {
        val video = get(videoId)
        videoDB.delete(video)
    }

    fun get(id: Int): Video = videoDB.get(id)

    fun getVideoIdFromUri(uri: String): Int = videoDB.getVideoIdFromUri(uri)

    fun getAll(): List<Video> {
        val allVideos = mutableListOf<Video>()
        val allVideosOnStorage = getVideosTitleFromStorage()
        for (video in videoDB.getAll()) {
            if (allVideosOnStorage.contains(video.title)) {
                allVideos.add(video)
            } else {
                delete(video.id)
            }
        }
        return allVideos
    }

    fun deleteVideoFromStorage(videoUriString: String): Boolean {
        val fileUri = videoUriString.toUri()

        try {
            if ("file".equals(fileUri.scheme, ignoreCase = true)) {
                // Se for uma file COLUMN_URI, sempre tentaremos excluir como um arquivo direto.
                // Isso cobre o armazenamento interno em todas as versões
                // e o armazenamento externo público antes do Android Q (com permissão WRITE_EXTERNAL_STORAGE).
                val filePath = fileUri.path
                if (filePath != null) {
                    val videoFile = File(filePath)
                    if (videoFile.exists()) {
                        if (videoFile.delete()) {
                            Log.d("VideoRepository", "Arquivo (file COLUMN_URI) excluído com sucesso: $filePath")
                            return true
                        } else {
                            Log.e("VideoRepository", "Falha ao excluir o arquivo (file COLUMN_URI): $filePath")
                            // Poderia haver um problema de permissão aqui para arquivos externos em APIs < Q
                            // se WRITE_EXTERNAL_STORAGE não estiver concedida ou se o arquivo estiver bloqueado.
                            return false
                        }
                    } else {
                        Log.w("VideoRepository", "Arquivo (file COLUMN_URI) não encontrado para exclusão: $filePath")
                        return false // Ou true, dependendo da sua lógica de "já excluído"
                    }
                } else {
                    Log.e("VideoRepository", "Caminho do arquivo nulo para file COLUMN_URI: $fileUri")
                    return false
                }
            } else if ("content".equals(fileUri.scheme, ignoreCase = true)) {
                // Se for uma content COLUMN_URI, provavelmente é do MediaStore (ou do seu FileProvider,
                // mas este mét_odo é mais para exclusão de "fontes" de vídeo).
                // A exclusão via ContentResolver é apropriada aqui.
                // Para Android Q+ (API 29+), isso geralmente só funciona para arquivos que seu app criou
                // ou se você obteve consentimento via createDeleteRequest (API 30+).
                // Para APIs < 29, você precisaria da permissão WRITE_EXTERNAL_STORAGE se a content COLUMN_URI
                // apontar para o armazenamento externo.

                // Não há necessidade de verificar a versão do SDK aqui para a chamada delete em si,
                // pois o ContentResolver.delete se comporta de acordo com as regras da plataforma.
                // A verificação de permissão (WRITE_EXTERNAL_STORAGE para < Q, ou gerenciamento de Scoped Storage para Q+)
                // deve ser tratada no nível do aplicativo antes de chamar este mét_odo, ou o sistema lançará SecurityException.
                try {
                    val rowsDeleted = context.contentResolver.delete(fileUri, null, null)
                    if (rowsDeleted > 0) {
                        Log.d("VideoRepository", "Arquivo (content COLUMN_URI) excluído com sucesso via ContentResolver: $fileUri")
                        return true
                    } else {
                        Log.w("VideoRepository", "Nenhum arquivo excluído via ContentResolver para content COLUMN_URI: $fileUri (pode já ter sido excluído ou não encontrado)")
                        // Isso pode acontecer se a COLUMN_URI não corresponder a um item ou se o item já foi excluído.
                        // Também pode falhar silenciosamente (rowsDeleted = 0) em Q+ se não for seu arquivo e você não tiver consentimento.
                        return false
                    }
                } catch (secEx: SecurityException) {
                    Log.e("VideoRepository", "SecurityException ao excluir content COLUMN_URI: $fileUri. Falta de permissão ou restrição de Scoped Storage.", secEx)
                    // Em Android Q+, se tentar excluir um arquivo de mídia que seu aplicativo não possui
                    // sem o consentimento adequado, uma RecoverableSecurityException (API 29) ou
                    // SecurityException (API 30+ para MediaStore.createDeleteRequest não usado) pode ser lançada.
                    // Para < Q, seria falta de WRITE_EXTERNAL_STORAGE.
                    return false
                } catch (e: Exception) {
                    // Captura outras exceções que podem ocorrer com contentResolver.delete
                    Log.e("VideoRepository", "Exceção ao excluir content COLUMN_URI via ContentResolver: $fileUri", e)
                    return false
                }
            } else {
                Log.w("VideoRepository", "Esquema de COLUMN_URI não suportado para exclusão: $fileUri")
                return false
            }
        } catch (e: Exception) {
            Log.e("VideoRepository", "Erro geral ao tentar excluir vídeo do armazenamento: $videoUriString", e)
            return false
        }
    }

//    fun getVideoTitleFromStorage(videoUri: String): String {
//        val contentResolver = context.contentResolver
//        val uri = videoUri.toUri()
//        val cursor = contentResolver.query(uri, null, null, null, null)
//        cursor?.moveToFirst()
//        val title = cursor?.getString(cursor.run { getColumnIndex(OpenableColumns.DISPLAY_NAME) })
//        cursor?.close()
//        return title ?: ""
//    }

    fun getVideosTitleFromStorage(): List<String> {
        val contentResolver = context.contentResolver
        val videoList = videoDB.getAll()
        val videoTitles = mutableListOf<String>()
        for (video in videoList) {
//            val uri = video.uri.toUri() // Converte a string COLUMN_URI para objeto Uri
//            Log.d("VideoRepository", "Tentando obter título para COLUMN_URI: $uri com scheme: ${uri.scheme}")
//            val cursor = contentResolver.query(uri, null, null, null, null)
//            cursor?.moveToFirst()
//            var title = ""
//            if (cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME) >= 0 && cursor!!.moveToFirst()) {
//                title =
//                    cursor.getString(cursor.run { getColumnIndex(OpenableColumns.DISPLAY_NAME) }).toString()
//            }
//            cursor.close()
//            videoTitles.add(title)
//        }
//        return videoTitles
            var title = "" // Inicializa o título
            try {
                val uri = video.uri.toUri() // Converte a string COLUMN_URI para objeto Uri
                Log.d("VideoRepository", "Tentando obter título para COLUMN_URI: $uri com scheme: ${uri.scheme}")

                // Tenta primeiro com ContentResolver (bom para MediaStore URIs ou FileProvider URIs)
                val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
                contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val displayNameColumnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (displayNameColumnIndex != -1) {
                            title = cursor.getString(displayNameColumnIndex) ?: ""
                            Log.d("VideoRepository", "Título obtido via ContentResolver para $uri: $title")
                        } else {
                            Log.w("VideoRepository", "Coluna DISPLAY_NAME não encontrada para $uri via ContentResolver.")
                            // Fallback se a coluna não existir
                            if (title.isEmpty()) { // Só aplica fallback se o título ainda estiver vazio
                                title = uri.lastPathSegment ?: ""
                                Log.d("VideoRepository", "Título obtido via fallback (lastPathSegment) para $uri: $title")
                            }
                        }
                    } else {
                        Log.w("VideoRepository", "Cursor vazio para $uri via ContentResolver.")
                        // Fallback se o cursor estiver vazio
                        if (title.isEmpty()) {
                            title = uri.lastPathSegment ?: ""
                            Log.d("VideoRepository", "Título obtido via fallback (lastPathSegment) para $uri: $title")
                        }
                    }
                } ?: run {
                    // Se contentResolver.query() retornou null (cursor foi nulo)
                    Log.w("VideoRepository", "contentResolver.query() retornou null para COLUMN_URI: $uri. Usando fallback.")
                    title = uri.lastPathSegment ?: ""
                    Log.d("VideoRepository", "Título obtido via fallback (lastPathSegment) para $uri: $title")
                }

                // Se o título ainda estiver vazio e for uma COLUMN_URI 'file', tente novamente o lastPathSegment
                // (Isso pode ser redundante se o fallback acima já o pegou, mas garante)
                if (title.isEmpty() && "file".equals(uri.scheme, ignoreCase = true)) {
                    title = uri.lastPathSegment ?: ""
                    Log.d("VideoRepository", "Título obtido via fallback final (lastPathSegment para file COLUMN_URI) para $uri: $title")
                }

            } catch (e: Exception) {
                Log.e("VideoRepository", "Erro ao obter título para COLUMN_URI: ${video.uri}. Erro: ${e.message}", e)
                // Tenta um último fallback em caso de exceção, se a COLUMN_URI for parsable
                try {
                    val fallbackUri = video.uri.toUri()
                    title = fallbackUri.lastPathSegment ?: ""
                    Log.d("VideoRepository", "Título obtido via fallback de exceção (lastPathSegment) para ${video.uri}: $title")
                } catch (parseEx: Exception) {
                    Log.e("VideoRepository", "Erro ao parsear COLUMN_URI no fallback de exceção: ${video.uri}", parseEx)
                }
            }
            videoTitles.add(title.ifEmpty { "Título Desconhecido" }) // Adiciona um placeholder se tudo falhar
        }
        return videoTitles
    }

    fun deleteAllVideos() {
        for (video in videoDB.getAll()) {
            delete(video.id)
        }
    }

    fun deleteAllVideosFromStorage() {
        val videosUri = videoDB.getAll().map { it.uri }
        for (uri in videosUri) {
            deleteVideoFromStorage(uri)
        }
    }
}