package com.app.symetrycreed.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.symetrycreed.databinding.ActivityCenterBinding

class CenterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCenterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCenterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Solo visual; sin listeners por ahora.
    }
}
