package com.zoomcodez.statussaver_downloadvideo

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.zoomcodez.statussaver_downloadvideo.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(this)
        MobileAds.initialize(this) {}

        val adRequest = AdRequest.Builder().build()
        binding.bannerAdView.loadAd(adRequest)

        binding.bannerAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                binding.adContainer.visibility = View.VISIBLE
            }
        }

        setClickListeners()
        checkPermissions()

    }

    fun showAlertDialog() {
        val alert: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        val mView: View = layoutInflater.inflate(R.layout.about_app_dialog, null)
        val btnOk = mView.findViewById<View>(R.id.buttonOk) as Button
        alert.setView(mView)
        val alertDialog: AlertDialog = alert.create()
        alertDialog.setCanceledOnTouchOutside(false)
        btnOk.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }


    fun checkPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Dexter.withContext(this)
                .withPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        checkPermissions()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest, token: PermissionToken
                    ) {

                    }
                }).check()
        }

//        Dexter.withContext(this)
//            .withPermissions(
//                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                android.Manifest.permission.READ_MEDIA_IMAGES,
//                android.Manifest.permission.READ_MEDIA_VIDEO,
//            ).withListener(object : MultiplePermissionsListener{
//                override fun onPermissionsChecked(report: MultiplePermissionsReport){
//                    checkPermissions()
//                }
//                override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken){ /* ... */}
//            }).check()


    }

    private fun setClickListeners() {
        binding.facebookContainer.setOnClickListener {
//            val intent = Intent(this@MainActivity, FacebookActivity::class.java)
//            startActivity(intent)

            Toast.makeText(this, "COMING SOON!!!", Toast.LENGTH_SHORT).show()
        }

        binding.aboutUsContainer.setOnClickListener {
            showAlertDialog()
        }


        binding.whatsappContainer.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                if (preferenceManager.getString(Util.URI_TREE) != "null") {
                    val intent = Intent(this@MainActivity, WhatsAppActivity::class.java)
                    startActivity(intent)
                } else {
                    val alert: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
                    val mView: View = layoutInflater.inflate(R.layout.permission_hint_dialog, null)
                    val btnOk = mView.findViewById<View>(R.id.buttonPermissionOk) as Button
                    alert.setView(mView)
                    val alertDialog: AlertDialog = alert.create()
                    btnOk.setOnClickListener {
                        val intent = Intent(this@MainActivity, WhatsAppActivity::class.java)
                        startActivity(intent)
                        alertDialog.dismiss()
                    }
                    alertDialog.show()
                }

            } else {
                val intent = Intent(this@MainActivity, WhatsAppActivity::class.java)
                startActivity(intent)
            }

        }

        binding.shareAppContainer.setOnClickListener {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(
                Intent.EXTRA_TEXT,
                "Hey! Guess what? I found an amazing app called Status Saver that lets you download WhatsApp statuses easily. No more missing out on those funny videos or special moments. Check it out here : https://play.google.com/store/apps/details?id=$packageName"
            )
            sendIntent.type = "text/plain"
            startActivity(sendIntent)
        }


        binding.rateAppContainer.setOnClickListener {
            val uri: Uri = Uri.parse("market://details?id=$packageName")
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            goToMarket.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )
            try {
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
                    )
                )
            }
        }


        binding.moreAppsContainer.setOnClickListener {
            val uri: Uri = Uri.parse("https://play.google.com/store/apps/developer?id=Zoomcodez")
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            goToMarket.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )
            try {
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW, uri
                    )
                )
            }
        }

    }

}