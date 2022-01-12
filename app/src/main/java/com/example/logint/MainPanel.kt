package com.example.logint
import menu_bottom.ContactsFragment
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.logint.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
//import com.example.logint.databinding.ActivityMainPanelBinding
import kotlinx.android.synthetic.main.activity_main_panel.*

class MainPanel : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_panel)

        auth = Firebase.auth
        val user = auth.currentUser
        val profile: ImageView= findViewById(R.id.profilePhoto)

        Glide
            .with(this)
            .load(user!!.photoUrl)
            .centerCrop()
            .placeholder(R.drawable.profile_photo)
            .into(profile)

        var tv_fijarDestino:TextView=findViewById(R.id.tv_fijarDestino)


        var contactsFragment = ContactsFragment()
        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.navigation_contactos->{
                    val intentContactos= Intent(this,ContactActivity::class.java)
                    startActivity(intentContactos)
                    true
                }

                else -> false
            }
        }

        tv_fijarDestino.setOnClickListener {
            val intent = Intent(this,MapsActivity::class.java)
            startActivity(intent)
        }



        profile.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }



    }

    //Navegar entre Fragmentos
    private fun setCurrentFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().apply{
            replace(R.id.container_view,fragment)
            commit()
        }
    }

}