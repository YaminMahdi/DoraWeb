package com.dora.web

import androidx.annotation.DrawableRes

val baseUrl = BuildConfig.BASE_URL.trim { it == '/' }

enum class Website(val url: String, @param:DrawableRes val icon: Int) {
    Home(baseUrl, R.drawable.home),
    Services("$baseUrl/services", R.drawable.ic_puzzle),
    About("$baseUrl/about", R.drawable.ic_info),
}

val Website.pageName
    get() = name
        .replace('_', ' ')
        .replace("([a-z])([A-Z])".toRegex(), "$1 $2")

val String?.website
    get() = Website.entries.find { it.url == this } ?: Website.Home