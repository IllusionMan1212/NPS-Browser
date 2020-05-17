package com.illusionware.npsbrowser.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.illusionware.npsbrowser.AppData
import com.illusionware.npsbrowser.databinding.ActivityMainBinding
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private val tsvReader = csvReader {
        charset = "UTF-8"
        quoteChar = '\\'
        escapeChar = '\\'
        delimiter = '\t'
        skipEmptyLine = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.root)

        binding.tsvChooserButton.setOnClickListener {
            if (!isStoragePermissionGranted())
                return@setOnClickListener
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/tab-separated-values"
            }
            startActivityForResult(intent, 1)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val entries : List<Map<String, String?>> = tsvReader.readAllWithHeader(inputStream!!)
                val myIntent = Intent(this, AppsListActivity::class.java)
                var apps = ArrayList<AppData>(entries.size)

                entries.forEach { entry ->
                    if (entry["Name"].isNullOrEmpty()) {
                        return@forEach;
                    }
                    val id = entry["Title ID"]
                    val region = entry["Region"]
                    val name = entry["Name"]
                    val link = entry["PKG direct link"]
                    val license = entry["zRIF"]
                    val contentID = entry["Content ID"]
                    val lastDateTime = entry["Last Modification Date"]
                    val fileSize = entry["File Size"]?.toLongOrNull()
                    val sha256 = entry["SHA256"]
                    val minFW = entry["Required FW"]
                    var app = AppData(id, region, name, link ?: "MISSING", license ?: "MISSING",
                        contentID ?: "MISSING", lastDateTime ?: "MISSING",
                        fileSize ?: 0, sha256 ?: "MISSING", minFW ?: "MISSING")
                    apps.add(app)
                }
                apps.sortBy { it.Title }
                AppsListActivity.apps = apps;
                startActivity(myIntent)
            }
        }
    }

    private fun isStoragePermissionGranted(): Boolean {
        return  if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v("perms", "Permission is granted")
                true
            } else {
                Log.v("perms", "Permission is revoked")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
                false
            }
    }
}
