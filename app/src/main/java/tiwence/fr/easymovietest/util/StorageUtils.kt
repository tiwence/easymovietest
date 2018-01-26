package tiwence.fr.easymovietest.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import tiwence.fr.easymovietest.MainActivity
import java.io.*

/**
 * Created by Tiwence on 23/01/2018.
 */

class StorageUtils {

    val EASY_MOVIE_DIR = "easymovie"

    lateinit var context:Context

    constructor(context: Context) {
        this.context = context
    }

    companion object {
        @Volatile private var INSTANCE: StorageUtils? = null

        fun getInstance(context: Context): StorageUtils {
            if (INSTANCE == null) {
                INSTANCE = StorageUtils(context)
            }
            return INSTANCE!!
        }
    }

    /**
     * Used to store the sample videos from the app to the external storage
     */
    fun writeLocalFilesToExternalStorage(resourceId: Int, resourceName: String) {
        if (isExternalStorageWritable()) {
            val rootDirectory = getVideoDirectoryStorageDir()
            try {
                val input = context.resources.openRawResource(resourceId)
                var out: FileOutputStream? = null
                out = FileOutputStream(rootDirectory!!.absolutePath + "/" + resourceName)
                input.copyTo(out, 1024)
                input.close()
                out!!.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /* Checks if external storage is available for read and write */
    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /**
     * Function used to store the recorded video by the user
     */
    fun storeRecordedVideoUri(videoUri: Uri) : String? {
        val outputPath = getVideoDirectoryStorageAbsolutePath() + "/recorded.mp4"
        try {
            val input = context.contentResolver.openInputStream(videoUri)
            var out: OutputStream = FileOutputStream(outputPath)
            input.copyTo(out, 1024)
            input.close()
            out.close()
            return outputPath
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null;
    }

    /**
     * Function used to get the externel storage where is stored all videos
     */
    fun getVideoDirectoryStorageDir(): File? {
        val file = File(context.getExternalFilesDir(
                Environment.DIRECTORY_MOVIES), EASY_MOVIE_DIR)
        if (!file?.mkdirs()) {
            file.mkdirs()
        }
        return file
    }

    fun getVideoDirectoryStorageAbsolutePath(): String? {
        return this.getVideoDirectoryStorageDir()?.absolutePath
    }
}