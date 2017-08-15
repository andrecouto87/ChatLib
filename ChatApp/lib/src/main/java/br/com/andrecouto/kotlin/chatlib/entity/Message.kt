package br.com.andrecouto.kotlin.chatlib.entity

class Message private constructor() {
    var type:Int = 0
    var message:String = ""
    var username:String = ""

    class Builder(type:Int) {
        private var mType:Int = 0
        private var mUsername:String = ""
        private var mMessage:String = ""

        init{
            mType = type
        }
        fun username(username:String):Builder {
            mUsername = username
            return this
        }
        fun message(message:String):Builder {
            mMessage = message
            return this
        }
        fun build():Message {
            val message = Message()
            message.type = mType
            message.username = mUsername
            message.message = mMessage
            return message
        }
    }
    companion object {
        val TYPE_MESSAGE = 0
        val TYPE_LOG = 1
        val TYPE_ACTION = 2
    }
}