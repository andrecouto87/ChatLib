package br.com.andrecouto.kotlin.chatlib.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import br.com.andrecouto.kotlin.chatlib.socketio.service.SocketIOService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {
        val serviceIntent = Intent(SocketIOService::class.java.name)
        p0!!.startService(serviceIntent)
    }

}