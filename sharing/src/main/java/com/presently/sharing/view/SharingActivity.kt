package com.presently.sharing.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.presently.sharing.R

class SharingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sharing)

        val content = intent.getStringExtra(EXTRA_SHARING_CONTENT)
        val dateString = intent.getStringExtra(EXTRA_SHARING_DATE)
        requireNotNull(content)
        requireNotNull(dateString)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, SharingFragment.newInstance(content, dateString))
                .commitNow()
        }
    }

    companion object {
        const val EXTRA_SHARING_CONTENT = "EXTRA_SHARING_CONTENT"
        const val EXTRA_SHARING_DATE = "EXTRA_SHARING_DATE"
    }
}