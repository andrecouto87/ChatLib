package br.com.andrecouto.kotlin.chatlib.socketio.listener

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.widget.Toast
import br.com.andrecouto.kotlin.chatlib.socketio.events.SocketEventConstants
import br.com.andrecouto.kotlin.chatlib.socketio.interfaces.SocketListener
import br.com.andrecouto.kotlin.chatlib.socketio.service.SocketIOService
import io.socket.client.Ack
import io.socket.emitter.Emitter
import br.com.andrecouto.kotlin.chatlib.ChatApplication

class AppSocketListener: SocketListener {

    private var socketServiceInterface: SocketIOService? = null
    private var activeSocketListener: SocketListener? = null

    fun setActiveSocketListener(activeSocketListener : SocketListener) {
        this.activeSocketListener = activeSocketListener
        if (socketServiceInterface != null && socketServiceInterface!!.isSocketConnected)
        {
            onSocketConnected()
        }
    }

    private val serviceConnection = object:ServiceConnection {
        override fun onServiceConnected(name:ComponentName, service:IBinder) {
            socketServiceInterface = (service as SocketIOService.LocalBinder).service
            socketServiceInterface!!.setServiceBinded(true)
            socketServiceInterface!!.setSocketListener(sharedInstance!!)
            if (socketServiceInterface!!.isSocketConnected)
            {
                onSocketConnected()
            }
        }
        override fun onServiceDisconnected(name:ComponentName) {
            socketServiceInterface!!.setServiceBinded(false)
            socketServiceInterface = null
            onSocketDisconnected()
        }
    }

    fun initialize(context: Context) {
        ChatApplication.appContext = context
        val intent = Intent(context, SocketIOService::class.java)
        context.startService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        LocalBroadcastManager.getInstance(context).registerReceiver(socketConnectionReceiver, IntentFilter(SocketEventConstants.socketConnection))
        LocalBroadcastManager.getInstance(context).registerReceiver(connectionFailureReceiver, IntentFilter(SocketEventConstants.connectionFailure))
        LocalBroadcastManager.getInstance(context).registerReceiver(newMessageReceiver, IntentFilter(SocketEventConstants.newMessage))
    }

    private val socketConnectionReceiver = object:BroadcastReceiver() {
        override fun onReceive(context:Context, intent:Intent) {
            val connected = intent.getBooleanExtra("connectionStatus", false)
            if (connected)
            {
                Log.i("AppSocketListener", "Socket connected")
                onSocketConnected()
            }
            else
            {
                onSocketDisconnected()
            }
        }
    }

    private val connectionFailureReceiver = object:BroadcastReceiver() {
        override fun onReceive(context:Context, intent:Intent) {
            val toast = Toast.makeText(ChatApplication.appContext, "Please check your network connection",
                    Toast.LENGTH_SHORT)
            toast.show()
        }
    }
    private val newMessageReceiver = object:BroadcastReceiver() {
        override fun onReceive(context:Context, intent:Intent) {
            val userName = intent.getStringExtra("username")
            val message = intent.getStringExtra("message")
            onNewMessageReceived(userName, message)
        }
    }

    fun destroy() {
        socketServiceInterface!!.setServiceBinded(false)
        ChatApplication.appContext!!.unbindService(serviceConnection)
        LocalBroadcastManager.getInstance(ChatApplication.appContext).unregisterReceiver(socketConnectionReceiver)
        LocalBroadcastManager.getInstance(ChatApplication.appContext).unregisterReceiver(newMessageReceiver)
    }

    override fun onSocketConnected() {
        if (activeSocketListener != null)
        {
            activeSocketListener!!.onSocketConnected()
        }
    }

    override fun onSocketDisconnected() {
        if (activeSocketListener != null)
        {
            activeSocketListener!!.onSocketDisconnected()
        }
    }

    override fun onNewMessageReceived(username:String, message:String) {
        if (activeSocketListener != null)
        {
            activeSocketListener?.onNewMessageReceived(username, message)
        }
    }

    fun addOnHandler(event:String, listener:Emitter.Listener) {
        socketServiceInterface?.addOnHandler(event, listener)
    }

    fun emit(event:String, args:Array<Any>, ack:Ack) {
        socketServiceInterface?.emit(event, args, ack)
    }

    fun emit(event:String, args:Array<Any>?) {
        socketServiceInterface?.emit(event, args)
    }

    internal fun connect() {
        socketServiceInterface?.connect()
    }

    fun disconnect() {
        socketServiceInterface?.disconnect()
    }
    fun off(event:String) {
        if (socketServiceInterface != null)
        {
            socketServiceInterface?.off(event)
        }
    }
    val isSocketConnected:Boolean
        get() {
            if (socketServiceInterface == null)
            {
                return false
            }
            return socketServiceInterface!!.isSocketConnected
        }
    fun setAppConnectedToService(status:Boolean) {
        if (socketServiceInterface != null)
        {
            socketServiceInterface?.setAppConnectedToService(status)
        }
    }
    fun restartSocket() {
        if (socketServiceInterface != null)
        {
            socketServiceInterface?.restartSocket()
        }
    }
    fun addNewMessageHandler() {
        if (socketServiceInterface != null)
        {
            socketServiceInterface?.addNewMessageHandler()
        }
    }
    fun removeNewMessageHandler() {
        if (socketServiceInterface != null)
        {
            socketServiceInterface?.removeMessageHandler()
        }
    }
    fun signOutUser() {
        AppSocketListener.instance.disconnect()
        removeNewMessageHandler()
        AppSocketListener.instance.connect()
    }
    fun disconnectUser() {
        AppSocketListener.instance.disconnect()
        removeNewMessageHandler()
    }
    companion object {
        private var sharedInstance : AppSocketListener? = null
        val instance:AppSocketListener
            get() {
                if (sharedInstance == null)
                {
                    sharedInstance = AppSocketListener()
                }
                return sharedInstance as AppSocketListener
            }
    }
}