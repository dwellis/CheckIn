package com.example.checkin.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.checkin.BuildConfig
import com.example.checkin.R
import com.example.checkin.createChannel
import com.example.checkin.databinding.ActivityUserLandingBinding
import com.example.checkin.utils.GeofenceBroadcastReceiver
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@RequiresApi(Build.VERSION_CODES.S)
class UserLanding : AppCompatActivity() {

    private lateinit var auth :FirebaseAuth
    private lateinit var binding :ActivityUserLandingBinding

    // db references
    private lateinit var database :DatabaseReference
    private lateinit var accounts :DatabaseReference
    private lateinit var account :DatabaseReference
    private lateinit var locations :DatabaseReference

    // set up for geofencing
    lateinit var geofencingClient :GeofencingClient

    // for easy access to changed values
    lateinit var subscribedID :String
    lateinit var lat :String
    lateinit var lng :String

    // creates the pending intent for geofence transitions handled by the broadcast receiver
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserLandingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        // get database references
        database = Firebase.database.reference
        accounts = Firebase.database.reference.child("accounts")
        account = accounts.child(Firebase.auth.currentUser?.uid.toString())
        locations = database.child("locations")

        // create geofencing client
        geofencingClient = LocationServices.getGeofencingClient(this)

        // create channel for notifications
        createChannel(this )

        // to avoid not initialized error
        lat = "0"
        lng = "0"
        var isRegistered = false
        var isComplete = false

        account.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                subscribedID = snapshot.child("subscribed").value.toString()
                isRegistered = snapshot.child("registered").value.toString().toBoolean()
                isComplete = snapshot.child("isFormComplete").value.toString().toBoolean()

                if(snapshot.child("firstName").value != null) {
                    val welcomeText = "Welcome, " + snapshot.child("firstName").value.toString()
                    binding.userLandingWelcomeText.text = welcomeText
                }

                if(!isRegistered) {
                    binding.userLandingIsRegistered.isChecked = false
                    binding.userLandingButtonForm.visibility = View.GONE
                    binding.userLandingButtonForm.isClickable = false
                }

                if(isRegistered) {
                    binding.userLandingIsRegistered.isChecked = true
                    binding.userLandingButtonForm.visibility = View.VISIBLE
                    binding.userLandingButtonForm.isClickable = true
                }

                if(isComplete) {
                    binding.userLandingIsComplete.isChecked = true
                    binding.userLandingButtonForm.visibility = View.GONE
                    binding.userLandingButtonForm.isClickable = false
                }

                if(!isComplete) {
                    binding.userLandingIsComplete.isChecked = false
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        
        locations.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.hasChildren()) {
                    snapshot.children.forEach {
                        if(it.key.toString() == subscribedID) {

                            lat = it.child("lat").value.toString()
                            lng = it.child("lng").value.toString()

                            binding.userLandingTitleTv.text = it.child("title").value.toString()
                            binding.userLandingAddressTv.text = it.child("address").value.toString()
                            binding.userLandingDescTv.text = it.child("desc").value.toString()

                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        binding.userLandingButtonSeeAll.setOnClickListener {
            startActivity(Intent(this, AllLocations::class.java))
        }

        binding.userLandingButtonForm.setOnClickListener {
            startActivity(Intent(applicationContext, UserForm::class.java))
        }

        binding.userLandingButtonUnsubscribe.setOnClickListener {
            account.child("subscribed").removeValue()
            account.child("registered").setValue(false)
            account.child("isComplete").setValue(false)
        }

        // END ONCREATE
    }

    override fun onStart() {
        super.onStart()
        checkPermissionsAndStartGeofencing()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeGeofences()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            //checkDeviceLocationSettingsAndStartGeofence(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED))
        {
            Toast.makeText(
                this,
                "Permissions denied",
                Toast.LENGTH_SHORT
            ).show()
            startActivity(Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } else {
            checkDeviceLocationSettingsAndStartGeofence()
        }
    }

    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION))
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    private fun requestForegroundAndBackgroundLocationPermissions() {
        val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

        if (foregroundAndBackgroundLocationPermissionApproved())
            return
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        ActivityCompat.requestPermissions(
            this@UserLanding,
            permissionsArray,
            resultCode
        )
    }

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    exception.startResolutionForResult(this@UserLanding,
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {

                }
            } else {
                Toast.makeText(
                    this,
                    "Location required", Toast.LENGTH_SHORT
                ).show()
                checkDeviceLocationSettingsAndStartGeofence()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                addGeofenceForClue()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofenceForClue() {

        val geofence = Geofence.Builder()
            .setRequestId(auth.uid.toString())
            .setCircularRegion(lat.toDouble(),
                lng.toDouble(),
                100f
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT or Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.removeGeofences(geofencePendingIntent).run {
            addOnCompleteListener {
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
                    addOnSuccessListener {

                    }
                    addOnFailureListener {

                    }
                }
            }
        }
    }

    private fun checkPermissionsAndStartGeofencing() {
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            checkDeviceLocationSettingsAndStartGeofence()
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }

    private fun removeGeofences() {
        if (!foregroundAndBackgroundLocationPermissionApproved()) {
            return
        }
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {

            }
            addOnFailureListener {

            }
        }
    }

    companion object {
        private const val TAG = "UserLanding"
        private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
        private const val LOCATION_PERMISSION_INDEX = 0
        private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
    }
}
