package com.dora.web

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.bundleOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.dora.web.databinding.ActivityMainBinding
import com.dora.web.utils.ConnectionListener
import com.dora.web.utils.InAppUpdate
import com.dora.web.utils.log
import com.dora.web.utils.showExitDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel>()
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController : NavController

    private val inAppUpdate = InAppUpdate(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        inAppUpdate.checkForUpdate()
        if (BuildConfig.isDarkMode) {
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(window.decorView.rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.contentMain.root.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        setupUI()
        setupListener()
    }

    override fun onResume() {
        super.onResume()
        inAppUpdate.onResume()
    }

    private fun setupUI() {
        binding.apply {

            navView.menu.apply {
                Website.entries.forEach {
                    add(0, it.ordinal, Menu.NONE, it.pageName).setIcon(it.icon)
                }
                if (BuildConfig.showShareApp)
                    add(1, 1, Menu.NONE, "Share this app").setIcon(R.drawable.share)
                add(1, 0, Menu.NONE, "Exit").setIcon(R.drawable.ic_logout)
                setGroupCheckable(0, true, true)
                setGroupCheckable(1, false, true)
            }
            navView.menu[0].isChecked = true

            navController =  (supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment)
                .findNavController()
        }
    }


    private fun setupListener() {
        binding.apply {
            navController.addOnDestinationChangedListener { _, _, arguments ->
                arguments?.getString("url").website.also {
                    if (!navView.menu[it.ordinal].isChecked) {
                        navView.menu[it.ordinal].isChecked = true
                    }
                }
            }
            navView.setNavigationItemSelectedListener {
                drawerLayout.closeDrawers()
                when (it.groupId) {
                    1 -> {
                        when(it.itemId){
                            0 -> showExitDialog()
                            1 -> shareApp()
                        }
                    }
                    else -> {
                        val currentUrl = Website.entries[it.itemId].url
                        viewModel.url = currentUrl
                        viewModel.currentIndex = it.itemId
                        runCatching {
                            navController.navigate(
                                resId = R.id.nav_web,
                                args = bundleOf("url" to currentUrl, "index" to it.itemId),
                                navOptions = NavOptions.Builder().build().apply {
                                    shouldPopUpToSaveState()
                                    shouldRestoreState()
                                    shouldLaunchSingleTop()
                                }
                            )
                        }
                    }
                }
                true
            }
        }
        lifecycleScope.launch {
            delay(2.seconds)
            connectionListener.connectionStatusFlow.collect {
                "ConnectionListener connectionStatusFlow collect: $it".log("ConnectionListener")
                when(it){
                    ConnectionListener.State.Default -> {
                        // Initial state, no action needed
                    }
                    ConnectionListener.State.HasConnection, ConnectionListener.State.Dismissed -> {
                        // Connection restored, dismiss any existing dialog
                        dismissNoInternetDialog()
                    }
                    ConnectionListener.State.NoConnection -> {
                        // Show no internet dialog
                        showNoInternetDialog()
                    }
                }
            }
        }
    }

    private var noInternetDialog: AlertDialog? = null

    private fun showNoInternetDialog() {
        // Don't show multiple dialogs
        if (noInternetDialog?.isShowing == true) return

        noInternetDialog = MaterialAlertDialogBuilder(this)
            .setTitle("No Internet Connection")
            .setMessage("Some features may not work properly without internet connection.")
            .setIcon(R.drawable.ic_wifi_off) // Optional icon
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
                noInternetDialog = null
            }
            .setOnDismissListener {
                noInternetDialog = null
            }
            .show()
    }

    private fun dismissNoInternetDialog() {
        noInternetDialog?.let { dialog ->
            if (dialog.isShowing)
                dialog.dismiss()
        }
        noInternetDialog = null
    }

    private fun shareApp() {
        val playStoreUrl = "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, playStoreUrl)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share this app")
        startActivity(shareIntent)

    }
}



