package com.example.wearos_project

import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.widget.Button
import android.widget.TextView
import java.util.*

class MainActivity : WearableActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mainButton = findViewById<Button>(R.id.mainButton)
        val buttonOutput = findViewById<TextView>(R.id.buttonOutput)

        mainButton.setOnClickListener {
            val rand = Random().nextInt(10)
            buttonOutput.text = rand.toString()
        }

    }
}
