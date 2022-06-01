package com.example.logint

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.logint.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_contact.*

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private val fileResult = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        binding.signOutImageView.setOnClickListener {
            signOut()
        }

        binding.updateProfileAppCompatButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            updateProfile(name)
        }

        binding.profileImageView.setOnClickListener {
            fileManager()
        }

        binding.deleteAccountTextView.setOnClickListener {
            val intent = Intent(this, DeleteAccountActivity::class.java)
            startActivity(intent)
        }

        binding.updatePasswordTextView.setOnClickListener {
            val intent = Intent(this, UpdatePasswordActivity::class.java)
            startActivity(intent)
        }

        updateUI()

        bottomNavigationView.selectedItemId = R.id.navigation_ajustes
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
    }

    private fun fileManager() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, fileResult)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == fileResult) {
            if (resultCode == RESULT_OK && data != null) {
                val uri = data.data
                uri?.let { imageUpload(it) }
            }
        }
    }

    private fun imageUpload(mUri: Uri) {
        val user = auth.currentUser
        val folder: StorageReference = FirebaseStorage.getInstance().reference.child("Users")
        val fileName: StorageReference = folder.child("img" + user!!.uid)

        fileName.putFile(mUri).addOnSuccessListener {
            fileName.downloadUrl.addOnSuccessListener { uri ->

                val profileUpdates = userProfileChangeRequest {
                    photoUri = Uri.parse(uri.toString())
                }

                user.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this, "Se realizaron los cambios correctamente.",
                                Toast.LENGTH_SHORT
                            ).show()
                            updateUI()
                        }
                    }
            }
        }.addOnFailureListener {
            Log.i("TAG", "file upload error")
        }
    }


    private fun updateProfile(name: String) {
        val user = auth.currentUser
        val profileUpdates = userProfileChangeRequest {
            displayName = name
        }

        user!!.updateProfile((profileUpdates)).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    baseContext,
                    "Se actualizó la información con exito",
                    Toast.LENGTH_SHORT
                ).show()
                updateUI()
            }
        }
    }

    private fun signOut() {
        Firebase.auth.signOut()
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
    }

    private fun updateUI() {
        val user = auth.currentUser

        if (user != null) {
            binding.emailTextView.text = user.email
            binding.nameTextView.text = user.displayName
            binding.nameEditText.setText(user.displayName)
            Glide
                .with(this)
                .load(user.photoUrl)
                .centerCrop()
                .placeholder(R.drawable.profile_photo)
                .into(binding.profileImageView)
            Glide
                .with(this)
                .load(user.photoUrl)
                .centerCrop()
                .placeholder(R.drawable.profile_photo)
                .into(binding.bgProfileImageView)
        }
    }
}