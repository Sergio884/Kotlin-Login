package com.example.logint

import android.Manifest
import android.app.ActivityManager
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import com.airbnb.lottie.LottieAnimationView
import kotlinx.android.synthetic.main.activity_contact.*
import kotlinx.android.synthetic.main.activity_main_panel.*
import kotlinx.android.synthetic.main.activity_main_panel.bottomNavigationView
import kotlinx.android.synthetic.main.activity_record_route.*

class RecordRouteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_route)
        var imageBool = false

        if(isMyServiceRunning(RecordRoute::class.java)==true){
            imageBool = recordAnimation(recordImageView,R.raw.routefinder,imageBool)
        }

        recordImageView.setOnClickListener {

            if(isMyServiceRunning(RecordRoute::class.java)==false){
                imageBool = recordAnimation(recordImageView,R.raw.routefinder,imageBool)
                et_nombreRuta.isFocusable = false
                et_nombreRuta.isEnabled = false
                et_nombreRuta.isCursorVisible = false
                et_nombreRuta.setBackgroundResource(R.drawable.ic_field_none)
                et_nombreRuta.setOnKeyListener(null)

                locationPermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                locationPermission.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                val intentRecord = Intent(this,RecordRoute::class.java)
                intentRecord.putExtra("nameRoute", et_nombreRuta.text.toString())
                startService(intentRecord)

            }else{
                imageBool = recordAnimation(recordImageView,R.raw.routefinder,imageBool)
                val intentRecord = Intent(this,RecordRoute::class.java)
                stopService(intentRecord)

            }


        }






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

    private fun recordAnimation(imageView: LottieAnimationView,animation: Int,image: Boolean):Boolean{
        if(!image){
            imageView.setAnimation(animation)
            imageView.repeatCount = 99999999
            imageView.scale = 500.0F
            imageView.playAnimation()

        }
        else{
            imageView.setImageResource(R.drawable.ic_record2)

        }
        return !image
    }

    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
        if(isGranted) println("acepto") //Toast.makeText(this, "acepto", Toast.LENGTH_SHORT).show()
        else println("no acepto")//Toast.makeText(this, "No acepto", Toast.LENGTH_SHORT).show()
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager

        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}