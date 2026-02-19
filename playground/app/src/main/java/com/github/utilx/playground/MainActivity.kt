package com.github.utilx.playground

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.github.utilx.AssetFilesKotlin

private const val TAG = "LOG"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TEST", getString(R.string.prefix_az_123_txt_1028221614))

        Log.d(TAG, "Kotlin asset file smoketest 1 ${AssetFilesKotlin.CONSTPREFIXKT_REPLACEKT_123_TXT_371122798}")
        Log.d(TAG, "Kotlin asset file smoketest 2 ${AssetFilesKotlin.CONSTPREFIXKT_ROOT_KT_TXT_203164056}")

        setContentView(R.layout.activity_main)
    }
}
