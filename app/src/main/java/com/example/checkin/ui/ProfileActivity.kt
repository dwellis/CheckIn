package com.example.checkin.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import com.example.checkin.R
import com.example.checkin.databinding.ActivityProfileBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var database: DatabaseReference

    private lateinit var admin : String

    companion object {
        private const val TAG = "ProfileActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Firebase.database.reference

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // setting admin so it won't throw null error
        admin = ""


        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.child("accounts").child(Firebase.auth.currentUser?.uid.toString()).child("firstName").value
                val welcomeText = "Welcome, " + snapshot.child("accounts").child(Firebase.auth.currentUser?.uid.toString()).child("firstName").value.toString() + "!"
                val fullName = snapshot.child("accounts").child(Firebase.auth.currentUser?.uid.toString()).child("firstName").value.toString() + " " + snapshot.child("accounts").child(Firebase.auth.currentUser?.uid.toString()).child("lastName").value.toString()
                val email = snapshot.child("accounts").child(Firebase.auth.currentUser?.uid.toString()).child("email").value.toString()
                val birthday = snapshot.child("accounts").child(Firebase.auth.currentUser?.uid.toString()).child("birthday").value.toString()
                admin = snapshot.child("accounts").child(Firebase.auth.currentUser?.uid.toString()).child("admin").value.toString()



                binding.profileWelcome.text = welcomeText
                binding.profileNameText.text = fullName
                binding.profileEmailText.text = email
                binding.profileBirthdayText.text = birthday

                // if admin enable admin button
                if(admin.toBoolean()) {
                    binding.profileButtonOption.visibility = android.view.View.VISIBLE
                    binding.profileButtonOption.isClickable = true
                    binding.profileButtonOption.text = "Manage"
                    binding.profileButtonOption.setOnClickListener {
                        goToAdmin()
                    }
                }

            }


            override fun onCancelled(error: DatabaseError) {

            }
        })

        // check if admin and enable button






        binding.profileButtonDelete.setOnClickListener {
            deleteUser()
        }

        binding.profileButtonSignOut.setOnClickListener {
            signOut()
        }

        binding.profileButtonUpdate.setOnClickListener {
            val intent = Intent(this, UpdateAccount::class.java)
            startActivity(intent)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(admin.toBoolean()) {
            return when (item.itemId) {
                R.id.main_menu_home -> {
                    var homeIntent = Intent(this, RegistrarLanding::class.java)
                    startActivity(homeIntent)
                    true
                }
                R.id.main_menu_profile -> {
                    var profileIntent = Intent(this, ProfileActivity::class.java)
                    startActivity(profileIntent)
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        } else {
            return when (item.itemId) {
                R.id.main_menu_home -> {
                    var homeIntent = Intent(this, UserLanding::class.java)
                    startActivity(homeIntent)
                    true
                }
                R.id.main_menu_profile -> {
                    var profileIntent = Intent(this, ProfileActivity::class.java)
                    startActivity(profileIntent)
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
    }

    private fun resetPassword() {
        if(Firebase.auth.currentUser != null) {
            val emailAddress = Firebase.auth.currentUser!!.email.toString()
            Firebase.auth.sendPasswordResetEmail(emailAddress).addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    Toast.makeText(this,"Password reset email sent", Toast.LENGTH_SHORT )
                }
            }
        }
    }

    private fun deleteUser() {
        val user = Firebase.auth.currentUser!!

        user.delete().addOnCompleteListener { task ->
            if(task.isSuccessful) {
                database.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.child("accounts").child(Firebase.auth.currentUser?.uid.toString()).ref.removeValue()
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

                Toast.makeText(this,"User account deleted", Toast.LENGTH_SHORT )
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun signOut() {
        Firebase.auth.signOut()
        Toast.makeText(this, "User signed out", Toast.LENGTH_SHORT)
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
    }

    private fun goToAdmin() {
        var intent = Intent(this, ManageActivity::class.java)
        startActivity(intent)
    }


}