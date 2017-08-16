package br.com.andrecouto.kotlin.chatapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnSample.setOnClickListener { goToChatLibrary(edtName.text.toString())}
    }

    fun goToChatLibrary(nickname : String) {
        val mIntent = Intent(this, br.com.andrecouto.kotlin.chatlib.activity.ChatMainActivity::class.java)
        mIntent.putExtra("nickname", nickname)
        startActivity(mIntent)
    }
}
