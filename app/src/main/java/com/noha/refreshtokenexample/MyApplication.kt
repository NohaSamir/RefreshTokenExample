package com.noha.refreshtokenexample

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context


class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var context: Context? = null

        val applicationContext: Context?
            get() = context
    }
}