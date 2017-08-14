package br.com.andrecouto.kotlin.chatlib.socketio.events

object SocketEventConstants {
    var newMessage = "new message"
    var stopTyping = "stop typing"
    var typing = "typing"
    var userJoined = "user joined"
    var userLeft = "user left"
    var socketConnection = "socket.connection"
    var login = "login"
    var addUser = "add user"
    var connectionFailure = "failedToConnect"
}