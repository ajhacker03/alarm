package com.example.alarm

import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView

class MainActivity : AppCompatActivity(), SensorEventListener {

    private var mCurrentDisplayRotation = -1
    var liveSensorValues = FloatArray(10000)
    lateinit var normalizedValues: FloatArray
    private var calibrationHandler: Handler? = null
    private val calibrationIntervalMillis = 2 * 60 * 1000.toLong()

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var textView: TextView
    private var isVibrating: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calibrationHandler = Handler()
        textView = findViewById(R.id.textView)
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm)
    }


    override fun onSensorChanged(sensorEvent: SensorEvent) {
        val rotation = windowManager.defaultDisplay.rotation
        val REVERSE_LANDSCAPE = 3

        if (mCurrentDisplayRotation !== rotation) {
            if (rotation == REVERSE_LANDSCAPE && isVibrating) {
                mediaPlayer.start()
                mediaPlayer.isLooping = true
                textView.text = "Reversed"
                Log.d("Orientation", "REVERSE")
            } else {
                mediaPlayer.release()
                textView.text = "normal"
                isVibrating = false
            }
            mCurrentDisplayRotation = rotation
        }

        liveSensorValues = sensorEvent.values.clone()
        if (!::normalizedValues.isInitialized) {
            setCurrentValuesAsCalibrated()
            return
        } else if (normalizedValues!!.size != 3) {
            return
        }

        val xDiff = Math.abs(liveSensorValues[0] - normalizedValues!![0])
        val yDiff = Math.abs(liveSensorValues[1] - normalizedValues!![1])
        val zDiff = Math.abs(liveSensorValues[2] - normalizedValues!![2])
        val changeThreshold = 0.7f
        Log.d("MOTION (diff)", "Sensors changed")
        if (xDiff >= changeThreshold || yDiff >= changeThreshold || zDiff >= changeThreshold) {
            window!!.statusBarColor = Color.BLUE
            Log.d("MOTION (diff)", "X: $xDiff; Y: $yDiff; Z: $zDiff;")
            isVibrating = true
        } else {
            window!!.statusBarColor = Color.BLACK
            isVibrating = false
        }
    }

    private fun setCurrentValuesAsCalibrated() {
        normalizedValues = FloatArray(1000)
        normalizedValues = liveSensorValues.clone()
        Log.d("Vibration", "Normalized ${normalizedValues.size}")
        calibrationHandler!!.postDelayed({ setCurrentValuesAsCalibrated() }, calibrationIntervalMillis)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

}