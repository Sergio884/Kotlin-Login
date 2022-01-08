package com.example.logint

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
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
import androidx.core.content.ContextCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.jar.Manifest


class ContactActivity : AppCompatActivity(){
    private var auth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()
    private var key = 1
    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        var nombreUsuario: TextView = findViewById(R.id.name_contact)
        var numberContact: TextView = findViewById(R.id.number_contact)
        val addContacts: ImageView = findViewById(R.id.add_contacts)




        // Launch para seleccionar a el contacto
        val contactosLauncher = registerForActivityResult(StartActivityForResult()) { activityResult ->

            if (activityResult.resultCode == RESULT_OK) {
                val data: Intent? = activityResult.data
                val cursor1: Cursor
                val cursor2: Cursor

                //get data from intent
                val uri = data!!.data

                cursor2 = contentResolver.query(uri!!, null, null, null, null)!!
                if (cursor2.moveToFirst()){
                    val user = auth.currentUser
                    val indexNombreContacto = cursor2.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val nombreContactos= cursor2.getString(indexNombreContacto)
                    val contactId = cursor2.getString(cursor2.getColumnIndex(ContactsContract.Contacts._ID))
                    cursor1 = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+contactId, null, null)!!
                    if (cursor1.moveToFirst()){
                        val indexNumberContacto = cursor1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val number = cursor1.getString(indexNumberContacto)
                        Toast.makeText(
                            this@ContactActivity,
                            "Nombre: ${nombreContactos} Numero: ${number}",
                            Toast.LENGTH_SHORT
                        ).show()
                        db.collection("contacts").document("${number}").set(
                            hashMapOf("name" to nombreContactos,"emailUser" to "pepito@gmail.com")
                        )
                    }


                    cursor1.close()
                    cursor2.close()
                }
            }
            else{
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
        addContacts.setOnClickListener {
            if(checkContactPermission()){
               // val intentAdd = Intent (Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                val intentAdds = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                //startActivityForResult(intentAdds, 2)
                contactosLauncher.launch(intentAdds)
            }

        }

    }
    //Verificar si los permisos de READ_CONTACTS estan activados
    private fun checkContactPermission(): Boolean{
        return  ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    //Menu inflater
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

    /*
    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //handle intent results || calls when user from Intent (Contact Pick) picks or cancels pick contact
        if (resultCode == RESULT_OK){

            //calls when user click a contact from contacts (intent) list
            if (requestCode == 2){

                val cursor1: Cursor
                val cursor2: Cursor

                //get data from intent
                val uri = data!!.data

                cursor2 = contentResolver.query(uri!!, null, null, null, null)!!
                if (cursor2.moveToFirst()){

                    val indexNombreContacto = cursor2.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val nombreContactos= cursor2.getString(indexNombreContacto)
                    val contactId = cursor2.getString(cursor2.getColumnIndex(ContactsContract.Contacts._ID))
                    cursor1 = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+contactId, null, null)!!
                    if (cursor1.moveToFirst()){
                        val indexNumberContacto = cursor1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val number = cursor1.getString(indexNumberContacto)
                        Toast.makeText(
                            this@ContactActivity,
                            "Nombre: ${nombreContactos} Numero: ${number}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }


                    cursor1.close()
                    cursor2.close()
                }
            }

        }
        else{
            //cancelled picking contact
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
        }
    } */



}