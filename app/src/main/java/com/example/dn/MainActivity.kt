package com.example.dn

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.dn.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        fun bitmapToString(bitmap: Bitmap): String {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        }

        fun stringToBitmap(encodedString: String): Bitmap? {
            if (encodedString !== null) {
                val decodedBytes = Base64.decode(encodedString, Base64.DEFAULT)
                return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            }
            return null
        }

        fun getUserDataValue(context: Context, key: String): String? {
            val preferences = context.getSharedPreferences("userData", Context.MODE_PRIVATE)
            return preferences.getString("pp", "");
        }

        fun writeLocationFile(context: Context, fileName: String, data: android.location.Location) {
            val content = "lat=${data.latitude},long=${data.longitude}"

            Log.d("DEBUG", "$content")

            try {
                // Internal storage directory
                val internalStorageDir = context.getExternalFilesDir(null)

                val file = File(internalStorageDir, fileName)
                file.writeText(content)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun readLocationFile(context: Context, fileName: String): android.location.Location? {
            try {
                // Internal storage directory
                val internalStorageDir = context.getExternalFilesDir(null)

                val file = File(internalStorageDir, fileName)
                val content = file.readText()

                val loc = recreateLocationFromString(content)

                if (loc != null) {
                    Log.d("JSON", "${loc.latitude}")
                }

                return loc
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        fun recreateLocationFromString(locationString: String): Location? {
            try {
                // Split the string using the delimiter (assumed to be a comma)
                val parts = locationString.split(",")

                if (parts.size == 2) {
                    //Log.d("test", "${ parts[0].split("lat=")[1] }")
                    val latitude = parts[0].split("lat=")[1].toDouble()
                    val longitude = parts[1].split("long=")[1].toDouble()

                    // Create a new Location object
                    val recreatedLocation = Location("RecreatedLocation")
                    recreatedLocation.latitude = latitude
                    recreatedLocation.longitude = longitude

                    return recreatedLocation
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.appBarMain.fab.setOnClickListener { view ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkLocationPermission()) {
                    getLocation(view)
                } else {
                    requestLocationPermission()
                }
            } else {
                getLocation(view)
            }
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun getLocation(view: View) {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val locationListener = LocationListener { location -> // Handle location changes

            val oldLocation = readLocationFile(applicationContext, "loc.txt")

            writeLocationFile(applicationContext, "loc.txt", location)
            if (findViewById<TextView>(R.id.text_home) !== null) {
                findViewById<TextView>(R.id.text_home).text =
                    "Dernière position enregistrée (lat: ${location.latitude}, long: ${location.longitude})"
            }

            if (oldLocation === null) {
                Snackbar.make(view, "Initialisation de votre position, sauvegarde en cours", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            5000, // minimum time interval between location updates in milliseconds
            3.0f, // minimum distance between location updates in meters
            locationListener
        )
    }

    fun takePhotoFromCamera(view: View) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val camera: Int = 123
        startActivityForResult(intent, camera)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val imageView: ImageView = findViewById(R.id.image)
        val bitmap = data?.extras?.get("data") as Bitmap

        val filename = "my_image.jpg"
        val directory =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "")

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, filename)

        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val preferences = getSharedPreferences("userData", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("pp", bitmapToString(bitmap))
        editor.apply()

        imageView.setImageBitmap(bitmap)
    }
}