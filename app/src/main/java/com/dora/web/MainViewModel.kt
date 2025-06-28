package com.dora.web

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class MainViewModel(private val savedStateHandle: SavedStateHandle): ViewModel() {

    var url
        get() = savedStateHandle["baseUrl"] ?: Website.entries[currentIndex].url
        set(value) { savedStateHandle["baseUrl"] = value }

    var currentIndex
        get() = savedStateHandle["currentIndex"] ?: Website.Home.ordinal
        set(value) { savedStateHandle["currentIndex"] = value }

    private var lastBrowsedLinks
        get() = savedStateHandle["lastBrowsedLinks"] ?: Website.entries.map { it.url }
        set(value) { savedStateHandle["lastBrowsedLinks"] = value }

    fun getLastBrowsedLink() = lastBrowsedLinks[currentIndex]

    fun setLastBrowsedLink(link: String, index: Int = currentIndex) {
        lastBrowsedLinks = lastBrowsedLinks.toMutableList().apply {
            removeAt(index)
            add(index, link)
        }
    }

}