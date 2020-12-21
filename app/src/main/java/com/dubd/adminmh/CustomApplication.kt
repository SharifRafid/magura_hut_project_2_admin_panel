package com.dubd.adminmh

import android.app.Application
import android.content.Context
import android.graphics.Color
import androidx.multidex.MultiDex
import io.customerly.Customerly

class CustomApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Customerly.configure(application = this, customerlyAppId = "039e4f8c", widgetColorInt = Color.parseColor("#689F38"))

    }
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

}