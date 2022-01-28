package com.example.logint

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.airbnb.lottie.LottieAnimationView
import kotlinx.android.synthetic.main.activity_main_panel.*
import kotlinx.android.synthetic.main.activity_main_panel.bottomNavigationView
import kotlinx.android.synthetic.main.activity_record_route.*

class RecordRouteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_route)
        var imageBool = false
        recordImageView.setOnClickListener {
            imageBool = recordAnimation(recordImageView,R.raw.routefinder,imageBool)
            et_nombreRuta.isFocusable = false
            et_nombreRuta.isEnabled = false
            et_nombreRuta.isCursorVisible = false
            et_nombreRuta.setBackgroundResource(R.drawable.ic_field_none)
            et_nombreRuta.setOnKeyListener(null)
        }






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
}