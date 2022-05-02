package com.jojo.android.mwodeola.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

class FloatingService : Service() {
    companion object { const val TAG = "FloatingService" }

    lateinit var floatingHeadWindow: FloatingHeadWindow
    private val mBinder = LocalBinder()


    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand")
        init()
        return super.onStartCommand(intent, flags, startId)
    }


    fun init() {
        Log.e(TAG, "init")
        if (!::floatingHeadWindow.isInitialized) {
            Log.e(TAG, "is not initalized")
            floatingHeadWindow = FloatingHeadWindow(applicationContext).apply {
                create()
                createLayoutParams()
                show()
            }
        }
    }


    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "onCreate")
    }


    inner class LocalBinder : Binder() {
        fun getService(): FloatingService {
            return this@FloatingService
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (!::floatingHeadWindow.isInitialized) {
            floatingHeadWindow.hide()
        }
        Log.e(TAG, "onDestroy")
    }
}
