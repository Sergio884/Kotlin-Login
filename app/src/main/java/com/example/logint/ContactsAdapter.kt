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
            view.name_contact.text=contactos.name
            view.number_contact.text=contactos.phone
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
                                    "Eliminando Contacto ${view.name_contact.text} and ${view.number_contact.text}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                db.collection("contacts-${user!!.uid}").document("${view.number_contact.text}").delete()

                                true
                            }
                            R.id.modificar_contacto -> {
                                Toast.makeText(
                                    act,
                                    "Modificando Contacto",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intentss: Intent = Intent(act,ModifyContact::class.java)
                                intentss.putExtra("name","${view.name_contact.text}")
                                intentss.putExtra("number","${view.number_contact.text}")
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