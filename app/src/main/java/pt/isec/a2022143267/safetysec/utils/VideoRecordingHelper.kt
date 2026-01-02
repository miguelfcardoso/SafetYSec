package pt.isec.a2022143267.safetysec.utils

import android.content.Context
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Helper class for video recording using CameraX
 */
class VideoRecordingHelper(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var recordedVideoFile: File? = null

    suspend fun startRecording(
        previewView: PreviewView,
        durationSeconds: Int = 30
    ): File? = suspendCoroutine { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                // Setup camera preview
                val preview = androidx.camera.core.Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                // Setup video capture
                val recorder = Recorder.Builder()
                    .setQualitySelector(QualitySelector.from(Quality.HD))
                    .build()

                videoCapture = VideoCapture.withOutput(recorder)

                // Select back camera
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    // Unbind all use cases before rebinding
                    cameraProvider.unbindAll()

                    // Bind use cases to camera
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        videoCapture
                    )

                    // Start recording
                    val videoFile = createVideoFile()
                    recordedVideoFile = videoFile

                    val outputOptions = FileOutputOptions.Builder(videoFile).build()

                    recording = videoCapture?.output
                        ?.prepareRecording(context, outputOptions)
                        ?.withAudioEnabled()
                        ?.start(ContextCompat.getMainExecutor(context)) { event ->
                            when (event) {
                                is VideoRecordEvent.Finalize -> {
                                    if (event.hasError()) {
                                        continuation.resumeWithException(
                                            Exception("Video recording error: ${event.error}")
                                        )
                                    } else {
                                        continuation.resume(videoFile)
                                    }
                                }
                            }
                        }

                    // Stop recording after specified duration
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        stopRecording()
                    }, durationSeconds * 1000L)

                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }

            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun stopRecording() {
        recording?.stop()
        recording = null
    }

    private fun createVideoFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = context.getExternalFilesDir(null)
        return File(storageDir, "ALERT_${timestamp}.mp4")
    }

    suspend fun uploadVideoToFirebase(
        videoFile: File,
        userId: String,
        alertId: String
    ): String {
        // Firebase Storage is not available in free plan
        // Alternative solution: Keep video locally and return local path

        // Move video to a permanent location with proper naming
        val permanentDir = File(context.getExternalFilesDir(null), "alert_videos")
        if (!permanentDir.exists()) {
            permanentDir.mkdirs()
        }

        val permanentFile = File(permanentDir, "${alertId}_video.mp4")

        try {
            videoFile.copyTo(permanentFile, overwrite = true)
            videoFile.delete() // Remove temporary file

            // Return local file path as "video URL"
            // This will be stored in Firestore and can be accessed later
            return "local://${permanentFile.absolutePath}"
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }

        /* Original Firebase Storage code (requires paid plan):
        val storage = FirebaseStorage.getInstance()
        val videoRef = storage.reference
            .child("videos")
            .child(userId)
            .child("${alertId}_video.mp4")

        val uploadTask = videoRef.putFile(Uri.fromFile(videoFile))
        uploadTask.await()

        val downloadUrl = videoRef.downloadUrl.await()
        videoFile.delete()
        return downloadUrl.toString()
        */
    }

    /**
     * Get local video file from a local:// URL
     */
    fun getLocalVideoFile(localUrl: String): File? {
        if (!localUrl.startsWith("local://")) return null
        val path = localUrl.removePrefix("local://")
        val file = File(path)
        return if (file.exists()) file else null
    }

    /**
     * Delete local video file
     */
    fun deleteLocalVideo(localUrl: String): Boolean {
        val file = getLocalVideoFile(localUrl) ?: return false
        return try {
            file.delete()
        } catch (e: Exception) {
            false
        }
    }
}

