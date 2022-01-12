package com.example.logint

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.item_contacto.view.*

class ModifyContact : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_contact)

        val etNameContact : EditText = findViewById(R.id.et_nameContact)
        val etNumberContact :EditText = findViewById(R.id.et_numberContact)
        val tvGuardarContact: TextView = findViewById(R.id.tv_guardarContacto)

        val nameContact = intent.getStringExtra("name")
        val numberContact = intent.getStringExtra("number")

        etNameContact.setText(nameContact)
        etNumberContact.setText(numberContact)

        tvGuardarContact.setOnClickListener {
            val auth = Firebase.auth
            val db = FirebaseFirestore.getInstance()
            val user = auth.currentUser
            db.collection("contacts-${user!!.uid}").document("${numberContact}").delete()
            db.collection("contacts-${user.uid}").document("${etNumberContact.text}").set(
                hashMapOf("name" to "${etNameContact.text}")
            )
            Toast.makeText(
                this,
                "Nombre:${etNameContact.text} Numero: ${etNameContact.text}",
                Toast.LENGTH_SHORT
            ).show()
            val intentss: Intent = Intent(this,ContactActivity::class.java)
            ContextCompat.startActivity(this, intentss, null)


        }


    }
}