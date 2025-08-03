package com.fabiosf34.secretvideorecorder.model.utilities

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.media.MediaMetadataRetriever
import android.util.Log
import android.widget.ImageView
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import androidx.core.graphics.scale

class ImagesUtils(val context: Context) {
    /**
     * Extrai um frame de um vídeo como um Bitmap (thumbnail).
     *
     * @param context Contexto da aplicação.
     * @param videoUri O Uri do arquivo de vídeo.
     * @param timeUs O tempo em MICROSEGUNDOS de onde extrair o frame.
     *               Use MediaMetadataRetriever.OPTION_CLOSEST_SYNC para o key frame mais próximo,
     *               ou MediaMetadataRetriever.OPTION_CLOSEST para o frame mais próximo (pode ser mais lento).
     * @return Um Bitmap do frame, ou null se a extração falhar.
     */
    fun getVideoThumbnail(
        videoUri: Uri,
        timeUs: Long = 1000000L, /* 1 segundo */
    ): Bitmap? {
        val retriever = MediaMetadataRetriever()
        var bitmap: Bitmap? = null

        try {
            // Para Uris de conteúdo (content://) ou de arquivo (file://)
            retriever.setDataSource(context, videoUri)

            // Tenta pegar um frame em um tempo específico.
            // MediaMetadataRetriever.OPTION_CLOSEST_SYNC é geralmente mais rápido e pega um key frame.
            bitmap =
                retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)

            // Fallback: Se não conseguiu um frame no tempo exato, tenta pegar qualquer frame (o primeiro)
            if (bitmap == null) {
                bitmap = retriever.frameAtTime // Pega o primeiro frame disponível
            }

            // Você também pode escalar o bitmap aqui se precisar de um tamanho específico
            // para economizar memória, antes de retorná-lo.
            // Exemplo: val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 128, 128, false)
            // return scaledBitmap

        } catch (e: IllegalArgumentException) {
            Log.e(
                "ThumbnailUtils",
                "IllegalArgumentException ao configurar a fonte de dados: ${e.message}"
            )
        } catch (e: RuntimeException) {
            Log.e("ThumbnailUtils", "RuntimeException ao recuperar o frame: ${e.message}")
        } catch (e: IOException) {
            Log.e(
                "ThumbnailUtils",
                "IOException, verifique o arquivo de vídeo e permissões: ${e.message}"
            )
        } finally {
            try {
                retriever.release()
            } catch (e: IOException) {
                Log.e("ThumbnailUtils", "Erro ao liberar MediaMetadataRetriever: ${e.message}")
            }
        }
        return bitmap
    }

    /**
     * Alternativa para extrair thumbnail usando o construtor getScaledFrameAtTime (API 27+).
     * Permite especificar dimensões diretamente, o que pode ser mais eficiente.
     *
     * @param context Contexto da aplicação.
     * @param videoUri O Uri do arquivo de vídeo.
     * @param timeUs O tempo em MICROSEGUNDOS de onde extrair o frame.
     * @param width A largura desejada para o thumbnail.
     * @param height A altura desejada para o thumbnail.
     * @return Um Bitmap do frame escalado, ou null se a extração falhar.
     */
    fun getScaledVideoThumbnail(
        videoUri: Uri,
        timeUs: Long = 1000000L, /* 1 segundo */
        width: Int,
        height: Int,
    ): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            // Para versões anteriores, use o mét_odo tradicional e escale manualmente se necessário
            val bmp = getVideoThumbnail(videoUri, timeUs)
            return bmp?.scale(width, height, false)
        }

        val retriever = MediaMetadataRetriever()
        var bitmap: Bitmap? = null

        try {
            retriever.setDataSource(context, videoUri)
            bitmap = retriever.getScaledFrameAtTime(
                timeUs,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                width,
                height
            )

            if (bitmap == null) { // Fallback
                bitmap = retriever.getScaledFrameAtTime(
                    -1,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                    width,
                    height
                )
            }

        } catch (e: IllegalArgumentException) {
            Log.e("ThumbnailUtils", "IllegalArgumentException (scaled): ${e.message}")
        } catch (e: RuntimeException) {
            Log.e("ThumbnailUtils", "RuntimeException (scaled): ${e.message}")
        } catch (e: IOException) {
            Log.e("ThumbnailUtils", "IOException (scaled): ${e.message}")
        } finally {
            try {
                retriever.release()
            } catch (e: IOException) {
                Log.e(
                    "ThumbnailUtils",
                    "Erro ao liberar MediaMetadataRetriever (scaled): ${e.message}"
                )
            }
        }
        return bitmap
    }

    fun loadVideoThumbnail(uri: String, imageView: ImageView) {
        CoroutineScope(Dispatchers.IO).launch {
            val thumbnail = getScaledVideoThumbnail(uri.toUri(), 2000000L, 44, 44)

            withContext(Dispatchers.Main) {
                if (thumbnail != null) {
                    imageView.setImageBitmap(thumbnail)
                    imageView.visibility = ImageView.VISIBLE
                } else {
                    imageView.visibility = ImageView.GONE
//                    imageView.setImageResource(R.drawable.ic_default_video_placeholder)
//                    imageView.setImageDrawable(R.drawable.ic_default_video_placeholder as? Drawable)
                }
            }
        }
    }
}
