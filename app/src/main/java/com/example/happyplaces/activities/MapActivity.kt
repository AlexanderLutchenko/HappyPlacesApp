package com.example.happyplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.happyplaces.R
import com.example.happyplaces.database.HappyPlaceEntity
import com.example.happyplaces.databinding.ActivityMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions



//Step57 Make the activity support map fragments, inherit from OnMapReadyCallback
class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    //Step50 Create an activity to show HappyPlaces locations on a map
    //create a binding object for activity_map.xml layout
    private lateinit var binding: ActivityMapBinding
    //variable to store received from intent HappyPlace Record object
    private var receivedRecord: HappyPlaceEntity? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Fill up the binding object with activity_map layout items
        binding = ActivityMapBinding.inflate(layoutInflater)
        //Now binding object can be used to access its layout items, set the contentView with it, etc.
        setContentView(binding.root)

        //Step55 Get the HappyPlace record from the intent
        if(intent.hasExtra("Happy Place Details")){
            receivedRecord = intent.getParcelableExtra("Happy Place Details")
        }
        //Create an action bar
        setSupportActionBar(binding.toolbarMap)
        //Create a "back action" button in toolbar
        if (supportActionBar != null && receivedRecord != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = receivedRecord?.title
        }
        binding.toolbarMap.setNavigationOnClickListener {
            //Set same action as a regularAndroid UI navigation "back button" has
            onBackPressedDispatcher.onBackPressed()
        }

        //Step56 Use a supportMap Fragment
        if(receivedRecord != null ){
            val supportMapFragment: SupportMapFragment =
                supportFragmentManager.findFragmentById(R.id.frMap) as SupportMapFragment
            //get a Map in background
            supportMapFragment.getMapAsync(this)
        }
    }

    //Step58 Override a function for OnMapReadyCallback class, the MapActivity inherits from
    override fun onMapReady(googleMap: GoogleMap?) {
        if(receivedRecord != null) {
            //Post a marker with HappyPlace location on Google Maps
            val markerPosition = LatLng(receivedRecord!!.latitude, receivedRecord!!.longitude)
            googleMap!!.addMarker(MarkerOptions().position(markerPosition).
            title(receivedRecord!!.location))
            //Zoom the map view closer to HappyPlace location (set zoom value from 0f to 15f)
            googleMap.animateCamera(CameraUpdateFactory.
            newLatLngZoom(markerPosition, 8f))
        }
    }
}