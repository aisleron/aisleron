package com.aisleron

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setContentView(R.layout.activity_about)
    }

    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }*/
}