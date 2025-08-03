package com.fabiosf34.secretvideorecorder.model.utilities

import android.content.Context
import android.os.Environment
import android.os.storage.StorageManager
import android.util.Log
import com.fabiosf34.secretvideorecorder.model.repository.Preferences
import java.io.File

class StorageHelper(val context: Context) {
    private val preferences = Preferences(context)
    private val tag = "StorageHelper"
    private var sdCardVideoDir: File? = null
    private var targetVideoDir: File? = null

    /**
     * Obtém o diretório de armazenamento de vídeo específico do aplicativo,
     * priorizando um cartão SD removível, se disponível e gravável.
     * Caso contrário, usa o armazenamento externo primário (geralmente interno emulado).
     *
     * @param context O contexto do aplicativo.
     * @return Um objeto File apontando para o diretório de vídeo, ou null se nenhum for encontrado/criado.
     */

    fun hasSdCardVideoDir(): Boolean {
        /**
         * Tentar encontrar um cartão SD removível, gravavel e com permissão do usuario
         * Se não encontrar, usar o armazenamento externo primário (geralmente interno emulado)
         *
         * @return Boolean indicando se o diretório de vídeo do cartão SD foi encontrado/criado
         */
        val externalStorageVolumes: Array<File> =
            context.getExternalFilesDirs(Environment.DIRECTORY_MOVIES)
        Log.d("StorageHelper", "externalStorageVolumes: ${externalStorageVolumes.getOrNull(0)}")
        var hasSdCardVideoDir = false
        val isSDCardSelected = preferences.retrieve(Utils.SAVE_VIDEO_ON_SD_CARD, false)

        // Tentar encontrar um cartão SD removível
        // getExternalFilesDirs e isExternalStorageRemovable
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        for (volumePath in externalStorageVolumes) {
            var volumePath = volumePath
            try {
                if (storageManager.getStorageVolume(volumePath)?.isRemovable == true && storageManager.getStorageVolume(
                        volumePath
                    )?.state == Environment.MEDIA_MOUNTED
                ) {
                    hasSdCardVideoDir = true
                    Log.d(
                        tag,
                        "Cartão SD (removível) encontrado para vídeos específicos do app: ${volumePath.absolutePath}"
                    )
                    volumePath = File(volumePath.absolutePath)
                    sdCardVideoDir = volumePath
                    break
                }
            } catch (e: Exception) {
                // Algumas ROMs mais antigas ou personalizadas podem lançar exceções aqui
                Log.e(tag, "Erro ao verificar o volume de armazenamento: $volumePath", e)
            }
        }

        if (!isSDCardSelected) {
            sdCardVideoDir = null
        }
        targetVideoDir = sdCardVideoDir ?: externalStorageVolumes.getOrNull(0)

        return hasSdCardVideoDir
    }

    fun getPrioritizedAppSpecificVideoStorageDirectory(): File? {
//        val externalStorageVolumes: Array<File> =
//            context.getExternalFilesDirs(Environment.DIRECTORY_MOVIES)
//        val isSDCardSelected = preferences.retrieve(Utils.SAVE_VIDEO_ON_SD_CARD, false)
//
//
//        if (!hasSdCardVideoDir() || !isSDCardSelected) {
//            sdCardVideoDir = null
//        }
//
//        val targetVideoDir: File? = sdCardVideoDir ?: externalStorageVolumes.getOrNull(0)

        hasSdCardVideoDir()
        if (targetVideoDir == null) {
            Log.e(
                tag,
                "Nenhum diretório de armazenamento externo (primário ou SD) específico do app para Movies disponível."
            )
            // Tentar um fallback para um diretório genérico no armazenamento primário
            val genericFileDir = context.getExternalFilesDir(null)
            if (genericFileDir == null) {
                Log.e(
                    tag,
                    "Diretório de armazenamento externo específico do app (genérico) também não está disponível."
                )
                return null
            }
            val customVideoDir = File(genericFileDir, "Videos_Fallback")
            if (!customVideoDir.exists() && !customVideoDir.mkdirs()) {
                Log.e(tag, "Falha ao criar o diretório de fallback 'Videos_Fallback'.")
                return null
            }
            Log.d(tag, "Usando fallback genérico: ${customVideoDir.absolutePath}")
            return customVideoDir
        }

        if (!targetVideoDir!!.exists()) {
            if (!targetVideoDir!!.mkdirs()) {
                Log.e(
                    tag,
                    "Falha ao criar o diretório específico do app (Movies) em: ${targetVideoDir!!.absolutePath}"
                )
                return null
            }
        }
        Log.d(tag, "Usando diretório de vídeo: ${targetVideoDir!!.absolutePath}")
        return targetVideoDir
    }
}