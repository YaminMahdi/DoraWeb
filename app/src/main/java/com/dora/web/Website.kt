package com.dora.web

import androidx.annotation.DrawableRes

const val baseUrl = BuildConfig.BASE_URL

enum class Website(val url: String, @param:DrawableRes val icon: Int = R.drawable.web) {
    Home(baseUrl, R.drawable.home),
    OurServices("$baseUrl/services"),
    About("$baseUrl/about"),
}

val Website.pageName
    get() = name
        .replace('_', ' ')
        .replace("([a-z])([A-Z])".toRegex(), "$1 $2")

val String?.website
    get() = Website.entries.find { it.url == this } ?: Website.Home