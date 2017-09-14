package br.com.andrecouto.kotlin.chatlib.activity

import android.os.Bundle
import br.com.andrecouto.kotlin.chatlib.R
import br.com.andrecouto.kotlin.chatlib.fragment.ChatMainFragment
import br.com.andrecouto.kotlin.chatlib.socketio.events.SocketEventConstants
import br.com.andrecouto.kotlin.chatlib.socketio.listener.AppSocketListener
import io.socket.client.Socket

class ChatMainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_main)
        if (savedInstanceState == null)
        {
            val frag = ChatMainFragment()
            frag.setArguments(getIntent().getExtras())
            getSupportFragmentManager().beginTransaction().add(R.id.chat_activity_content_id, frag).commit()
        }
    }

    private fun leave() {
        removeHandlers()
        AppSocketListener.instance.disconnectUser()
    }

    fun removeHandlers() {
        AppSocketListener.instance.off(Socket.EVENT_CONNECT_ERROR)
        AppSocketListener.instance.off(Socket.EVENT_CONNECT_TIMEOUT)
        AppSocketListener.instance.off(SocketEventConstants.newMessage)
        AppSocketListener.instance.off(SocketEventConstants.userJoined)
        AppSocketListener.instance.off(SocketEventConstants.userLeft)
        AppSocketListener.instance.off(SocketEventConstants.typing)
        AppSocketListener.instance.off(SocketEventConstants.stopTyping)
        AppSocketListener.instance.off(SocketEventConstants.login)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        leave()
    }

    override fun onDestroy() {
        super.onDestroy()
        leave()
    }
}