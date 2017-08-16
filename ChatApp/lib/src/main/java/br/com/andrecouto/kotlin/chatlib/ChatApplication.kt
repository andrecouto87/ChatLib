package br.com.andrecouto.kotlin.chatlib

import android.app.Application
import android.content.Context
import br.com.andrecouto.kotlin.chatlib.socketio.listener.AppSocketListener

class ChatApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        initializeSocket(appContext!!)
    }

    fun initializeSocket(context: Context) {
        AppSocketListener.instance.initialize(context)
    }

    fun destroySocketListener() {
        AppSocketListener.instance.destroy()
    }

    override fun onTerminate() {
        super.onTerminate()
        destroySocketListener()
    }

    companion object {
        var appContext : Context? = null
    }
}
