package com.example.checkin.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.example.checkin.R
import com.example.checkin.databinding.ActivityUpdateAccountBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class UpdateAccount : AppCompatActivity() {

    private lateinit var binding : ActivityUpdateAccountBinding
    private lateinit var database: DatabaseReference
    private lateinit var accounts : DatabaseReference
    private lateinit var account : DatabaseReference

    companion object {
        private const val TAG = "UpdateAccount"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        database = Firebase.database.reference
        accounts = Firebase.database.reference.child("accounts")
        account = accounts.child(Firebase.auth.currentUser?.uid.toString())


        binding = ActivityUpdateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonUpdate.setOnClickListener {

            Firebase.auth.currentUser?.updateEmail(binding.createAccountEmailInput.text.toString())
            Firebase.auth.currentUser?.updatePassword(binding.createAccountPasswordInput.text.toString())

            account.child("firstName").setValue(binding.createAccountFirstNameInput.text.toString())
            account.child("lastName").setValue(binding.createAccountLastNameInput.text.toString())
            account.child("birthday").setValue(binding.createAccountBirthdayInput.text.toString())
            account.child("admin").setValue(binding.createAccountAdminCheckbox.isChecked)
            account.child("email").setValue(binding.createAccountEmailInput.text.toString())

            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // update inputs to current values
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.child("accounts").child(Firebase.auth.currentUser?.uid.toString()).child("firstName").value

                binding.createAccountFirstNameInput.setText(snapshot.child("accounts").child(Firebase.auth.currentUser?.uid.toString()).child("firstName").value.toString())
                binding.createAccountLastNameInput.setText(snapshot.child("accounts").child(Firebase.auth.currentUser?.uid.toString()).child("lastName").value.toString())
                binding.createAccountBirthdayInput.setText(snapshot.child("accounts").child(Firebase.auth.currentUser?.uid.toString()).child("birthday").value.toString())
                binding.createAccountEmailInput.setText(snapshot.child("accounts").child(Firebase.auth.currentUser?.uid.toString()).child("email").value.toString())
                binding.createAccountAdminCheckbox.isChecked = snapshot.child("accounts").child(Firebase.auth.currentUser?.uid.toString()).child("admin").value.toString().toBoolean()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
            return when(item.itemId) {
                R.id.main_menu_home -> {
                    var homeIntent = Intent(this, ProfileActivity::class.java)
                    startActivity(homeIntent)
                    true
                }
                R.id.main_menu_profile -> {
                    var loginIntent = Intent(this, ProfileActivity::class.java)
                    startActivity(loginIntent)
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
    }
}