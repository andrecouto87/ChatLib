package br.com.andrecouto.kotlin.chatlib.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.annotation.Nullable
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import br.com.andrecouto.kotlin.chatlib.R
import br.com.andrecouto.kotlin.chatlib.adapter.MessageAdapter
import br.com.andrecouto.kotlin.chatlib.entity.Message
import br.com.andrecouto.kotlin.chatlib.socketio.events.SocketEventConstants
import br.com.andrecouto.kotlin.chatlib.socketio.interfaces.SocketListener
import br.com.andrecouto.kotlin.chatlib.socketio.listener.AppSocketListener
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.fragment_chat_main.*
import org.json.JSONException
import org.json.JSONObject


class ChatMainFragment: BaseFragment(), SocketListener {

    private val TYPING_TIMER_LENGTH: Long = 600
    private var mMessages = ArrayList<Message>()
    private var mTyping = false
    private var mTypingHandler = Handler()
    private var mUsername:String = ""

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppSocketListener.instance.setActiveSocketListener(this)
        // Restart Socket.io to avoid weird stuff ;-)
        AppSocketListener.instance.restartSocket()
        setHasOptionsMenu(false)
    }

    override fun onStart() {
        super.onStart()
        //attemptAutoLogin()
    }
    override fun onResume() {
        super.onResume()
        scrollToBottom()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Retorna a view /res/layout/fragment_carros.xml
        val view = inflater?.inflate(R.layout.fragment_chat_main, container, false)
        return view
    }

    override fun onDestroy() {
        removeHandlers()
        super.onDestroy()
    }

    fun removeHandlers() {
        AppSocketListener.instance.off(Socket.EVENT_CONNECT_ERROR)
        AppSocketListener.instance.off(Socket.EVENT_CONNECT_TIMEOUT)
        AppSocketListener.instance.off(SocketEventConstants.newMessage)
        AppSocketListener.instance.off(SocketEventConstants.userJoined)
        AppSocketListener.instance.off(SocketEventConstants.userLeft)
        AppSocketListener.instance.off(SocketEventConstants.typing)
        AppSocketListener.instance.off(SocketEventConstants.stopTyping)
    }

    override fun onViewCreated(view:View, savedInstanceState:Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        message_input.setOnEditorActionListener(object:TextView.OnEditorActionListener {
            override fun onEditorAction(v:TextView, id:Int, event: KeyEvent):Boolean {
                if (id == 0 || id == EditorInfo.IME_NULL)
                {
                    attemptSend()
                    return true
                }
                return false
            }
        })

        message_input.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (null == mUsername) return
                // if (!mSocket.connected()) return;
                if (!mTyping) {
                    mTyping = true
                    AppSocketListener.instance.emit("typing")
                }
                mTypingHandler.removeCallbacks(onTypingTimeout)
                mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH)
            }
        })

        send_button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                attemptSend()
            }
        })
        recyclerView.setLayoutManager(LinearLayoutManager(getActivity()))
        recyclerView.setAdapter(MessageAdapter(activity, mMessages))

    }

    override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (Activity.RESULT_OK !== resultCode)
        {
            getActivity().finish()
            return
        }
        mUsername = data.getStringExtra("username")
        val numUsers = data.getIntExtra("numUsers", 1)
        addLog(getResources().getString(R.string.message_welcome))
        addParticipantsLog(numUsers)
    }

    private fun addLog(message:String) {
        getActivity().runOnUiThread(object:Runnable {
            public override fun run() {
                mMessages.add(Message.Builder(Message.TYPE_LOG)
                        .message(message).build())
                MessageAdapter(activity,mMessages).notifyItemInserted(mMessages.size - 1)
                scrollToBottom()
            }
        })
    }

    private fun addParticipantsLog(numUsers:Int) {
        addLog(getResources().getQuantityString(R.plurals.message_participants, numUsers, numUsers))
    }

    private fun addMessage(username:String, message:String) {
        mMessages.add(Message.Builder(Message.TYPE_MESSAGE)
                .username(username).message(message).build())
        MessageAdapter(activity,mMessages).notifyItemInserted(mMessages.size - 1)
        scrollToBottom()
    }

    private fun addTyping(username: String) {
        mMessages.add(Message.Builder(Message.TYPE_ACTION)
                .username(username).build())
        MessageAdapter(activity,mMessages).notifyItemInserted(mMessages.size - 1)
        scrollToBottom()
    }

    private fun removeTyping(username: String) {
        for (i in mMessages.size - 1 downTo 0) {
            val message = mMessages.get(i)
            if (message.type === Message.TYPE_ACTION && message.username.equals(username)) {
                mMessages.removeAt(i)
                MessageAdapter(activity,mMessages).notifyItemRemoved(i)
            }
        }
    }

    private fun attemptSend() {
        if (null == mUsername) return
        mTyping = false
        val message = message_input.getText().toString().trim()
        if (TextUtils.isEmpty(message)) {
            message_input.requestFocus()
            return
        }
        message_input.setText("")
        addMessage(mUsername, message)
        // perform the sending message attempt.
        AppSocketListener.instance.emit("new message", message)
    }

    /*private fun attemptAutoLogin() {
        if (PreferenceStorage.shouldDoAutoLogin()) {
            mUsername = PreferenceStorage.getUsername()
        } else {
            startSignIn()
        }
    }*/

    /*private fun startSignIn() {
        mUsername = null
        val intent = Intent(getActivity(), LoginActivity::class.java)
        startActivityForResult(intent, REQUEST_LOGIN)
    }*/

    private fun leave() {
        removeHandlers()
        mUsername = ""
        //PreferenceStorage.clearUserSession()
        AppSocketListener.instance.signOutUser()
        //startSignIn()
    }

    /*fun askForLogout() {
        AlertDialog.Builder(getActivity())
                .setTitle("Logout")
                .setMessage("Do you want to logout")
                .setPositiveButton("Logout", object:DialogInterface.OnClickListener() {
                    fun onClick(dialog:DialogInterface, which:Int) {
                        // continue with delete
                        leave()
                    }
                })
                .setNegativeButton("Cancel", object:DialogInterface.OnClickListener() {
                    fun onClick(dialog:DialogInterface, which:Int) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
    }*/

    private fun scrollToBottom() {
        recyclerView.scrollToPosition(MessageAdapter(activity, mMessages).getItemCount() - 1)
    }

    private val onConnectError = object : Emitter.Listener {
        override fun call(vararg args: Any) {
            Log.i("Failed", "Failed to connect")
            getActivity().runOnUiThread(object : Runnable {
                public override fun run() {
                    Toast.makeText(getActivity().getApplicationContext(),
                            R.string.error_connect, Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private val onNewMessage = object : Emitter.Listener {
        override fun call(vararg args: Any) {
            getActivity().runOnUiThread(object : Runnable {
                public override fun run() {
                    val data = args[0] as JSONObject
                    val username: String
                    val message: String
                    try {
                        username = data.getString("username")
                        message = data.getString("message")
                    } catch (e: JSONException) {
                        return
                    }
                    removeTyping(username)
                    addMessage(username, message)
                }
            })
        }
    }

    private val onUserJoined = object : Emitter.Listener {
        override fun call(vararg args: Any) {
            getActivity().runOnUiThread(object : Runnable {
                public override fun run() {
                    val data = args[0] as JSONObject
                    val username: String
                    val numUsers: Int
                    try {
                        username = data.getString("username")
                        numUsers = data.getInt("numUsers")
                    } catch (e: JSONException) {
                        return
                    }
                    addLog(getResources().getString(R.string.message_user_joined, username))
                    addParticipantsLog(numUsers)
                }
            })
        }
    }

    private val onUserLeft = object : Emitter.Listener {
        override fun call(vararg args: Any) {
            getActivity().runOnUiThread(object : Runnable {
                public override fun run() {
                    val data = args[0] as JSONObject
                    val username: String
                    val numUsers: Int
                    try {
                        username = data.getString("username")
                        numUsers = data.getInt("numUsers")
                    } catch (e: JSONException) {
                        return
                    }
                    addLog(getResources().getString(R.string.message_user_left, username))
                    addParticipantsLog(numUsers)
                    removeTyping(username)
                }
            })
        }
    }

    private val onTyping = object : Emitter.Listener {
        override fun call(vararg args: Any) {
            getActivity().runOnUiThread(object : Runnable {
                public override fun run() {
                    val data = args[0] as JSONObject
                    val username: String
                    try {
                        username = data.getString("username")
                    } catch (e: JSONException) {
                        return
                    }
                    addTyping(username)
                }
            })
        }
    }

    private val onStopTyping = object : Emitter.Listener {
        override fun call(vararg args: Any) {
            getActivity().runOnUiThread(object : Runnable {
                public override fun run() {
                    val data = args[0] as JSONObject
                    val username: String
                    try {
                        username = data.getString("username")
                    } catch (e: JSONException) {
                        return
                    }
                    removeTyping(username)
                }
            })
        }
    }

    private val onLogin = object : Emitter.Listener {
        override fun call(vararg args: Any) {
            val data = args[0] as JSONObject
            val numUsers: Int
            try {
                numUsers = data.getInt("numUsers")
            } catch (e: JSONException) {
                return
            }
            addLog(getResources().getString(R.string.message_welcome))
            addParticipantsLog(numUsers)
        }
    }
    private val onTypingTimeout = object : Runnable {
        public override fun run() {
            if (!mTyping) return
            mTyping = false
            AppSocketListener.instance.emit(SocketEventConstants.stopTyping)
        }
    }

    override fun onSocketConnected() {
        AppSocketListener.instance.addOnHandler(Socket.EVENT_CONNECT_ERROR, onConnectError)
        AppSocketListener.instance.addOnHandler(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
        AppSocketListener.instance.addOnHandler(SocketEventConstants.userJoined, onUserJoined)
        AppSocketListener.instance.addOnHandler(SocketEventConstants.userLeft, onUserLeft)
        AppSocketListener.instance.addOnHandler(SocketEventConstants.typing, onTyping)
        AppSocketListener.instance.addOnHandler(SocketEventConstants.stopTyping, onStopTyping)
        AppSocketListener.instance.addOnHandler(SocketEventConstants.login, onLogin)

        if (mUsername != null) {
            AppSocketListener.instance.emit(SocketEventConstants.addUser, mUsername)
        }
    }

    override fun onSocketDisconnected() {}

    override fun onNewMessageReceived(username: String, message: String) {
        getActivity().runOnUiThread(object : Runnable {
            public override fun run() {
                removeTyping(username)
                addMessage(username, message)
            }
        })
    }

    companion object {
        private val REQUEST_LOGIN = 0
        private val TYPING_TIMER_LENGTH = 600
    }
}