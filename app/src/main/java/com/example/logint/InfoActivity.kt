package com.example.logint

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_contact.*
import kotlinx.android.synthetic.main.activity_contact.bottomNavigationView
import kotlinx.android.synthetic.main.activity_main_panel.*

class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)


        bottomNavigationView.selectedItemId = R.id.navigation_informacion

        val tvInfoT: TextView = findViewById(R.id.textViewInfoT)
        tvInfoT.append("¿Cómo funciona el envío de alertas?\nLas alertas se pueden mandar presionando el" +
                "botón de SOS en el inicio de la aplicación, en caso de sentirse en peligro, el usuario" +
                "presionara el botón y este se pondrá en verde indicando que se mandaron las alertas. Por" +
                "otro lado, si el usuario esta recorriendo una ruta y sale del radio de tolerancia una " +
                "cantidad de tiempo mayor a la que ingreso antes de iniciar el recorrido se mandaran alertas." +
                "\n\n")

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