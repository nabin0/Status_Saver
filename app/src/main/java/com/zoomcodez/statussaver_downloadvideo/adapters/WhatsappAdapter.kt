package com.zoomcodez.statussaver_downloadvideo.adapters

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.snackbar.Snackbar
import com.zoomcodez.statussaver_downloadvideo.R
import com.zoomcodez.statussaver_downloadvideo.Util
import com.zoomcodez.statussaver_downloadvideo.databinding.LayoutAdBinding
import com.zoomcodez.statussaver_downloadvideo.databinding.LayoutWhatsappItemBinding
import com.zoomcodez.statussaver_downloadvideo.models.WhatsappStatusModel
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.OutputStream


class WhatsappAdapter(val context: Context, val activity: Activity) :
    ListAdapter<WhatsappStatusModel, RecyclerView.ViewHolder>(TaskDiffUtil()) {
    private var mInterstitialAd: InterstitialAd? = null
    private val saveFilePath = "${Util.RootDirectoryWhatsapp}/"
    var adRequest: AdRequest = AdRequest.Builder().build()

    init {

        InterstitialAd.load(context,
            "ca-app-pub-3940256099942544/1033173712",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
            })
    }

    class TaskDiffUtil : DiffUtil.ItemCallback<WhatsappStatusModel>() {
        override fun areItemsTheSame(
            oldItem: WhatsappStatusModel, newItem: WhatsappStatusModel
        ): Boolean {
            return oldItem.uri == newItem.uri
        }

        override fun areContentsTheSame(
            oldItem: WhatsappStatusModel, newItem: WhatsappStatusModel
        ): Boolean {
            return when {
                (oldItem.uri != newItem.uri) -> false
                (oldItem.path != newItem.path) -> false
                (oldItem.name != newItem.name) -> false
                (oldItem.fileName != newItem.fileName) -> false

                else -> true
            }
        }
    }

    inner class WhatsappViewHolder(private val itemBinding: LayoutWhatsappItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(item: WhatsappStatusModel, context: Context) {
            if (item.uri.toString().endsWith(".mp4") || item.uri.toString()
                    .endsWith(".3gp") || item.uri.toString().endsWith(".mpeg4")
            ) {
                itemBinding.imagePlayIcon.visibility = View.VISIBLE
            } else {
                itemBinding.imagePlayIcon.visibility = View.GONE
            }

            Glide.with(itemBinding.root.context).load(item.uri).into(itemBinding.itemImageView)

            itemBinding.imageDownloadIcon.setOnClickListener {

                Util.DOWNLOAD_COUNT++

                if (Util.DOWNLOAD_COUNT % 2 == 0) {
                    adRequest = AdRequest.Builder().build()
                    InterstitialAd.load(context,
                        context.getString(R.string.interstitialAd),
                        adRequest,
                        object : InterstitialAdLoadCallback() {
                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                mInterstitialAd = null
                            }

                            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                                mInterstitialAd = interstitialAd
                            }
                        })
                    if (mInterstitialAd != null) {
                        mInterstitialAd?.show(activity)
                    } else {
                        Log.d("TAG", "The interstitial ad wasn't ready yet.")
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (item.uri.toString().endsWith(".mp4")) {
                        val inputStream =
                            context.contentResolver.openInputStream(Uri.parse(item.uri.toString()))
                        val fileName = "${System.currentTimeMillis()}.mp4"
                        try {
                            val values = ContentValues()
                            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                            values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                            values.put(
                                MediaStore.MediaColumns.RELATIVE_PATH,
                                Environment.DIRECTORY_DOCUMENTS + "/StatusSaver/Videos/"
                            )

                            val uri = context.contentResolver.insert(
                                MediaStore.Files.getContentUri("external"), values
                            )
                            val outputStream: OutputStream = uri?.let {
                                context.contentResolver.openOutputStream(it)
                            }!!
                            if (inputStream != null) {
                                outputStream.write(inputStream.readBytes())
                            }
                            inputStream?.close()
                            outputStream.close()
                            showSnackbar(
                                itemBinding.itemImageView,
                                "Video Saved to : Documents/StatusSaver/Videos"
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    } else {
                        val fileName = "${System.currentTimeMillis()}.jpg"

                        val source: ImageDecoder.Source =
                            ImageDecoder.createSource(context.contentResolver, item.uri)
                        val bitmap: Bitmap = ImageDecoder.decodeBitmap(source)
                        context.contentResolver.also { resolver ->
                            var outputStream: OutputStream?
                            val contentValues = ContentValues().apply {
                                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                                put(
                                    MediaStore.MediaColumns.RELATIVE_PATH,
                                    Environment.DIRECTORY_PICTURES + "/StatusSaver"
                                )
                            }
                            val imageUri: Uri? = resolver.insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
                            )
                            outputStream = imageUri?.let { resolver.openOutputStream(it) }

                            outputStream.use {
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                                showSnackbar(
                                    itemBinding.itemImageView,
                                    "Image Saved to : Pictures/StatusSaver"
                                )
                            }
                        }
                    }
                } else {
                    Util.createFileFolder()
                    val file = File(item.path)
                    val destFile = File(saveFilePath)

                    try {
                        FileUtils.copyFileToDirectory(file, destFile)
                        showSnackbar(
                            itemBinding.itemImageView,
                            "Image Saved to : Documents/StatusSaver/Whatsapp"
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun showSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
    }

    inner class AdViewHolder(private val binding: LayoutAdBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            val builder = AdLoader.Builder(context, context.getString(R.string.nativeAd))
                .forNativeAd { nativeAd ->
                    val nativeAdView = LayoutInflater.from(context).inflate(
                        R.layout.layout_native_ad, null
                    ) as NativeAdView

                    populateNativeAdView(nativeAd, nativeAdView)
                    binding.adLayout.removeAllViews()
                    binding.adLayout.addView(nativeAdView)
                }

            val adLoader = builder.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    Log.d("TAG", "onAdFailedToLoad: ${p0.message}")
                }
            }).build()

            adLoader.loadAd(AdRequest.Builder().build())
        }

        private fun populateNativeAdView(
            nativeAd: NativeAd, adView: NativeAdView
        ) {
            // Set the media view.
            adView.mediaView = adView.findViewById(R.id.ad_media)

            // Set other ad assets.
            adView.headlineView = adView.findViewById(R.id.ad_headline)
            adView.bodyView = adView.findViewById(R.id.ad_body)
            adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
            adView.iconView = adView.findViewById(R.id.ad_app_icon)
            adView.priceView = adView.findViewById(R.id.ad_price)
            adView.starRatingView = adView.findViewById(R.id.ad_stars)
            adView.storeView = adView.findViewById(R.id.ad_store)
            adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

            // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
            (adView.headlineView as TextView?)!!.text = nativeAd.headline
            adView.mediaView!!.mediaContent = nativeAd.mediaContent

            // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
            // check before trying to display them.

            if (nativeAd.body == null) {
                adView.bodyView!!.visibility = View.INVISIBLE
            } else {
                adView.bodyView!!.visibility = View.VISIBLE
                (adView.bodyView as TextView?)!!.text = nativeAd.body
            }

            if (nativeAd.callToAction == null) {
                adView.callToActionView!!.visibility = View.INVISIBLE
            } else {
                adView.callToActionView!!.visibility = View.VISIBLE
                (adView.callToActionView as Button?)!!.text = nativeAd.callToAction
            }

            if (nativeAd.icon == null) {
                adView.iconView!!.visibility = View.GONE
            } else {
                (adView.iconView as ImageView?)!!.setImageDrawable(
                    nativeAd.icon!!.drawable
                )
                adView.iconView!!.visibility = View.VISIBLE
            }

            if (nativeAd.price == null) {
                adView.priceView!!.visibility = View.INVISIBLE
            } else {
                adView.priceView!!.visibility = View.VISIBLE
                (adView.priceView as TextView?)!!.text = nativeAd.price
            }

            if (nativeAd.store == null) {
                adView.storeView!!.visibility = View.INVISIBLE
            } else {
                adView.storeView!!.visibility = View.VISIBLE
                (adView.storeView as TextView?)!!.text = nativeAd.store
            }

            if (nativeAd.starRating == null) {
                adView.starRatingView!!.visibility = View.INVISIBLE
            } else {
                (adView.starRatingView as RatingBar?)!!.rating = nativeAd.starRating!!.toFloat()
                adView.starRatingView!!.visibility = View.VISIBLE
            }

            if (nativeAd.advertiser == null) {
                adView.advertiserView!!.visibility = View.INVISIBLE
            } else {
                (adView.advertiserView as TextView?)!!.text = nativeAd.advertiser
                adView.advertiserView!!.visibility = View.VISIBLE
            }

            // This method tells the Google Mobile Ads SDK that you have finished populating your
            // native ad view with this native ad.

            adView.setNativeAd(nativeAd)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = LayoutWhatsappItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        if (viewType == 0) {
            return WhatsappViewHolder(itemBinding)
        } else if (viewType == 1) {
            val adItemBinding = LayoutAdBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return AdViewHolder(adItemBinding)
        }
        return WhatsappViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == 0) {
            (holder as WhatsappViewHolder).bind(getItem(position), context)
        } else if (holder.itemViewType == 1) {
            (holder as AdViewHolder).bind()
        }

    }


    override fun getItemId(position: Int): Long {
        return getItem(position).hashCode().toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return if ((position + 1) % 3 == 0) {
            1
        } else {
            0
        }
    }

}