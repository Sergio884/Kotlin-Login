package com.example.logint

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.*
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.*

class ContactActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        var nombreUsuario: TextView = findViewById(R.id.name_contact)
        var numberContact: TextView = findViewById(R.id.number_contact)
        var addContacts: ImageView = findViewById(R.id.add_contacts)
        val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){
                isGranted: Boolean ->
            if (isGranted){

                Log.i("Bien","Bien")
            }else{
                Log.i("Mal","Mal")
            }
        }
        val contactosLauncher = registerForActivityResult(StartActivityForResult()) { activityResult ->

            if (activityResult.resultCode == RESULT_OK) {
                val data: Intent? = activityResult.data
                val contactUri: Uri = data?.data!!
                val projection: Array<String> = arrayOf(CommonDataKinds.Phone.CONTENT_TYPE)
                contentResolver.query(contactUri, projection, null, null, null).use { cursor ->
                    // If the cursor returned is valid, get the phone number
                    if (cursor!!.moveToFirst()) {
                        val numberIndex = cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER)
                        val number = cursor.getString(numberIndex)
                        Toast.makeText(
                            this@ContactActivity,
                            number,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        addContacts.setOnClickListener {

            val intentAdd = Intent(Intent.ACTION_PICK).apply {
                type = CommonDataKinds.Phone.CONTENT_TYPE
            }
            if(intentAdd.resolveActivity(packageManager)!=null){
                Toast.makeText(
                    this@ContactActivity,
                    "SI DIOOOO",
                    Toast.LENGTH_SHORT
                ).show()

                contactosLauncher.launch(intent)
            }

            //.apply {type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE }

        }

    }


    fun showPopUp(v: View) {
        PopupMenu(this, v).apply {
            inflate(R.menu.menu_contactos)
            setOnMenuItemClickListener {
                when (it!!.itemId) {
                    R.id.eliminar_contacto -> {
                        Toast.makeText(
                            this@ContactActivity,
                            "Eliminando Contacto",
                            Toast.LENGTH_SHORT
                        ).show()
                        true
                    }
                    R.id.modificar_contacto -> {
                        Toast.makeText(
                            this@ContactActivity,
                            "Modificando Contacto",
                            Toast.LENGTH_SHORT
                        ).show()
                        true
                    }
                    else -> false
                }
            }
        }.show()
    }



}