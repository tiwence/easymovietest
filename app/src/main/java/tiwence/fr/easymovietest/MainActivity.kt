package tiwence.fr.easymovietest

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import kotlinx.android.synthetic.main.activity_main.*
import android.provider.MediaStore
import android.content.Intent
import android.net.Uri
import tiwence.fr.easymovietest.util.StorageUtils
import java.io.*

class MainActivity : AppCompatActivity() {

    lateinit var ffmpeg: FFmpeg

    private val REQUEST_VIDEO_CAPTURE = 1

    private val REQUEST_VIDEO_RECORDED = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ffmpeg = FFmpeg.getInstance(this)

        buttonRecord.setOnClickListener { view ->
            //dispatchTakeVideoIntent()
            val intent = Intent(this, RecordingActivity::class.java)
            startActivityForResult(intent, REQUEST_VIDEO_RECORDED)
        }

        buttonWatch.setOnClickListener { view ->
            val outputPathString = StorageUtils.getInstance(this).getVideoDirectoryStorageAbsolutePath() + "/output.mp4"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(outputPathString))
            intent.setDataAndType(Uri.parse(outputPathString), "video/mp4")
            startActivity(intent)
        }

        ffmpeg.loadBinary(object : LoadBinaryResponseHandler() {

            override fun onStart() { }

            override fun onFailure() { }

            override fun onSuccess() {
                Log.d(MainActivity::class.java.name, "Load FFmpeg binary success")
            }

            override fun onFinish() { }
        })

        StorageUtils.getInstance(this).writeLocalFilesToExternalStorage(R.raw.r1, "r1.mp4")
        StorageUtils.getInstance(this).writeLocalFilesToExternalStorage(R.raw.r2, "r2.mp4")
        StorageUtils.getInstance(this).writeLocalFilesToExternalStorage(R.raw.r4, "r4.mp4")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode === REQUEST_VIDEO_RECORDED){
            concatenateVideoFiles(StorageUtils.getInstance(this).getVideoDirectoryStorageAbsolutePath() + "/recorded.mp4")
        }
    }

    /**
     * Function used to truncate the recorded videos by the user
     */
    /*private fun truncateVideo(recordedPath: String?) {
        val truncatedFile: File = File( StorageUtils.getInstance(this).getVideoDirectoryStorageAbsolutePath()
                + "/" + File(recordedPath).nameWithoutExtension + "_truncated.mp4")
        truncatedFile.delete()

        val cmd = arrayOf("-t", "00:00:05.6", "-i", recordedPath,
                "-vf", "pad=ih*16/9:ih:(ow-iw)/2:(oh-ih)/2,scale=1920:1080,setdar=16/9", //to avoid portrait video aspect ratio
                "-c:v", "libx264",
                "-strict", "experimental", "-preset", "ultrafast",
                truncatedFile.absolutePath)

        ffmpeg.execute(cmd, object : ExecuteBinaryResponseHandler() {
            override fun onStart() {
                super.onStart()
                buttonRecord.isEnabled = false
                buttonWatch.isEnabled = false
                progressBar.visibility = View.VISIBLE
                statusTextView.text = getText(R.string.truncate)
            }

            override fun onProgress(message: String?) { }

            override fun onFailure(message: String?) {
                buttonRecord.isEnabled = true
                progressBar.visibility = View.INVISIBLE
                statusTextView.text = "Error " + message;
            }

            override fun onSuccess(message: String?) {
                File(recordedPath).delete()
                concatenateVideoFiles(truncatedFile.absolutePath)
            }

            override fun onFinish() {
                super.onFinish()
            }
        })
    }*/

    /**
     * Function used to concatenate video files with the recorded according to the specification
     */
    private fun concatenateVideoFiles(recordedPath: String?) {

        val file1: String = StorageUtils.getInstance(this).getVideoDirectoryStorageAbsolutePath() + "/r1.mp4"
        val file2: String = StorageUtils.getInstance(this).getVideoDirectoryStorageAbsolutePath() + "/r2.mp4"
        val file4: String = StorageUtils.getInstance(this).getVideoDirectoryStorageAbsolutePath() + "/r4.mp4"

        //Output file finally  used
        val outputFile: File = File(StorageUtils.getInstance(this).getVideoDirectoryStorageAbsolutePath() + "/output.mp4")
        outputFile.delete()

        //We concatenate all video files in a single video
        val cmd = arrayOf("-i", file1, "-i", file2, "-i", recordedPath, "-i", file4,
                "-filter_complex", "[0:v:0] [0:a:0] [1:v:0] [1:a:0] [2:v:0] [2:a:0] [3:v:0] [3:a:0] concat=n=4:v=1:a=1 [v] [a]",
                "-map", "[v]", "-map", "[a]",
                "-s", "1920x1080",
                "-t", "00:00:17.92",
                "-c:v", "libx264",
                "-strict", "experimental", "-preset", "ultrafast",
                outputFile.absolutePath)

        ffmpeg.execute(cmd, object : ExecuteBinaryResponseHandler() {

            override fun onStart() {
                super.onStart()
                buttonRecord.isEnabled = false
                statusTextView.text = getText(R.string.concatenate)
                progressBar.visibility = View.VISIBLE
            }

            override fun onProgress(message: String?) {
                super.onProgress(message)
            }

            override fun onFailure(message: String?) {
                super.onFailure(message)
                buttonRecord.isEnabled = true
                statusTextView.text = "Error " + message;
            }

            override fun onSuccess(message: String?) {
                super.onSuccess(message)
                statusTextView.text = getText(R.string.finished)
                buttonWatch.isEnabled = true
                buttonRecord.isEnabled = true
            }

            override fun onFinish() {
                super.onFinish()
                File(recordedPath).delete()
                progressBar.visibility = View.INVISIBLE
            }
        })

    }
}
