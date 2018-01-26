package tiwence.fr.easymovietest

import android.Manifest
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.media.MediaRecorder
import android.content.pm.PackageManager
import android.hardware.Camera
import android.view.*
import android.view.SurfaceHolder
import android.media.CamcorderProfile
import android.os.CountDownTimer
import android.support.v4.app.ActivityCompat
import android.view.SurfaceView
import tiwence.fr.easymovietest.util.StorageUtils
import kotlinx.android.synthetic.main.activity_media_recorder.*

class RecordingActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private var surfaceHolder: SurfaceHolder? = null
    private var surfaceView: SurfaceView? = null
    private var mRecorder: MediaRecorder? = null

    private var mCamera: Camera? = null
    var cameraConfigured: Boolean = false

    /**
     * Countdown used to stop video recording at 5.6 sec
     */
    var timer:CountDownTimer = object : CountDownTimer(5600, 100) {

        override fun onTick(millisUntilFinished: Long) { }

        override fun onFinish() {
            stopRecording()
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_recorder)

        supportActionBar?.hide()

        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        decorView.systemUiVisibility = uiOptions

        toggleRecordingButton.setOnCheckedChangeListener( { buttonView, isChecked ->
            if (isChecked) {
                startRecording()
                toggleRecordingButton.visibility = View.GONE
            }
        })

        //We need to ask permission for the Android Camera
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO),0)
        }

        mCamera = Camera.open()
        //Used to display the camera preview
        surfaceView = findViewById(R.id.surfaceCamera)
        surfaceHolder = surfaceView?.holder
        surfaceHolder?.addCallback(this)
        surfaceHolder?.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    protected fun startRecording() {
        mRecorder = MediaRecorder()  // Works well
        mCamera?.unlock()

        mRecorder?.setCamera(mCamera)

        mRecorder?.setMaxDuration(5600)
        mRecorder?.setPreviewDisplay(surfaceHolder?.surface)
        mRecorder?.setVideoSource(MediaRecorder.VideoSource.CAMERA)
        mRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)

        mRecorder?.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH))
        mRecorder?.setPreviewDisplay(surfaceHolder?.surface)
        mRecorder?.setOutputFile(StorageUtils.getInstance(this).getVideoDirectoryStorageAbsolutePath() + "/recorded.mp4")

        mRecorder?.prepare()
        mRecorder?.start()
        timer.start()
    }

    protected fun stopRecording() {
        releaseMediaRecorder()
        releaseCamera()

        finish()
    }

    private fun releaseMediaRecorder() {
        if (mRecorder != null) {
            mRecorder?.reset()
            mRecorder?.release()
            mRecorder = null
            mCamera?.lock()
        }
    }

    private fun releaseCamera() {
        if (mCamera != null) {
            mCamera?.stopPreview()
            mCamera?.release()        // release the camera for other applications
            mCamera = null
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int,
                                height: Int) {
        initPreview(width, height);
        startPreview();
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        releaseMediaRecorder()
        releaseCamera()
    }

    private fun initPreview(width: Int, height: Int) {
        if (mCamera != null && surfaceHolder?.getSurface() != null) {
            try {
                mCamera?.setPreviewDisplay(surfaceHolder)
            } catch (t: Throwable) {
            }

            if (!cameraConfigured) {
                val parameters = mCamera?.getParameters()
                val size = getBestPreviewSize(width, height, parameters!!)
                if (size != null) {
                    parameters.setPreviewSize(size.width, size.height)
                    mCamera?.setParameters(parameters)
                    cameraConfigured = true
                }
            }
        }
    }

    private fun startPreview() {
        if (cameraConfigured && mCamera != null) {
            mCamera?.startPreview()
        }
    }

    private fun getBestPreviewSize(width: Int, height: Int, parameters: Camera.Parameters): Camera.Size? {
        var result: Camera.Size? = null

        for (size in parameters.supportedPreviewSizes) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size
                } else {
                    val resultArea = result.width * result.height
                    val newArea = size.width * size.height

                    if (newArea > resultArea) {
                        result = size
                    }
                }
            }
        }
        return result
    }
}