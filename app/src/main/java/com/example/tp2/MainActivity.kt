package com.example.tp2

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity


import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorList: List<Sensor>
    private lateinit var recyclerView: RecyclerView
    private lateinit var sensorAdapter: SensorAdapter
    private lateinit var statusTextView: TextView
    private lateinit var directionTextView: TextView
    private var accelerometer: Sensor? = null
    private var isFlashlightOn: Boolean = false
    private lateinit var proximityImageView: ImageView
    private var proximitySensor: Sensor? = null

    private val functionalities = mapOf(
        "Accelerometer Feature" to Sensor.TYPE_ACCELEROMETER,
        "Gyroscope Feature" to Sensor.TYPE_GYROSCOPE,
        "Proximity Feature" to Sensor.TYPE_PROXIMITY
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        sensorAdapter = SensorAdapter(sensorList)
        recyclerView.adapter = sensorAdapter

        statusTextView = findViewById(R.id.statusTextView)
        directionTextView = findViewById(R.id.directionTextView)

        checkSensorAvailability()

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Initialize the proximityImageView
        proximityImageView = findViewById(R.id.proximityImageView)

        // Get the proximity sensor
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    }

    private fun checkSensorAvailability() {
        val unavailableSensors = mutableListOf<String>()

        for ((feature, sensorType) in functionalities) {
            val sensor: Sensor? = sensorManager.getDefaultSensor(sensorType)
            if (sensor == null) {
                unavailableSensors.add(feature)
            }
        }

        if (unavailableSensors.isNotEmpty()) {
            statusTextView.text = "Unavailable Features:\n${unavailableSensors.joinToString("\n")}"
        } else {
            statusTextView.text = "All Features are Available!"
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        proximitySensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun updateProximityStatus(distance: Float) {
        if (distance < proximitySensor!!.maximumRange) {
            proximityImageView.setImageResource(R.drawable.lamp_close)
            proximityImageView.visibility = View.VISIBLE
        } else {
            proximityImageView.setImageResource(R.drawable.lamp_far)
            proximityImageView.visibility = View.VISIBLE
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val totalAcceleration = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()

            changeBackgroundColor(totalAcceleration)
            detectDirection(x, y)
        } else if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
            updateProximityStatus(event.values[0])
        }
    }

    private fun changeBackgroundColor(acceleration: Float) {
        when {
            acceleration < 5 -> {
                window.decorView.setBackgroundColor(android.graphics.Color.GREEN)
            }
            acceleration in 5f..10f -> {
                window.decorView.setBackgroundColor(android.graphics.Color.BLUE)
            }
            else -> {
                window.decorView.setBackgroundColor(android.graphics.Color.RED)
            }
        }
    }

    private fun detectDirection(x: Float, y: Float) {
        when {
            x < -5 -> {
                directionTextView.text = "Direction: Left"
            }
            x > 5 -> {
                directionTextView.text = "Direction: Right"
            }
            y < -5 -> {
                directionTextView.text = "Direction: Up"
            }
            y > 5 -> {
                directionTextView.text = "Direction: Down"
            }
            else -> {
                directionTextView.text = "Direction: Stationary"
            }
        }
    }

    private fun detectShake(x: Float, y: Float, z: Float) {
        val shakeThreshold = 12.0f // Adjust this value as needed

        // Check if the device is shaken
        if (Math.abs(x) > shakeThreshold || Math.abs(y) > shakeThreshold || Math.abs(z) > shakeThreshold) {
            toggleFlashlight()
        }
    }

    private fun toggleFlashlight() {
        if (isFlashlightOn) {
            turnFlashlightOff()
        } else {
            turnFlashlightOn()
        }
    }

    private fun turnFlashlightOn() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraManager.setTorchMode(cameraManager.cameraIdList[0], true)
        isFlashlightOn = true
        Toast.makeText(this, "Flashlight ON", Toast.LENGTH_SHORT).show()
    }

    private fun turnFlashlightOff() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraManager.setTorchMode(cameraManager.cameraIdList[0], false)
        isFlashlightOn = false
        Toast.makeText(this, "Flashlight OFF", Toast.LENGTH_SHORT).show()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used, but required by the SensorEventListener interface
    }
}
