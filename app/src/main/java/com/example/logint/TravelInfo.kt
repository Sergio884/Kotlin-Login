package com.example.logint

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class TravelInfo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_info)

        val etRadioTolerancia: EditText = findViewById(R.id.et_radioTolerancia)
        val etTiempoTolerancia: EditText = findViewById(R.id.et_tiempoTolerancia)
        val tvIrARecorrido: TextView = findViewById(R.id.tv_irARecorrido)

        val radioTolerancia = intent.getStringExtra("name")
        val tiempoTolerancia = intent.getStringExtra("number")

        etRadioTolerancia.setText(radioTolerancia)
        etTiempoTolerancia.setText(tiempoTolerancia)

        tvIrARecorrido.setOnClickListener {
            val auth = Firebase.auth
            val db = FirebaseFirestore.getInstance()
            val user = auth.currentUser
            db.collection("contacts-${user!!.uid}").document("${tiempoTolerancia}").delete()
            db.collection("contacts-${user.uid}").document("${etTiempoTolerancia.text}").set(
                hashMapOf("name" to "${etRadioTolerancia.text}")
            )
            Toast.makeText(
                this,
                "Radio de Tolerancia: ${etRadioTolerancia.text} Tiempo de Tolerancia: ${etTiempoTolerancia.text}",
                Toast.LENGTH_SHORT
            ).show()
            val intentss: Intent = Intent(this, ContactActivity::class.java)
            ContextCompat.startActivity(this, intentss, null)


        }
    }
}
