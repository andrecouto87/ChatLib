package br.com.andrecouto.kotlin.chatlib.socketio

interface SocketListener {
    fun onSocketConnected()
    fun onSocketDisconnected()
    fun onNewMessageReceived(username:String, message:String)
}