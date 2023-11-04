package com.example.alarmstopper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val stopButton = findViewById<Button>(R.id.stopAlarmButton)
        stopButton.setOnClickListener {
            Toast.makeText(
                this@MainActivity,
                "stopAlarmButton",
                Toast.LENGTH_SHORT
            ).show()
        }

        val addDeviceButton = findViewById<Button>(R.id.addDivceButton)
        addDeviceButton.setOnClickListener {
            val deviceListActivity = Intent(this, DeviceListActivity::class.java)
            startActivity(deviceListActivity)
        }
    }
}