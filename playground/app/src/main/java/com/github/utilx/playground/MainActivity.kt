package com.github.utilx.playground

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.github.utilx.AssetFiles
import com.github.utilx.AssetFilesKt

private const val TAG = "LOG"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TEST", getString(R.string.prefix_az_123_txt_1028221614))

        Log.d(TAG, "Java asset file smoketest 1 ${AssetFiles.CONSTPREFIXJAVA_REPLACEJAVA_123_TXT_2038126485}")
        Log.d(TAG, "Java asset file smoketest 2 ${AssetFiles.CONSTPREFIXJAVA_ROOT_JAVA_TXT_1351089311}")

        Log.d(TAG, "Kotlin asset file smoketest 1 ${AssetFilesKt.CONSTPREFIXKT_REPLACEKT_123_TXT_371122798}")
        Log.d(TAG, "Kotlin asset file smoketest 2 ${AssetFilesKt.CONSTPREFIXKT_ROOT_KT_TXT_203164056}")

        setContentView(R.layout.activity_main)
    }
}
