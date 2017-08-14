package br.com.andrecouto.kotlin.chatlib

import android.app.Application
import android.content.Context
class AppContext: Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        initializeSocket()
    }

    fun initializeSocket() {
        //AppSocketListener.getInstance().initialize()
    }

    fun destroySocketListener() {
        //AppSocketListener.getInstance().destroy()
    }

    override fun onTerminate() {
        super.onTerminate()
        destroySocketListener()
    }

    companion object {
        var appContext : Context? = null
    }
}
