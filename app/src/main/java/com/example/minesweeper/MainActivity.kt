package com.example.minesweeper

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        var id = 0
        fun setId(n:Int){
            id = n
        }
        startButton.setOnClickListener{
            if(easyButton.isChecked || mediumButton.isChecked||hardButton.isChecked){
                when(difficultychoose.checkedRadioButtonId){
                    easyButton.id -> setId(1)
                    mediumButton.id -> setId(2)
                    hardButton.id -> setId(3)
                }
                val intent = Intent(this,GameActivity::class.java).apply{
                    putExtra("id",id)
                }
                finish()
                startActivity(intent)
            }else{
                Toast.makeText(this,"Choose difficulty",Toast.LENGTH_SHORT).show()
            }
        }
    }
}