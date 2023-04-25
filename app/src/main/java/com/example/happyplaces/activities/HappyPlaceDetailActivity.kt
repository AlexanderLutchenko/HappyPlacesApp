package com.example.happyplaces.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.happyplaces.database.HappyPlaceEntity
import com.example.happyplaces.databinding.ActivityHappyPlaceDetailBinding

//Step30 Create an activity to display the record details once its clicked in RecyclerView
//create a binding object for current activity layout
private lateinit var binding: ActivityHappyPlaceDetailBinding
//prepare the global variable to receive a HappyPlace record details from another activity
private var receivedRecord: HappyPlaceEntity? = null
class HappyPlaceDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Fill up the binding object with activity_happy_place_detail layout items
        binding = ActivityHappyPlaceDetailBinding.inflate(layoutInflater)
        //Now binding object can be used to access its layout items, set the contentView with it, etc.
        setContentView(binding.root)
        //Create a custom toolbar bar
        setSupportActionBar(binding.toolbarPlaceDetail)
        //Create a "back action" button in toolbar
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        //Set action of "back action" button same as a regular Android UI navigation "back button"
        binding.toolbarPlaceDetail.setNavigationOnClickListener {
            onBackPressed()
        }
    //Receive HappyPlace record details sent from MainActivity (Implemented in HappyPlaceAdapter)
        if(intent.hasExtra("Record Details")){
            //Get the extra details from intent and cast them as HappyPlaceEntity datatype
            receivedRecord = intent.getParcelableExtra("Record Details") as HappyPlaceEntity?
        }
        //Populate the screen view with the received record details
        if(receivedRecord != null){
            supportActionBar!!.title = receivedRecord?.title
            binding.ivPlaceDetailImage.setImageURI(Uri.parse(receivedRecord?.image))
            binding.tvPlaceDetailDescription.text = receivedRecord?.description
            binding.tvPlaceDetailLocation.text = receivedRecord?.location



            //Step54 Set actions for "View on Map" button
            binding.btnViewOnMap.setOnClickListener {
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra("Happy Place Details", receivedRecord)
                startActivity(intent)
            }
        }
    }

}