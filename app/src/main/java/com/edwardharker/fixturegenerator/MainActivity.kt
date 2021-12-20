package com.edwardharker.fixturegenerator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.edwardharker.fixturegenerator.graphql.model.LaunchDetailsQuery

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        LaunchDetailsQuery.Mission
    }
}