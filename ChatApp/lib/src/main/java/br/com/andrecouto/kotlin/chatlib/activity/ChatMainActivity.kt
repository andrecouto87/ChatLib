package br.com.andrecouto.kotlin.chatlib.activity

import android.os.Bundle
import br.com.andrecouto.kotlin.chatlib.R
import br.com.andrecouto.kotlin.chatlib.fragment.ChatMainFragment

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

}