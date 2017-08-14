package br.com.andrecouto.kotlin.chatapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var edt = findViewById(R.id.edtName) as EditText
        findViewById(R.id.btnSample).setOnClickListener { goToChatLibrary(edt.text.toString())}
    }

    fun goToChatLibrary(nickname : String) {
        val mIntent = Intent(this, br.com.andrecouto.kotlin.chatlib.activity.ChatMainActivity::class.java)
        mIntent.putExtra("nickname", nickname)
        startActivity(mIntent)
    }


}
