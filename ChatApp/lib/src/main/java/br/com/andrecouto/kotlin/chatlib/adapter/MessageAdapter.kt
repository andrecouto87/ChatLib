package br.com.andrecouto.kotlin.chatlib.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import br.com.andrecouto.kotlin.chatlib.R
import br.com.andrecouto.kotlin.chatlib.entity.Message


class MessageAdapter(context: Context, messages: List<Message>) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    private var mMessages: List<Message>
    private var mUsernameColors: IntArray

    override fun getItemCount(): Int {
        return mMessages.size
    }

    init {
        mMessages = messages
        mUsernameColors = context.getResources().getIntArray(R.array.username_colors)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var layout = -1
        when (viewType) {
            Message.TYPE_MESSAGE -> layout = R.layout.item_message
            Message.TYPE_LOG -> layout = R.layout.item_log
            Message.TYPE_ACTION -> layout = R.layout.item_action
        }
        val v = LayoutInflater
                .from(parent.getContext())
                .inflate(layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val message = mMessages.get(position)
        viewHolder.setMessage(message.message)
        viewHolder.setUsername(message.username)
    }

    override fun getItemViewType(position: Int): Int {
        return mMessages.get(position).type
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mUsernameView: TextView
        private val mMessageView: TextView

        init {
            mUsernameView = itemView.findViewById(R.id.username) as TextView
            mMessageView = itemView.findViewById(R.id.message) as TextView
        }

        fun setUsername(username: String) {
            if (null == mUsernameView) return
            mUsernameView.setText(username)
            mUsernameView.setTextColor(getUsernameColor(username))
        }

        fun setMessage(message: String) {
            if (null == mMessageView) return
            mMessageView.setText(message)
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
    }
}