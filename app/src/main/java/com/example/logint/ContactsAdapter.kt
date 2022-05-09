package com.example.logint

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat.*

import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.item_contacto.view.*

class ContactsAdapter(val contactos: kotlin.collections.MutableList<Contactos>,val act: Activity):RecyclerView.Adapter<ContactsAdapter.ContactsHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ContactsHolder(layoutInflater.inflate(R.layout.item_contacto,parent,false),act)
    }

    override fun onBindViewHolder(holder: ContactsHolder, position: Int) {
       holder.render(contactos[position])
    }

    override fun getItemCount(): Int {
       return contactos.size
    }




    class ContactsHolder(val view: View,val act: Activity):RecyclerView.ViewHolder(view){

        fun render(contactos: Contactos){
            view.location_name.text=contactos.name
            view.location.text=contactos.phone
            view.points_menu.setOnClickListener {
                PopupMenu(act, view).apply {
                    inflate(R.menu.menu_contactos)
                    setOnMenuItemClickListener {

                        when (it!!.itemId) {
                            R.id.eliminar_contacto -> {
                                val auth = Firebase.auth
                                val db = FirebaseFirestore.getInstance()
                                val user = auth.currentUser
                                Toast.makeText(
                                    act,
                                    "Eliminando Contacto ${view.location_name.text} and ${view.location.text}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                db.collection("contacts-${user!!.uid}").document("${view.location.text}").delete()

                                true
                            }
                            R.id.modificar_contacto -> {
                                Toast.makeText(
                                    act,
                                    "Modificando Contacto",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intentss: Intent = Intent(act,ModifyContact::class.java)
                                intentss.putExtra("name","${view.location_name.text}")
                                intentss.putExtra("number","${view.location.text}")
                                startActivity(act,intentss,null)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }

        }
    }


}