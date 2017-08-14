package br.com.andrecouto.kotlin.chatlib.socketio.interfaces

interface SocketListener {
    fun onSocketConnected()
    fun onSocketDisconnected()
    fun onNewMessageReceived(username:String, message:String)
}