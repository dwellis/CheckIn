package com.example.checkin.ui


import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.checkin.BuildConfig
import com.example.checkin.R
import com.example.checkin.databinding.ActivityManageBinding
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.File
import java.io.FileInputStream
import java.util.*


class ManageActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityManageBinding
    private lateinit var database: DatabaseReference
    private lateinit var accounts: DatabaseReference
    private lateinit var account: DatabaseReference

    lateinit var id : String
    lateinit var name : String
    lateinit var address : String
    lateinit var lat : String
    lateinit var lng : String

    companion object {
        private const val TAG = "ManageActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        binding = ActivityManageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = Firebase.database.reference
        accounts = Firebase.database.reference.child("accounts")
        account = accounts.child(Firebase.auth.currentUser?.uid.toString())

        // gets API key from local.properties
        val apiKey = BuildConfig.MAPS_API_KEY

        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        // Create a new Places client instance.
        val placesClient = Places.createClient(this)

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS
        ))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                id = place.id.toString()
                name = place.name.toString()
                address = place.address.toString()
                lat = place.latLng?.latitude.toString()
                lng = place.latLng?.longitude.toString()
            }
            override fun onError(status: Status) {

            }
        })

        binding.manageButtonAddCheckIn.setOnClickListener {
            val title = binding.autoCompleteTextView.text.toString()
            val desc = binding.autoCompleteTextView2.text.toString()

            val location = Location( id, name, address, lat, lng, title, desc, true)

            database.child("locations").child(auth.uid.toString()).setValue(location)
        }

        // listener for current location
        database.child("locations").child(auth.uid.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.manageTitleTv.text = snapshot.child("title").value.toString()
                binding.manageDescriptionTv.text = snapshot.child("desc").value.toString()
                binding.manageAddressTv.text = snapshot.child("address").value.toString()
                binding.manageActiveCheckBox.isChecked = snapshot.child("active").value.toString().toBoolean()
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu,menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
            return when(item.itemId) {
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
    }

    // class for creating location object in database
    @IgnoreExtraProperties
    data class Location(
        val ID : String? = null,
        val name: String? = null,
        val address : String? = null,
        val lat: String? = null,
        val lng: String? = null,
        val title: String? = null,
        val desc: String? = null,
        val active : Boolean? = null
    ) {
        // Null default values create a no-argument default constructor, which is needed
        // for deserialization from a DataSnapshot.
    }
}