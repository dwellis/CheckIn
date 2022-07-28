package com.example.checkin.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import com.example.checkin.R
import com.example.checkin.databinding.ActivityCreateAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class CreateAccount : AppCompatActivity() {

    companion object {
        private const val TAG = "CreateAccount"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityCreateAccountBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // db reference for creating account
        database = Firebase.database.reference

        // button for updating an account
        // replaces previous data with newly entered
        // TODO: Change password is implemented here instead of its own process
        binding.buttonUpdate.setOnClickListener {
            createAccount(binding.createAccountEmailInput.text.toString(), binding.createAccountPasswordInput.text.toString())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(FirebaseAuth.getInstance().currentUser == null) {
            return when(item.itemId) {
                R.id.main_menu_home -> {
                    var homeIntent = Intent(this, LoginActivity::class.java)
                    startActivity(homeIntent)
                    true
                }
                R.id.main_menu_profile -> {
                    var loginIntent = Intent(this, LoginActivity::class.java)
                    startActivity(loginIntent)
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
        else {
            return when(item.itemId) {
                R.id.main_menu_home -> {
                    var homeIntent = Intent(this, ProfileActivity::class.java)
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

    private fun createAccount(email: String, password: String) {
        // [START create_user_with_email]
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser

                    Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT)

                    val account = UserAccount(auth.currentUser?.uid.toString(),
                        binding.createAccountBirthdayInput.text.toString(),
                        binding.createAccountFirstNameInput.text.toString(),
                        binding.createAccountLastNameInput.text.toString(),
                        auth.currentUser?.email.toString(),
                        binding.createAccountAdminCheckbox.isChecked)
                    database.child("accounts").child(auth.currentUser?.uid.toString()).setValue(account).addOnCompleteListener {

                    }

                    var intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    // TODO: More information to the user for why they failed to create an account
                    Toast.makeText(baseContext, "Authentication failure",
                        Toast.LENGTH_SHORT).show()
                }
            }
        // [END create_user_with_email]
    }

    @IgnoreExtraProperties
    data class UserAccount(
        val UID : String? = null,
        val birthday: String? = null,
        val firstName: String? = null,
        val lastName: String? = null,
        val email : String? = null,
        val admin : Boolean? = null
    ) {
        // Null default values create a no-argument default constructor, which is needed
        // for deserialization from a DataSnapshot.
    }
}