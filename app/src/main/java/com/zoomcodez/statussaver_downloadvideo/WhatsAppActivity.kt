package com.zoomcodez.statussaver_downloadvideo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayoutMediator
import com.zoomcodez.statussaver_downloadvideo.adapters.ViewPagerAdapter
import com.zoomcodez.statussaver_downloadvideo.databinding.ActivityWhatsAppBinding
import com.zoomcodez.statussaver_downloadvideo.fragments.WhatsappImagesFragment
import com.zoomcodez.statussaver_downloadvideo.fragments.WhatsappVideosFragment


class WhatsAppActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWhatsAppBinding
    private lateinit var whatsAppActivity: WhatsAppActivity
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWhatsAppBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
        preferenceManager = PreferenceManager(this)
        whatsAppActivity = this

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (preferenceManager.getString(Util.URI_TREE) != "null") {
                initRecyclerView()
            } else {
                getFolderPermission()
            }
        } else {
            initRecyclerView()
        }

        binding.backArrow.setOnClickListener {
            this.finish()
        }


    }

    private fun getFolderPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val intent =
                (getSystemService(STORAGE_SERVICE) as StorageManager).primaryStorageVolume.createOpenDocumentTreeIntent()
            val startDir = "Android%2Fmedia%2Fcom.whatsapp%2FWhatsapp%2FMedia%2F.Statuses"
            var uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI")
            var scheme = uri.toString()
            Log.d("TAG", "INITIAL_URI scheme: $scheme")
            scheme = scheme.replace("/root/", "/document/")
            scheme += "%3A$startDir"
            uri = Uri.parse(scheme)
            intent.putExtra("android.provider.extra.INITIAL_URI", uri)
            Log.d("TAG", "uri: $uri")
            startActivityForResult(intent, 12)
            return
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (data != null) {
                val uri = data.data;
                if (uri?.path?.endsWith(".Statuses") == true) {
                    Log.d("TAG", "onActivityResult: " + uri.path);
                    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        contentResolver.takePersistableUriPermission(uri, takeFlags);
                    }

                    preferenceManager.putString(Util.URI_TREE, uri.toString())
                    initRecyclerView()
                } else {
                    // dialog when user gave wrong path
                    Toast.makeText(this, "wrong path", Toast.LENGTH_SHORT).show()
                }

            }


//        if (resultCode == AppCompatActivity.RESULT_OK) {
//            val treeUri = data?.data
//            uriTree = treeUri
//            preferenceManager.putString(Util.URI_TREE, treeUri.toString())
//            initRecyclerView()
//        }

        }
    }

    @SuppressLint("InflateParams")
    private fun initRecyclerView() {
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, lifecycle)

        viewPagerAdapter.addFragment(WhatsappImagesFragment(), "Images")
        viewPagerAdapter.addFragment(WhatsappVideosFragment(), "Videos")

        binding.viewPager.apply {
            adapter = viewPagerAdapter
            offscreenPageLimit = 1
        }

        // Connect ViewPagerAdapter with TabLayout (Enables swipe to tab change effect)
        TabLayoutMediator(
            binding.tabLayout, binding.viewPager
        ) { tab, position ->
            tab.text = viewPagerAdapter.fragmentTitleList[position]
        }.attach()


        // Set custom view for tab in TabLayout
        for (i in 0 until binding.tabLayout.tabCount) {
            val textView = LayoutInflater.from(whatsAppActivity).inflate(R.layout.custom_tab, null)
            binding.tabLayout.getTabAt(i)?.customView = textView
        }
    }


}