package com.genterix.motm.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import com.genterix.motm.R
import java.util.*

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        sendToPrivacy()
        sendToHdnfc()

    }
    private fun sendToPrivacy() {
        val privacyText = findViewById<TextView>(R.id.privacy)
        privacyText.movementMethod = LinkMovementMethod.getInstance()
        privacyText.setLinkTextColor(Color.BLUE)
    }

    private fun sendToHdnfc() {
        val hdnfcText = findViewById<TextView>(R.id.hdnfc)
        hdnfcText.movementMethod = LinkMovementMethod.getInstance()
        hdnfcText.setLinkTextColor(Color.BLUE)
    }
}