package com.dora.web

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.addCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.dora.web.databinding.FragmentWebViewBinding
import com.dora.web.utils.Constants
import com.dora.web.utils.changeVisibility
import com.dora.web.utils.invisible
import com.dora.web.utils.showExitDialog
import com.dora.web.utils.visible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class WebViewFragment : Fragment() {
    private lateinit var binding: FragmentWebViewBinding
    private val viewModel by activityViewModels<MainViewModel>()

    private var permissionRequest: PermissionRequest? = null
    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            Log.d(TAG, "Permission granted: $isGranted")
            permissionRequest?.apply {
                if (isGranted) {
                    grant(resources)
//                    if(photoUri!= null)
//                        launcherCamera.launch(photoUri)
                }
                else deny()
            }
        }

    private var filePickerCallback: ValueCallback<Array<Uri>>? = null
    private val launcherFilePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            Log.d(TAG, "registerForActivityResult: ${it.data?.data}")
            it.data?.apply {
                clipData?.apply {
                    val uris = mutableListOf<Uri>()
                    for (i in 0 until itemCount) {
                        uris.add(getItemAt(i).uri)
                    }
                    Log.d(TAG, "registerForActivityResult: $uris")
                    filePickerCallback?.onReceiveValue(uris.toTypedArray())
                } ?: data?.apply {
                    filePickerCallback?.onReceiveValue(arrayOf(this))
                }
            }
        }
    private val launcherSingleMediaPicker =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            Log.d(TAG, "registerForActivityResult: $it")
            it?.let {
                filePickerCallback?.onReceiveValue(arrayOf(it))
            }
        }

    private val launcherMultipleMediaPicker =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) {
            Log.d(TAG, "registerForActivityResult: $it")
            filePickerCallback?.onReceiveValue(it.toTypedArray())
        }

