package com.genterix.motm.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.genterix.motm.R
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import java.util.*


class DetailsActivity : AppCompatActivity() {

    private lateinit var item: LocationInfo
    private lateinit var contentTextView: TextView
    private lateinit var imageView: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        contentTextView = findViewById(R.id.contentTextView)
        imageView = findViewById(R.id.imageView)

        val json = intent.getStringExtra("ITEM")
        item = Gson().fromJson(json, LocationInfo::class.java)

        title = item.name
        contentTextView.text = item.content

//        contentTextView.movementMethod = ScrollingMovementMethod()

        Picasso.get().load(item.imageURL).into(imageView);
    }

    fun didClickNavigate(v: View?) {
        val uri: String = java.lang.String.format(Locale.ENGLISH, "geo:0,0?q=%s,%s(%s)", item.latlng?.latitude,
            item.latlng?.longitude, Uri.encode(item.name))
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
//        intent.setPackage("com.google.android.apps.maps")
        this.startActivity(intent)
    }
}
