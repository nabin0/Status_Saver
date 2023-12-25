package com.zoomcodez.statussaver_downloadvideo.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import com.zoomcodez.statussaver_downloadvideo.PreferenceManager
import com.zoomcodez.statussaver_downloadvideo.Util
import com.zoomcodez.statussaver_downloadvideo.WhatsAppActivity
import com.zoomcodez.statussaver_downloadvideo.adapters.WhatsappAdapter
import com.zoomcodez.statussaver_downloadvideo.databinding.FragmentWhatsappImagesBinding
import com.zoomcodez.statussaver_downloadvideo.models.WhatsappStatusModel
import java.io.File
import java.util.Arrays

class WhatsappImagesFragment : Fragment() {

    private lateinit var binding: FragmentWhatsappImagesBinding
    private var list: MutableList<WhatsappStatusModel> = mutableListOf()
    private lateinit var whatsappAdapter: WhatsappAdapter

    lateinit var preferenceManager: PreferenceManager

    var uriTree: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentWhatsappImagesBinding.inflate(layoutInflater)
        context?.let {
            whatsappAdapter = WhatsappAdapter(it, activity as WhatsAppActivity)
        }
        preferenceManager = PreferenceManager(requireContext())


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (preferenceManager.getString(Util.URI_TREE) != "null") {
                uriTree = Uri.parse(preferenceManager.getString(Util.URI_TREE))
                initRecyclerView()
                getData()
            } else {
                // TODO: provide permission button
            }
        } else {
            initRecyclerView()
            list = mutableListOf()
            getData()
        }

        binding.refreshLayout.setOnRefreshListener {
            list = mutableListOf()
            getData()
            binding.refreshLayout.isRefreshing = false
        }

        return binding.root
    }

    private fun getData() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (preferenceManager.getString(Util.URI_TREE) != "null") {
                uriTree = Uri.parse(preferenceManager.getString(Util.URI_TREE))
                getDataAbove11()
            } else {
                getFolderPermission()
            }
        } else {
            getDataBelow11()
        }
    }

    private fun getDataAbove11() {
        if (uriTree != null) {
            context?.contentResolver?.takePersistableUriPermission(
                uriTree!!, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            val docFile =
                context?.applicationContext?.let { DocumentFile.fromTreeUri(it, uriTree!!) }

            val allFiles = docFile?.listFiles()

            if (allFiles != null) {
                for (file in allFiles) {
                    if (file.uri.toString().endsWith(".png") || file.uri.toString()
                            .endsWith(".jpg")
                    ) {
                        val model = WhatsappStatusModel(
                            "whats ${file.uri}",
                            "${Environment.getExternalStorageDirectory().absolutePath}/Android/media/com.whatsapp/WhatsApp/Media/.Statuses/${file.name}",
                            file.uri,
                            file.name.toString()
                        )

                        Log.d(
                            "TAG",
                            "getDataAbove11:  \n" + "uri: ${file.uri} \n " + "uritree: ${uriTree.toString()} \n" + "filename: ${file.name}\n" + "path ${Environment.getExternalStorageDirectory().absolutePath}/Android/com.whatsapp/media/WhatsApp/Media/.Statuses/${file.name}"
                        )

                        list.add(model)
                        if(list.size % 3 == 0){
                            list.add(model)
                        }
                        whatsappAdapter.submitList(list)
                    }
                }
            }
        }
    }


    private fun getFolderPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val storageManager =
                context?.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val intent = storageManager.primaryStorageVolume.createOpenDocumentTreeIntent()
            val targetDirectory = "Android%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses"
            var uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI") as Uri
            var scheme = uri.toString()
            scheme = scheme.replace("/root/", "/tree/")
            scheme += "%3A$targetDirectory"

            uri = Uri.parse(scheme)
            intent.putExtra("android.provider.extra.INITIAL_URI", uri)
            intent.putExtra("android.content.extra.SHOW_ADVANCED", true)

            startActivityForResult(intent, 1234)
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == AppCompatActivity.RESULT_OK) {
            val treeUri = data?.data
            uriTree = treeUri
            preferenceManager.putString(Util.URI_TREE, uriTree.toString())
            getData()
        }
    }

    private fun initRecyclerView() {
        binding.whatsappRecyclerView.apply {
            adapter = whatsappAdapter
        }
    }

    private fun getDataBelow11() {
        var model: WhatsappStatusModel
        val targetPath =
            "${Environment.getExternalStorageDirectory().absolutePath}/WhatsApp/Media/.Statuses"
        val targetDirectory = File(targetPath)

        val allFiles: Array<File>? = targetDirectory.listFiles()

        if (allFiles.isNullOrEmpty()) return

        Arrays.sort(allFiles, compareByDescending {
            it.lastModified()
        })

        for (file in allFiles) {
            if (Uri.fromFile(file).toString().endsWith(".png") || Uri.fromFile(file).toString()
                    .endsWith(".jpg")
            ) {
                model = WhatsappStatusModel(
                    "whats ${file.absolutePath} ", file.absolutePath, Uri.fromFile(file), file.name
                )
                list.add(model)
                if(list.size % 3 == 0){
                    list.add(model)
                }
                whatsappAdapter.submitList(list)
            }
        }
    }
}