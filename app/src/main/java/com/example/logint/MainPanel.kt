package com.example.logint
import android.Manifest
import menu_bottom.ContactsFragment
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.logint.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
//import com.example.logint.databinding.ActivityMainPanelBinding
import kotlinx.android.synthetic.main.activity_main_panel.*


import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.isLocationEnabled
//import com.marjasoft.ejgps.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception
import java.util.*
import android.app.ActivityManager
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_contact.*
import kotlinx.android.synthetic.main.activity_main_panel.bottomNavigationView


class MainPanel : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private var globalClass =  GlobalClass()


    val PERMISSION_ID = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_panel)

        GlobalClass.course = 1
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

        tv_fijarDestino.setOnClickListener {
            val intent = Intent(this,TravelSelectionActivity::class.java)
            startActivity(intent)
        }



        profile.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        if( isMyServiceRunning(SendLocation::class.java) == true){
            imageButtonSOS.setImageResource(R.drawable.ic_sos_green)

        }
        if(isMyServiceRunning(RecordRoute::class.java)==true){
            val intentRecord = Intent(this,RecordRouteActivity::class.java)
            intentRecord.putExtra("runing",true)
            startActivity(intentRecord)
        }


        imageButtonSOS.setOnClickListener {

            if(isMyServiceRunning(SendLocation::class.java) == false){
                imageButtonSOS.setImageResource(R.drawable.ic_sos_green)
                locationPermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                locationPermission.launch(Manifest.permission.SEND_SMS)
                locationPermission.launch(Manifest.permission.READ_PHONE_STATE)
                locationPermission.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                sendSMS()
                val intentSOS = Intent(this,SendLocation::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intentSOS)

                }else{
                    startService(intentSOS)
                }
                //startService(intentSOS)
                //globalClass.setBandera(1)

            }
            else{
                imageButtonSOS.setImageResource(R.drawable.ic_sos)
                val intentSOS = Intent(this,SendLocation::class.java)
                stopService(intentSOS)
                //globalClass.setBandera(0)
            }
            //val permissions = arrayOf("${Manifest.permission.ACCESS_COARSE_LOCATION}","${Manifest.permission.ACCESS_FINE_LOCATION}",
            //"${Manifest.permission.SEND_SMS}","${Manifest.permission.READ_PHONE_STATE}","${Manifest.permission.ACCESS_BACKGROUND_LOCATION}")
        }

        tv_grabarRuta.setOnClickListener {

            if(isMyServiceRunning(RecordRoute::class.java)==true){
                val intentRecord = Intent(this,RecordRouteActivity::class.java)
                intentRecord.putExtra("runing",true)
                startActivity(intentRecord)
            }
            else{
                val intent = Intent(this,RecordRouteActivity::class.java)
                startActivity(intent)
            }
        }
    }


        //Navegar entre Fragmentos
    private fun setCurrentFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().apply{
            replace(R.id.container_view,fragment)
            commit()
        }
    }

    //pedir permisos de Ubicacion
    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted ->
        if(isGranted) println("acepto") //Toast.makeText(this, "acepto", Toast.LENGTH_SHORT).show()
        else println("no acepto")//Toast.makeText(this, "No acepto", Toast.LENGTH_SHORT).show()
    }

    private fun sendSMS(){
        val auth = Firebase.auth
        val db = FirebaseFirestore.getInstance()
        val user = auth.currentUser
        val docs = db.collection("contacts-${user!!.uid}")
        val uid :String = user!!.uid
        val informacion = "¡ALERTA DE EMERGENCIA!\n "+user.displayName.toString()+" se encuentra en peligro te compartimos un link con el cual podras acceder a su ubicacion".replace("ñ","n").replace("á","a").replace("é","e").replace("í","i").replace("ó","o")
        val url = "safesos.online/mapa.php?u=${uid}&n="+user.displayName.toString()
        print(url)
        docs.get().addOnSuccessListener { documents ->
            for(document in documents){
                    //Log.d("contacto: ", "${document.id} => ${document.data}")
                    println("${document.id.toString()} ${document.data.toString()}")
                    val sms = SmsManager.getDefault()
                    sms.sendTextMessage(document.id.toString(),null,informacion,null,null)
                    sms.sendTextMessage(document.id.toString(),null,url,null,null)
                    /*val sendWhats = Intent(Intent.ACTION_SEND)
                    sendWhats.type = "text/plain"
                    sendWhats.setPackage("com.whatsapp")
                    sendWhats.putExtra("jid", "525587355557" + "@s.whatsapp.net");
                    sendWhats.putExtra(Intent.EXTRA_TEXT,"Prueba Api");
                    startActivity(sendWhats);*/


            }
        }

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

