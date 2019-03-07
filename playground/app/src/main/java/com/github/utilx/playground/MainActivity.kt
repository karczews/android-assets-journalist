package com.github.utilx.playground

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TEST", getString(R.string.prefix_testFolder_1_testFolder_1_testFile_txt))
        setContentView(R.layout.activity_main)
    }
}
