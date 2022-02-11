package com.example.logint

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_contact.*

class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)




        //****************************** Nav Bar *************************************
        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.navigation_contactos->{
                    val intentContactos= Intent(this,ContactActivity::class.java)
                    startActivity(intentContactos)
                    true
                }
                R.id.navigation_home->{
                    val intentHome= Intent(this,MainPanel::class.java)
                    startActivity(intentHome)
                    true
                }
                R.id.navigation_ajustes ->{
                    val intentAjustes = Intent(this,MainActivity::class.java)
                    startActivity(intentAjustes)
                    true
                }
                R.id.navigation_informacion ->{
                    val intentInfo = Intent(this,InfoActivity::class.java)
                    startActivity(intentInfo)
                    true
                }


                else -> false
            }
        }
    }
}