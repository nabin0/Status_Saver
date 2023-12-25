package com.zoomcodez.statussaver_downloadvideo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.zoomcodez.statussaver_downloadvideo.databinding.ActivityFacebookBinding

class FacebookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFacebookBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFacebookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.blue)

        binding.backArrow.setOnClickListener {
            this.finish()
        }
    }
}