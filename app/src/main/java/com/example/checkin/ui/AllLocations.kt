package com.example.checkin.ui

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.marginTop
import com.example.checkin.R
import com.example.checkin.databinding.ActivityAllLocationsBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AllLocations : AppCompatActivity() {

    lateinit var binding : ActivityAllLocationsBinding
    private lateinit var database: DatabaseReference
    private lateinit var accounts : DatabaseReference
    private lateinit var account : DatabaseReference
    private lateinit var locations : DatabaseReference

    companion object {
        private const val TAG = "AllLocations"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAllLocationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // database references
        database = Firebase.database.reference
        accounts = Firebase.database.reference.child("accounts")
        account = accounts.child(Firebase.auth.currentUser?.uid.toString())
        locations = database.child("locations")

        // for each location add a view and update if they complete the form
        locations.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.hasChildren()) {
                    snapshot.children.forEach {
                        // manually set for now
                        // TODO: Move into layout
                        val tvTitle = TextView(applicationContext)
                        tvTitle.text = it.child("title").value.toString()
                        tvTitle.setPadding(0, 110, 0, 0)
                        tvTitle.textSize = 20f

                        val tvDesc = TextView(applicationContext)
                        tvDesc.text = it.child("desc").value.toString()
                        tvDesc.setPadding(0, 20, 0, 30)

                        val tvAddress = TextView(applicationContext)
                        tvAddress.text = it.child("address").value.toString()
                        tvAddress.setPadding(0, 20, 0, 0)

                        val id = it.key.toString()
                        val but = Button(applicationContext)
                        but.text = "Subscribe"
                        but.setOnClickListener {
                            account.child("subscribed").setValue(id)
                        }

                        binding.allLocationsLl.addView(tvTitle)
                        binding.allLocationsLl.addView(tvAddress)
                        binding.allLocationsLl.addView(tvDesc)
                        binding.allLocationsLl.addView(but)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                val noneTv = TextView(applicationContext)
                noneTv.text = "No current locations"
                binding.allLocationsLl.addView(noneTv)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
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