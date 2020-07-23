package com.alim.writer

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.alim.writer.Database.ApplicationData
import com.alim.writer.UIHelper.ZoomageView
import com.bumptech.glide.Glide
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

class ImageActivity : AppCompatActivity() {

    val thread = Thread {
        try {
            val url = URL(intent.getStringExtra("IMAGE"));
            val conn = url.openConnection() as HttpURLConnection
            conn.readTimeout = 60000
            conn.connectTimeout = 65000
            conn.requestMethod = "GET"
            conn.doInput = true
            conn.connect()
            val bufferedInputStream = BufferedInputStream(conn.inputStream)
            val bmpImage = BitmapFactory.decodeStream(bufferedInputStream)
            runOnUiThread {
                findViewById<ZoomageView>(R.id.photo_view).setImageBitmap(bmpImage)
            }

        } catch(e: Exception) {
            Log.println(Log.ASSERT,"Ex", "$e")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (ApplicationData(this).theme)
            setTheme(R.style.AppThemeDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        //Thread(thread).start()

        Glide.with(this).load(intent.getStringExtra("IMAGE"))
            .into(findViewById(R.id.photo_view))
    }
}