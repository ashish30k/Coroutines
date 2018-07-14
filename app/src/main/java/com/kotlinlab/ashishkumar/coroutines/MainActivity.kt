package com.kotlinlab.ashishkumar.coroutines

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.timeunit.TimeUnit
import okhttp3.*
import java.io.IOException
import kotlin.coroutines.experimental.suspendCoroutine

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // 1. Displays the message after 5 seconds delay without blocking the Main thread.
        // Since we are passing UI CoroutineDispatcher so that  setTextAfterDelay() runs on Main thread
        /* fab.setOnClickListener {
             launch(UI) {
                 setTextAfterDelay(5, "Welcome to Coroutine World !!!")
             }
         }*/

        // 2.Make a n/w call in a separate thread and after result comes back from server update the UI
        // Note: Here caller is handling success or failure scenarios
        /*fab.setOnClickListener {
            launch(UI) {
                var data = ""
                try {
                    async(CommonPool) { data = downloadBlockingData() }.await()
                } catch (exception: IOException) {
                    data = "Error"
                }
                // after execution of downloadBlockingData() update UI
                message_textview.text = data
            }
        }*/

        // 3.Make a n/w call in a separate thread and after result comes back from server update the UI
        // Note: here we are wrapping response in callback
        fab.setOnClickListener {
            launch(UI) {
                val data = downloadAsyncData()
                message_textview.text = data
            }
        }

    }

    private suspend fun downloadAsyncData(): String {
        return suspendCoroutine { continuation ->
            val httpClient = OkHttpClient()
            val request = Request.Builder()
                    .url("https://jsonplaceholder.typicode.com/posts")
                    .build()
            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    continuation.resume(response.body()?.string() ?: "")
                }
            })
        }
    }

    private fun downloadBlockingData(): String {
        val httpClient = OkHttpClient()
        val request = Request.Builder()
                .url("https://jsonplaceholder.typicode.com/posts")
                .build()
        val response = httpClient.newCall(request).execute()
        return response.body()?.string() ?: ""

    }

    private suspend fun setTextAfterDelay(seconds: Long, message: String) {
        delay(seconds, TimeUnit.SECONDS)
        message_textview.text = message
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
