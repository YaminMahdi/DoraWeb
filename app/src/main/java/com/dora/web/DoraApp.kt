package com.dora.web

import android.app.Application
import com.dora.web.utils.ConnectionListener

class DoraApp : Application() {
    val connectionListener by lazy {ConnectionListener(this)}

    override fun onCreate() {
        super.onCreate()
        instance = this
        connectionListener.registerListener()
    }

    companion object {
        lateinit var instance: DoraApp
            private set
    }
}

val connectionListener by lazy { DoraApp.instance.connectionListener }