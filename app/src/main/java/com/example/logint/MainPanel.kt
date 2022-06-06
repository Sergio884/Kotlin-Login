package com.example.logint
import android.Manifest
import menu_bottom.ContactsFragment
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.logint.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
//import com.example.logint.databinding.ActivityMainPanelBinding
import kotlinx.android.synthetic.main.activity_main_panel.*


import android.content.Context
import android.telephony.SmsManager
import androidx.activity.result.contract.ActivityResultContracts
//import com.marjasoft.ejgps.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.android.synthetic.main.activity_main_panel.bottomNavigationView
import java.lang.NullPointerException


class MainPanel : AppCompatActivity(){
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
        var notificationGoal : Boolean = false


        if (intent != null) {
            notificationGoal = intent.getBooleanExtra("ruteGooal",false)

        }
//        Comprobamos notificacion
        if (notificationGoal == true){
            goalNotification()

        }

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
            if(isMyServiceRunning(OnRoute::class.java)==true){
                try{
                    val intentKill = Intent(this,OnRoute::class.java)
                    stopService(intentKill)
                }catch (e:NullPointerException){
                    e.printStackTrace()
                }
            }
            if(isMyServiceRunning(SecurityZone::class.java)==true){
                try{
                    val intentKill = Intent(this,SecurityZone::class.java)
                    stopService(intentKill)
                }catch (e:NullPointerException){
                    e.printStackTrace()
                }

            }
            if(isMyServiceRunning(RecordRoute::class.java)==true){
                try{
                    val intentKill = Intent(this,RecordRoute::class.java)
                    stopService(intentKill)
                }catch (e:NullPointerException){
                    e.printStackTrace()
                }

            }



        }
        if(isMyServiceRunning(RecordRoute::class.java)==true){
            val intentRecord = Intent(this,RecordRouteActivity::class.java)
            intentRecord.putExtra("runing",true)
            startActivity(intentRecord)
        }
        if(isMyServiceRunning(OnRoute::class.java)==true){
            val intentOnRoute = Intent(this,OnRouteActivity::class.java)
            //intentOnRoute.putParcelableArrayListExtra("path",GlobalClass.polyLine)
            startActivity(intentOnRoute)
        }
        if(isMyServiceRunning(SecurityZone::class.java)==true){
            val intentZone = Intent(this,SecurityZoneActivity::class.java)
            startActivity(intentZone)


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

        tv_zona_segura.setOnClickListener {
            val intentZone = Intent(this,SecurityZoneActivity::class.java)

            startActivity(intentZone)

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


    fun goalNotification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChanel()
        }
        val notificationBuilder = NotificationCompat.Builder(this,"Tracking")//NotificationCompat.Builder(Intent(this,MainPanel::class.java),"Tracking")
            //.setAutoCancel(false)
            //.setOngoing(true)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle("¡ Llegamos a tu destino !")
            .setContentText("Recuerda siempre llevar SafeSOS con tigo")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(1,notificationBuilder)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChanel(){
        val chanel = NotificationChannel(
            "Tracking",
            "Tracking Route",
            NotificationManager.IMPORTANCE_HIGH
        )
        //val canal = notificationManager.createNotificationChannel(chanel)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(chanel)
    }






}

