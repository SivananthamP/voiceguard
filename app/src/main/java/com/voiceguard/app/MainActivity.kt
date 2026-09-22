package com.voiceguard.app

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.nio.FloatBuffer
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max

class MainActivity : AppCompatActivity() {

    // =========================================================
    // UI
    // =========================================================

    private lateinit var statusText: TextView
    private lateinit var probabilityText: TextView
    private lateinit var windowText: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    // =========================================================
    // AUDIO
    // =========================================================

    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var isRecording = false

    private val sampleRate = 16000
    private val windowSamples = 48000

    // =========================================================
    // ONNX RUNTIME
    // =========================================================

    private lateinit var ortEnvironment: OrtEnvironment

    // Nullable so the application cannot crash if model loading fails
    private var ortSession: OrtSession? = null

    // =========================================================
    // ACTIVITY CREATED
    // =========================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        probabilityText = findViewById(R.id.probabilityText)
        windowText = findViewById(R.id.windowText)

        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)

        // Start disabled until ONNX model is successfully loaded
        startButton.isEnabled = false
        stopButton.isEnabled = false

        statusText.text = "LOADING MODEL"
        probabilityText.text = "Synthetic probability: --"
        windowText.text = "Loading AASIST model..."

        initializeModel()

        // =====================================================
        // START BUTTON
        // =====================================================

        startButton.setOnClickListener {

            if (!isModelReady()) {
                statusText.text = "MODEL ERROR"
                windowText.text = "ONNX model is not ready"
                return@setOnClickListener
            }

            if (checkMicrophonePermission()) {
                startAnalysis()
            } else {
                requestMicrophonePermission()
            }
        }

        // =====================================================
        // STOP BUTTON
        // =====================================================

        stopButton.setOnClickListener {
            stopAnalysis()
        }
    }

    // =========================================================
    // CHECK MODEL
    // =========================================================

    private fun isModelReady(): Boolean {
        return ortSession != null
    }

    // =========================================================
    // LOAD ONNX MODEL
    // =========================================================

    private fun initializeModel() {

        try {

            Log.d("VoiceGuard", "Starting ONNX initialization...")

            // -------------------------------------------------
            // Create ONNX Runtime environment
            // -------------------------------------------------

            ortEnvironment = OrtEnvironment.getEnvironment()

            Log.d(
                "VoiceGuard",
                "ONNX Runtime environment created"
            )

            // -------------------------------------------------
            // Load model from Android assets
            // -------------------------------------------------

            val modelBytes = assets
                .open("aasistmodel.onnx")
                .use { it.readBytes() }

            Log.d(
                "VoiceGuard",
                "ONNX model loaded from assets: ${modelBytes.size} bytes"
            )

            // -------------------------------------------------
            // Create session options
            // -------------------------------------------------

            val sessionOptions = OrtSession.SessionOptions()

            // -------------------------------------------------
            // Create ONNX session
            // -------------------------------------------------

            ortSession = ortEnvironment.createSession(
                modelBytes,
                sessionOptions
            )

            Log.d(
                "VoiceGuard",
                "ONNX SESSION CREATED SUCCESSFULLY"
            )

            // -------------------------------------------------
            // Model ready
            // -------------------------------------------------

            statusText.text = "MODEL READY"

            probabilityText.text =
                "Synthetic probability: --"

            windowText.text =
                "AASIST model loaded successfully"

            startButton.isEnabled = true

        } catch (e: Exception) {

            // -------------------------------------------------
            // Model loading failed
            // -------------------------------------------------

            Log.e(
                "VoiceGuard",
                "ONNX INITIALIZATION FAILED",
                e
            )

            // Make sure we don't retain a partially initialized session
            ortSession = null

            statusText.text = "MODEL ERROR"

            windowText.text =
                "ONNX error: ${e.message ?: "Unknown error"}"

            probabilityText.text =
                "Synthetic probability: --"

            startButton.isEnabled = false
        }
    }

    // =========================================================
    // MICROPHONE PERMISSION
    // =========================================================

    private fun checkMicrophonePermission(): Boolean {

        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestMicrophonePermission() {

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            100
        )
    }

    // =========================================================
    // START AUDIO ANALYSIS
    // =========================================================

    private fun startAnalysis() {

        if (isRecording) {
            return
        }

        // -----------------------------------------------------
        // Safety check
        // -----------------------------------------------------

        val session = ortSession

        if (session == null) {

            statusText.text = "MODEL ERROR"

            windowText.text =
                "ONNX session is not initialized"

            return
        }

        try {

            // -------------------------------------------------
            // Determine minimum microphone buffer
            // -------------------------------------------------

            val minBuffer = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            if (minBuffer <= 0) {

                statusText.text = "AUDIO ERROR"

                windowText.text =
                    "Microphone is not available"

                return
            }

            // -------------------------------------------------
            // Buffer size
            // -------------------------------------------------

            val bufferSize = max(
                minBuffer,
                windowSamples * 2
            )

            // -------------------------------------------------
            // Create AudioRecord
            // -------------------------------------------------

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            // -------------------------------------------------
            // Verify microphone initialization
            // -------------------------------------------------

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {

                statusText.text = "AUDIO ERROR"

                windowText.text =
                    "Could not initialize microphone"

                audioRecord?.release()
                audioRecord = null

                return
            }

            // -------------------------------------------------
            // Start recording
            // -------------------------------------------------

            audioRecord?.startRecording()

            isRecording = true

            startButton.isEnabled = false
            stopButton.isEnabled = true

            statusText.text = "LISTENING"

            probabilityText.text =
                "Synthetic probability: --"

            windowText.text =
                "Collecting 3-second audio..."

            // -------------------------------------------------
            // Background recording thread
            // -------------------------------------------------

            recordingThread = Thread {

                val audioBuffer =
                    ShortArray(windowSamples)

                while (isRecording) {

                    var totalRead = 0

                    // -----------------------------------------
                    // Collect exactly 3 seconds
                    // -----------------------------------------

                    while (
                        totalRead < windowSamples &&
                        isRecording
                    ) {

                        val read = audioRecord?.read(
                            audioBuffer,
                            totalRead,
                            windowSamples - totalRead
                        ) ?: 0

                        if (read > 0) {
                            totalRead += read
                        }
                    }

                    // -----------------------------------------
                    // Run inference
                    // -----------------------------------------

                    if (
                        totalRead == windowSamples &&
                        isRecording
                    ) {

                        val probability =
                            runInference(audioBuffer)

                        runOnUiThread {

                            if (isRecording) {

                                updateRisk(
                                    probability
                                )
                            }
                        }
                    }
                }
            }

            recordingThread?.start()

        } catch (e: Exception) {

            Log.e(
                "VoiceGuard",
                "AUDIO START FAILED",
                e
            )

            statusText.text = "AUDIO ERROR"

            windowText.text =
                e.message ?: "Could not start microphone"

            stopAnalysis()
        }
    }

    // =========================================================
    // ONNX INFERENCE
    // =========================================================

    private fun runInference(
        audio: ShortArray
    ): Float {

        var tensor: OnnxTensor? = null
        var result: OrtSession.Result? = null

        return try {

            // -------------------------------------------------
            // Check ONNX session
            // -------------------------------------------------

            val session = ortSession
                ?: throw IllegalStateException(
                    "ONNX session is not initialized"
                )

            // -------------------------------------------------
            // Convert PCM16 -> Float32
            // -------------------------------------------------

            val input = FloatArray(windowSamples)

            var maxValue = 0.0f

            for (i in audio.indices) {

                val value =
                    audio[i].toFloat() / 32768.0f

                input[i] = value

                maxValue = max(
                    maxValue,
                    abs(value)
                )
            }

            // -------------------------------------------------
            // Normalize
            // -------------------------------------------------

            if (maxValue > 0.0f) {

                for (i in input.indices) {

                    input[i] =
                        input[i] / maxValue
                }
            }

            // -------------------------------------------------
            // Model input shape
            //
            // [batch, channel, samples]
            // [1,     1,       48000]
            // -------------------------------------------------

            val shape = longArrayOf(
                1,
                1,
                windowSamples.toLong()
            )

            // -------------------------------------------------
            // Create ONNX tensor
            // -------------------------------------------------

            tensor = OnnxTensor.createTensor(
                ortEnvironment,
                FloatBuffer.wrap(input),
                shape
            )

            // -------------------------------------------------
            // Model input
            // -------------------------------------------------

            val inputs = mapOf(
                "audio" to tensor
            )

            // -------------------------------------------------
            // Run ONNX model
            // -------------------------------------------------

            result = session.run(inputs)

            // -------------------------------------------------
            // Read output
            //
            // Model output:
            // logit
            // -------------------------------------------------

            val outputValue = result[0].value

            val logit: Float = when (outputValue) {

                is FloatArray -> {

                    if (outputValue.isEmpty()) {
                        throw IllegalStateException(
                            "ONNX output FloatArray is empty"
                        )
                    }

                    outputValue[0]
                }

                is Array<*> -> {

                    if (outputValue.isEmpty()) {
                        throw IllegalStateException(
                            "ONNX output Array is empty"
                        )
                    }

                    val first = outputValue[0]

                    when (first) {

                        is FloatArray -> {

                            if (first.isEmpty()) {
                                throw IllegalStateException(
                                    "Nested ONNX output is empty"
                                )
                            }

                            first[0]
                        }

                        is Float -> {
                            first
                        }

                        else -> {

                            throw IllegalStateException(
                                "Unexpected nested ONNX output type: " +
                                        "${first?.javaClass}"
                            )
                        }
                    }
                }

                is Float -> {
                    outputValue
                }

                else -> {

                    throw IllegalStateException(
                        "Unexpected ONNX output type: " +
                                "${outputValue?.javaClass}"
                    )
                }
            }

            Log.d(
                "VoiceGuard",
                "AASIST logit = $logit"
            )

            // -------------------------------------------------
            // Logit -> probability
            // -------------------------------------------------

            sigmoid(logit)

        } catch (e: Exception) {

            Log.e(
                "VoiceGuard",
                "INFERENCE FAILED",
                e
            )

            runOnUiThread {

                windowText.text =
                    "Inference error: ${e.message ?: "Unknown error"}"
            }

            0.0f

        } finally {

            // -------------------------------------------------
            // Always release tensor/result
            // -------------------------------------------------

            try {
                tensor?.close()
            } catch (_: Exception) {
            }

            try {
                result?.close()
            } catch (_: Exception) {
            }
        }
    }

    // =========================================================
    // SIGMOID
    // =========================================================

    private fun sigmoid(x: Float): Float {

        return (
                1.0 /
                        (1.0 + exp(-x.toDouble()))
                ).toFloat()
    }

    // =========================================================
    // RISK CLASSIFICATION
    // =========================================================

    private fun updateRisk(
        probability: Float
    ) {

        val percentage =
            probability * 100.0f

        probabilityText.text =
            String.format(
                "Synthetic probability: %.2f%%",
                percentage
            )

        windowText.text =
            "3-second audio window analyzed"

        when {

            probability < 0.40f -> {

                statusText.text = "SAFE"
            }

            probability < 0.70f -> {

                statusText.text = "CAUTION"
            }

            else -> {

                statusText.text = "HIGH RISK"
            }
        }
    }

    // =========================================================
    // STOP ANALYSIS
    // =========================================================

    private fun stopAnalysis() {

        isRecording = false

        try {
            audioRecord?.stop()
        } catch (_: Exception) {
        }

        try {
            recordingThread?.join(500)
        } catch (_: Exception) {
        }

        audioRecord?.release()

        audioRecord = null
        recordingThread = null

        if (!isFinishing && !isDestroyed) {

            startButton.isEnabled =
                ortSession != null

            stopButton.isEnabled = false

            if (ortSession != null) {

                statusText.text = "READY"

                probabilityText.text =
                    "Synthetic probability: --"

                windowText.text =
                    "Analysis stopped"
            }
        }
    }

    // =========================================================
    // ACTIVITY DESTROYED
    // =========================================================

    override fun onDestroy() {

        isRecording = false

        try {
            audioRecord?.stop()
        } catch (_: Exception) {
        }

        try {
            audioRecord?.release()
        } catch (_: Exception) {
        }

        audioRecord = null

        try {
            recordingThread?.interrupt()
        } catch (_: Exception) {
        }

        recordingThread = null

        try {
            ortSession?.close()
        } catch (_: Exception) {
        }

        ortSession = null

        try {
            if (::ortEnvironment.isInitialized) {
                ortEnvironment.close()
            }
        } catch (_: Exception) {
        }

        super.onDestroy()
    }
}