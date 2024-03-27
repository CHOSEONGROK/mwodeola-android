package com.jojo.android.mwodeola

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.jojo.android.mwodeola.model.RetrofitService
import com.jojo.android.mwodeola.presentation.main.MainActivity
import com.jojo.android.mwodeola.presentation.security.BiometricHelper
import com.jojo.android.mwodeola.presentation.security.screenLock.ScreenLockHandler

class MyApplication : Application() {
    companion object { private const val TAG = "MyApplication" }

    var DEBUG: Boolean = false

    private lateinit var lifecycleHandler: ScreenLockHandler

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate()")
        DEBUG = isDebuggable(this)

        BiometricHelper.init(applicationContext)

        lifecycleHandler = ScreenLockHandler(applicationContext).also {
            it.setFirstSubscriber(MainActivity::class.java)

            registerActivityLifecycleCallbacks(it)
            registerComponentCallbacks(it)
        }
    }



    override fun onLowMemory() {
        super.onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.w(TAG, "onTerminate()")
        unregisterActivityLifecycleCallbacks(lifecycleHandler)
        unregisterComponentCallbacks(lifecycleHandler)
    }

    override fun registerActivityLifecycleCallbacks(callback: ActivityLifecycleCallbacks?) {
        super.registerActivityLifecycleCallbacks(callback)
        Log.i(TAG, "registerActivityLifecycleCallbacks(), screenLockCallback=$callback")
    }

    fun signOut() {

    }

    private fun isDebuggable(context: Context): Boolean {
        var debuggable = false
        val pm: PackageManager = context.packageManager
        try {
            val appInfo: ApplicationInfo = pm.getApplicationInfo(context.packageName, 0)
            debuggable = 0 != appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
        } catch (e: PackageManager.NameNotFoundException) {
            /* debuggable variable will remain false */
        }

        return debuggable
    }
}