//    private var photoUri: Uri? = null
//    private val launcherCamera =
//        registerForActivityResult(ActivityResultContracts.TakePicture()) {
//            Log.d(TAG, "registerForActivityResult: $it")
//            if (it && photoUri != null)
//                filePickerCallback?.onReceiveValue(listOfNotNull(photoUri).toTypedArray())
//            photoUri = null
//        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentWebViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWebView()
        setupListener()
        binding.toolbar.changeVisibility(Constants.showToolbar)
        binding.progressBar.changeVisibility(Constants.showLoading)
        binding.btnMenu.changeVisibility(Constants.showMenu)
    }

    private fun setupListener() {
        binding.btnBack.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
        binding.btnMenu.setOnClickListener {
            activity?.findViewById<DrawerLayout>(R.id.drawer_layout)?.openDrawer(GravityCompat.END)
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            val drawer = activity?.findViewById<DrawerLayout>(R.id.drawer_layout)
            if (drawer?.isDrawerOpen(GravityCompat.END) == true)
                drawer.close()
            else if (binding.webView.canGoBack())
                binding.webView.goBack()
            else
                activity?.showExitDialog()
        }
    }



    @Suppress("DEPRECATION")
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val dbPath =
            context?.getDir("database", Context.MODE_PRIVATE)?.path

        binding.webView.apply {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
//            setInitialScale(100)
//            evaluateJavascript("""
//                var meta = document.createElement('meta');
//                meta.name = "viewport";
//                meta.content = "width=2024";
//                document.getElementsByTagName('head')[0].appendChild(meta);
//            """.trimIndent(), null)
            setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY)
            setScrollbarFadingEnabled(false)
            settings.apply {
                // Essential JavaScript settings
                javaScriptEnabled = true
                javaScriptCanOpenWindowsAutomatically = true

                // DOM and database settings
                domStorageEnabled = true
                databaseEnabled = true
                databasePath = dbPath

                // Cache settings
                cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

                // Viewport and zoom settings
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                defaultZoom = WebSettings.ZoomDensity.FAR

                // Content loading settings
                loadsImagesAutomatically = true
                allowContentAccess = true
                allowFileAccess = true

                // CRITICAL: Enable these for modern web apps
                allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true

                // Additional settings for modern JavaScript
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW


                // Enable hardware acceleration
                setLayerType(View.LAYER_TYPE_HARDWARE, null)

                // Set user agent to modern browser
                userAgentString = "Mozilla/5.0 (Linux; Android ${Build.VERSION.RELEASE}; ${Build.MODEL}) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
//                userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"

                // Enable multiple windows support
                setSupportMultipleWindows(true)

                // Set minimum font size
                minimumFontSize = 8

                // Enable text selection
                textZoom = 100
            }
            // Enable debugging for development

            WebView.setWebContentsDebuggingEnabled(true)
            if(!Constants.showLoading) return
                binding.progressBar.progress = progress
            webViewClient = object : WebViewClient() {

                override fun onLoadResource(view: WebView, url: String?) {
                    Log.d("WebView", "Loading resource: $url")
//                    view.evaluateJavascript(
//                        "document.querySelector('meta[name=\"viewport\"]').setAttribute('content', 'width=2024px, initial-scale=' + (document.documentElement.clientWidth / 1024));",
//                        null
//                    )
                }

                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    Log.d("WebView", "Page finished loading: $url")
                    if(!Constants.showLoading) return
                    lifecycleScope.launch {
                        delay(500)
                        binding.progressBar.invisible()
                    }
                }
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    Log.d("WebView", "Page started loading: $url")
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    Log.e("WebView", "Error: ${ if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) error?.description else "Unknown error"}")

                }

                override fun onReceivedHttpError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    errorResponse: WebResourceResponse?
                ) {
                    super.onReceivedHttpError(view, request, errorResponse)
                    Log.e("WebView", "HTTP Error: ${errorResponse?.statusCode}")
                }

                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?,
                ): WebResourceResponse? {
                    Log.d("WebView", "Intercepting request: ${request?.url}")
                    return super.shouldInterceptRequest(view, request)
                }

                @Deprecated("Deprecated")
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
//                    binding.btnBack.changeVisibility(binding.webView.canGoBack())
                    viewModel.url = url
                    viewModel.setLastBrowsedLink(url)
                    when {
                        url.startsWith("intent://") ->
                            startActivity(Intent.parseUri(url, Intent.URI_INTENT_SCHEME))

                        url.startsWith("tel:") -> {
                            startActivity(Intent(Intent.ACTION_DIAL, url.toUri()))
                        }

                        url.startsWith("mailto:") ->
                            startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))

                        url.contains("youtube.com/") || url.contains("youtu.be/") ->
                            startActivity(
                                Intent(Intent.ACTION_VIEW, url.toUri()).setPackage("com.google.android.youtube")
                            )

                        url.startsWith("fb://") ->
                            startActivity(
                                Intent(Intent.ACTION_VIEW, url.toUri()).setPackage("com.facebook.katana")
                            )

                        else -> {
                            view.loadUrl(url)
                            if(!Constants.showLoading) return true
                            binding.progressBar.visible()
                        }
                    }
                    return true
                }
            }
            webChromeClient = object : WebChromeClient() {

                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    Log.d("WebView Console", "${consoleMessage?.message()} -- From line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                    return true
                }
                override fun onJsAlert(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    result: JsResult?
                ): Boolean {
                    Log.d("WebView", "JS Alert: $message")
                    result?.confirm()
                    return true
                }

                override fun onJsConfirm(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    result: JsResult?
                ): Boolean {
                    Log.d("WebView", "JS Confirm: $message")
                    result?.confirm()
                    return true
                }

                // Grant permissions for cam
                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?,
                ): Boolean {
                    filePickerCallback = filePathCallback
                    val acceptTypes= fileChooserParams?.acceptTypes?.toList().orEmpty()
                    Log.d(TAG, "onShowFileChooser: $acceptTypes")
//                    val captureEnabled = fileChooserParams?.isCaptureEnabled ?: false
                    val isPhoto = acceptTypes.find {
                        it.contains("image/") || it == ".jpg" || it == ".jpeg" || it == ".png"
                    } != null
                    val isVideo = acceptTypes.find {
                        it.contains("video/") || it.equals(".mp4") || it == ".avi" || it == ".mkv"
                    } != null
//                    val capturePhoto = captureEnabled && isPhoto
//                    val captureVideo = captureEnabled && isVideo

                    when (fileChooserParams?.mode) {
                        FileChooserParams.MODE_OPEN -> {
//                            if(capturePhoto){
//                                val directory = File(context.filesDir, "camera_images")
//                                if (!directory.exists())
//                                    directory.mkdirs()
//                                val file = File(directory, "${System.currentTimeMillis()}.jpg")
////                                photoUri = Uri.fromFile(file)
//                                photoUri = FileProvider. getUriForFile (requireContext(), requireContext().applicationContext.packageName +".provider", file)
//
//                                if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
//                                    == PackageManager.PERMISSION_GRANTED
//                                ) launcherCamera.launch(photoUri)
//                                else
//                                    permissionRequestLauncher.launch(android.Manifest.permission.CAMERA)
//                            }
//                            else
                            if (isPhoto)
                                launcherSingleMediaPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            else if (isVideo)
                                launcherSingleMediaPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                            else
                                launcherFilePicker.launch(Intent.createChooser(Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT), "Select a file"))
                            isFilePickerActive = true
                        }
                        FileChooserParams.MODE_OPEN_MULTIPLE ->{
                            if(isPhoto)
                                launcherMultipleMediaPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            else if(isVideo)
                                launcherMultipleMediaPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                            else {
                                launcherFilePicker.launch(
                                    Intent.createChooser(
                                        Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT)
                                            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true),
                                        "Select files"
                                    )
                                )
                            }
                            isFilePickerActive = true
                        }

                        FileChooserParams.MODE_SAVE -> Unit
                    }
                    return true
//                    return super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
                }

                override fun onPermissionRequest(request: PermissionRequest) {
                    permissionRequest = request
                    Log.d(TAG, "onPermissionRequest")
                    lifecycleScope.launch {
                        Log.d(TAG, request.origin.toString())
                        request.resources.forEach {
                            when (it) {
                                PermissionRequest.RESOURCE_VIDEO_CAPTURE ->
                                    permissionRequestLauncher.launch(android.Manifest.permission.CAMERA)

                                PermissionRequest.RESOURCE_AUDIO_CAPTURE ->
                                    permissionRequestLauncher.launch(android.Manifest.permission.RECORD_AUDIO)

                                PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID ->
                                    permissionRequestLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                        }
                        if (request.toString() == "file:///") {
                            Log.d(TAG, "GRANTED")
                            request.grant(request.resources)
                        } else {
                            Log.d(TAG, "DENIED")
                            request.deny()
                        }
                    }
                }

            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(!isFilePickerActive)
            binding.webView.loadUrl(viewModel.getLastBrowsedLink())
        else
            isFilePickerActive = false
    }

    override fun onPause() {
        super.onPause()
        binding.webView.url?.let {
            viewModel.setLastBrowsedLink(it, arguments?.getInt("index") ?: 0)
        }
    }

    companion object {
        private const val TAG = "WebViewFragment"
        private var isFilePickerActive = false
    }
}