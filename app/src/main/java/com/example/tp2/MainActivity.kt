package com.example.tp2

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
//import androidx.appcompat.app.AppCompatActivity
//import androidx.compose.ui.graphics.Color

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.sqrt

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorList: List<Sensor>
    private lateinit var recyclerView: RecyclerView
    private lateinit var sensorAdapter: SensorAdapter
    private lateinit var statusTextView: TextView
    private lateinit var directionTextView: TextView
    private var accelerometer: Sensor? = null

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
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val totalAcceleration = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()

            changeBackgroundColor(totalAcceleration)
            detectDirection(x, y)
        }
    }

    private fun changeBackgroundColor(acceleration: Float) {
        // Your existing color change code
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

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used, but required by the SensorEventListener interface
    }
}
