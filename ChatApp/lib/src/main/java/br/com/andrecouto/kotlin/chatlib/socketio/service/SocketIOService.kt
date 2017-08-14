package br.com.andrecouto.kotlin.chatlib.socketio.service

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import br.com.andrecouto.kotlin.chatlib.R
import br.com.andrecouto.kotlin.chatlib.activity.ChatMainActivity
import br.com.andrecouto.kotlin.chatlib.constants.Constants
import br.com.andrecouto.kotlin.chatlib.socketio.events.SocketEventConstants
import br.com.andrecouto.kotlin.chatlib.socketio.interfaces.SocketListener
import org.json.JSONException
import org.json.JSONObject
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import java.lang.Exception
import java.lang.RuntimeException

class SocketIOService:Service() {
    private var socketListener : SocketListener? = null
    private var appConnectedToService : Boolean? = false
    private var mSocket : Socket? = null
    private var serviceBinded = false
    private val mBinder = LocalBinder()

    fun setAppConnectedToService(appConnectedToService : Boolean?) {
        this.appConnectedToService = appConnectedToService
    }
    fun setSocketListener(socketListener:SocketListener) {
        this.socketListener = socketListener
    }
    inner class LocalBinder:Binder() {
        val service:SocketIOService
            get() {
                return this@SocketIOService
            }
    }
    fun setServiceBinded(serviceBinded:Boolean) {
        this.serviceBinded = serviceBinded
    }
    override fun onBind(intent:Intent):IBinder {
        return mBinder
    }
    override fun onCreate() {
        super.onCreate()
        initializeSocket()
        addSocketHandlers()
    }
    override fun onDestroy() {
        super.onDestroy()
        closeSocketSession()
    }
    override fun onUnbind(intent:Intent):Boolean {
        return serviceBinded
    }
    override fun onStartCommand(intent:Intent, flags:Int, startId:Int):Int {
        return START_STICKY
    }
    private fun initializeSocket() {
        try
        {
            val options = IO.Options()
            options.forceNew = true
            mSocket = IO.socket(Constants.CHAT_SERVER_URL, options)
        }
        catch (e: Exception) {
            Log.e("Error", "Exception in socket creation")
            throw RuntimeException(e)
        }
    }
    private fun closeSocketSession() {
        mSocket!!.disconnect()
        mSocket!!.off()
    }
    private fun addSocketHandlers() {
        mSocket!!.on(Socket.EVENT_CONNECT, object:Emitter.Listener {
            override fun call(vararg args:Any) {
                val intent = Intent(SocketEventConstants.socketConnection)
                intent.putExtra("connectionStatus", true)
                broadcastEvent(intent)
            }
        })
        mSocket!!.on(Socket.EVENT_DISCONNECT, object:Emitter.Listener {
            override fun call(vararg args:Any) {
                val intent = Intent(SocketEventConstants.socketConnection)
                intent.putExtra("connectionStatus", false)
                broadcastEvent(intent)
            }
        })
        mSocket!!.on(Socket.EVENT_CONNECT_ERROR, object:Emitter.Listener {
            override fun call(vararg args:Any) {
                val intent = Intent(SocketEventConstants.connectionFailure)
                broadcastEvent(intent)
            }
        })
        mSocket!!.on(Socket.EVENT_CONNECT_TIMEOUT, object:Emitter.Listener {
            override fun call(vararg args:Any) {
                val intent = Intent(SocketEventConstants.connectionFailure)
                broadcastEvent(intent)
            }
        })

        mSocket!!.connect()
    }
    fun addNewMessageHandler() {
        mSocket!!.off(SocketEventConstants.newMessage)
        mSocket!!.on(SocketEventConstants.newMessage, object:Emitter.Listener {
            override fun call(vararg args:Any) {
                val data = args[0] as JSONObject
                val username:String
                val message:String
                try
                {
                    username = data.getString("username")
                    message = data.getString("message")
                }
                catch (e:JSONException) {
                    return
                }
                if (isForeground("com.example.mahabali.socketiochat"))
                {
                    val intent = Intent(SocketEventConstants.newMessage)
                    intent.putExtra("username", username)
                    intent.putExtra("message", message)
                    broadcastEvent(intent)
                }
                else
                {
                    showNotificaitons(username, message)
                }
            }
        })
    }
    fun removeMessageHandler() {
        mSocket!!.off(SocketEventConstants.newMessage)
    }
    fun emit(event:String, args:Array<Any>, ack:Ack) {
        mSocket!!.emit(event, args, ack)
    }
    fun emit(event:String, vararg args:Any) {
        try
        {
            mSocket!!.emit(event, args, null)
        }
        catch (e:Exception) {
            e.printStackTrace()
        }
    }
    fun addOnHandler(event:String, listener:Emitter.Listener) {
        mSocket!!.on(event, listener)
    }
    fun connect() {
        mSocket!!.connect()
    }
    fun disconnect() {
        mSocket!!.disconnect()
    }
    fun restartSocket() {
        mSocket!!.off()
        mSocket!!.disconnect()
        addSocketHandlers()
    }
    fun off(event:String) {
        mSocket!!.off(event)
    }
    private fun broadcastEvent(intent:Intent) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
    val isSocketConnected:Boolean
        get() {
            if (mSocket == null)
            {
                return false
            }
            return mSocket!!.connected()
        }
    fun showNotificaitons(username:String, message:String) {
        val intent = Intent(getApplicationContext(), ChatMainActivity::class.java)

        intent.putExtra("username", message)
        intent.putExtra("message", message)
        intent.setAction("android.intent.action.MAIN")
        val pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        val n = NotificationCompat.Builder(this)
                .setContentTitle("You have pending new messages")
                .setContentText("New Message")
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher_hdp)
                .build()
        @SuppressLint("ServiceCast")
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
        notificationManager.notify(0, n)
    }

    fun isForeground(myPackage:String):Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val runningTaskInfo = manager.getRunningTasks(1)
        val componentInfo = runningTaskInfo.get(0).topActivity
        return componentInfo.getPackageName().equals(myPackage)
    }
}