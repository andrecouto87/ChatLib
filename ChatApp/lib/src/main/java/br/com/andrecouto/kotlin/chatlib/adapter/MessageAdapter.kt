package br.com.andrecouto.kotlin.chatlib.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import br.com.andrecouto.kotlin.chatlib.R
import br.com.andrecouto.kotlin.chatlib.entity.Message
import kotlinx.android.synthetic.main.item_action.view.*
import kotlinx.android.synthetic.main.item_log.view.*
import kotlinx.android.synthetic.main.item_message.view.*

class MessageAdapter(
        context: Context,
        messages: List<Message>) :
        RecyclerView.Adapter<MessageAdapter.MessagesViewHolder>() {

    private var mMessages: List<Message>
    private var mUsernameColors: IntArray

    override fun getItemCount(): Int {
        return mMessages.size
    }

    init {
        mMessages = messages
        mUsernameColors = context.getResources().getIntArray(R.array.username_colors)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {
        var layout = -1
        when (viewType) {
            Message.TYPE_MESSAGE -> layout = R.layout.item_message
            Message.TYPE_LOG -> layout = R.layout.item_log
            Message.TYPE_ACTION -> layout = R.layout.item_action
        }
        val v = LayoutInflater
                .from(parent.getContext())
                .inflate(layout, parent, false)
        return MessagesViewHolder(v)
    }

    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
        val msg = mMessages.get(position)
        val view = holder.itemView
        with(view) {
            setMessage(message_item, message_log, msg.message)
            setUserName(username_action, username_message, msg.username)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return mMessages.get(position).type
    }

    fun setMessage(txtuser: TextView?, txtlog: TextView?, message: String) {
        if (null == txtuser || null == txtlog) return

        if (null != txtuser) {
            txtuser.setText(message)
            txtuser.setTextColor(getUsernameColor(message))
        } else {
            txtlog.setText(message)
            txtlog.setTextColor(getUsernameColor(message))
        }
    }

    fun setUserName(txtaction: TextView?, txtmessage: TextView?, username: String) {
        if (null == txtaction || null == txtmessage) return

        if(null != txtaction) {
            txtaction.setText(username)
        } else {
            txtmessage.setText(username)
        }
    }

    private fun getUsernameColor(username: String): Int {
        var hash = 7
        var i = 0
        val len = username.length
        while (i < len) {
            hash = username.codePointAt(i) + (hash shl 5) - hash
            i++
        }
        val index = Math.abs(hash % mUsernameColors.size)
        return mUsernameColors[index]
    }

    class MessagesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}
}