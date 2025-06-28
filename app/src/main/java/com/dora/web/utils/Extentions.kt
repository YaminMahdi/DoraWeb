package com.dora.web.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import com.dora.web.BuildConfig
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Any?.log(tag: String = "TAG", hints: String = "") {
    if (BuildConfig.DEBUG)
        Log.i("log> '$tag'", "$hints: ${this?.javaClass?.name}: $this")
}

/**View Extension Function*/
fun View.changeVisibility(isVisible: Boolean, useGone: Boolean = true) {
    visibility = if (isVisible) View.VISIBLE else if (useGone) View.GONE else View.INVISIBLE
}

fun View.visible() {
    if(this.visibility != View.VISIBLE)
        this.visibility = View.VISIBLE
}

fun View.gone() {
    if(this.visibility != View.GONE)
        this.visibility = View.GONE
}

fun View.invisible() {
    if(this.visibility != View.INVISIBLE)
        this.visibility = View.INVISIBLE
}

fun Activity.showExitDialog() {
    MaterialAlertDialogBuilder(this)
        .setTitle("Exit App")
        .setMessage("Are you sure you want to exit?")
        .setPositiveButton("Exit") { _, _ ->
            this.applicationContext
            finish()
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .setCancelable(true) // Allow dismissing by touching outside
        .show()
}

fun Context?.toast(text: String?, duration: Int = Toast.LENGTH_SHORT) {
    this ?: return
    text?.let { Toast.makeText(this, text, duration).show() }
}