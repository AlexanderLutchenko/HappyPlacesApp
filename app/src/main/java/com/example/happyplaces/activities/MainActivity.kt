package com.example.happyplaces.activities

import android.R
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.HappyPlacesApp
import com.example.happyplaces.adapters.HappyPlacesAdapter
import com.example.happyplaces.database.HappyPlaceDAO
import com.example.happyplaces.database.HappyPlaceEntity
import com.example.happyplaces.databinding.ActivityMainBinding
import com.example.happyplaces.utilities.SwipeToDeleteCallback
import com.example.happyplaces.utilities.SwipeToEditCallback
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch



class MainActivity : AppCompatActivity() {

    //create a binding object for activity_main.xml layout
    private lateinit var binding:ActivityMainBinding

    //create a global DAO object to access the Database within the entire activity
    private lateinit var happyDAO: HappyPlaceDAO


    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        //Fill up the binding object with activity_main layout items
        binding = ActivityMainBinding.inflate(layoutInflater)
        //Now binding object can be used to access its layout items, set the contentView with it, etc.
        setContentView(binding.root)

        //Step1.9 Set an action for the floating button "+", start AddHappyPlace activity when pressed
        binding.fabAddHappyPlace.setOnClickListener{
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            startActivity(intent)
        }

        //Step20 Create a DAO object to access the Database
        // In order to create a Database DAO instance, the Application class is required where the database will be initialised
        //If the db has been created previously in another module/class, it will be reused due to singleton pattern of its creation
        happyDAO = (application as HappyPlacesApp).db.createHappyDAO()

//Step25 In background (another thread) fill-up recyclerView with the existing records of database:
        //lifecycleScope is a coroutine block built to handle coroutines from any activity class
        lifecycleScope.launch {
            happyDAO.fetchAllPlaces().collect {
                val list = ArrayList(it) //Get the ArrayList of all HappyPlaces records in Database
                if(list.isNotEmpty()) {
                    binding.rvPlaces.visibility = View.VISIBLE
                    binding.tvNoPlaces.visibility = View.INVISIBLE
                    setupHappyPlacesRecyclerView(list)
                }else{
                    binding.rvPlaces.visibility = View.INVISIBLE
                    binding.tvNoPlaces.visibility = View.VISIBLE
                }
            }
        }
    }


    //Step26 Create a function to setup the recycler view to display the HappyPlaces records
    //Step26 Begin
    private fun setupHappyPlacesRecyclerView(list: ArrayList<HappyPlaceEntity>){
        //Disable invalidation of RecyclerView size on screen when adapter content changes,
        //to avoid unnecessary resources engagement
        //(Only applicable when RecyclerView doesn't use "wrap_content" in layout)
        binding.rvPlaces.setHasFixedSize(true)

        binding.rvPlaces.layoutManager = LinearLayoutManager(this)
        binding.rvPlaces.adapter = HappyPlacesAdapter(this, list)
    //Step26 End

    //Step34 Implement functionality for "swipe to edit" feature in the RecyclerView
        val swipeToEditHandler = object: SwipeToEditCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                //adapter must be passed each time the Handler created
                //to make sure it's not cached and uses the latest updated version
                val adapter = binding.rvPlaces.adapter as HappyPlacesAdapter
                adapter.editItem(this@MainActivity, viewHolder.adapterPosition, 1)
            }
        }
        val itemTouchEditHelper = ItemTouchHelper(swipeToEditHandler)
        //bind the edit feature to the RecyclerView
        itemTouchEditHelper.attachToRecyclerView(binding.rvPlaces)


        //Step40 Implement functionality for "swipe to delete" feature in the RecyclerView
        val swipeToDeleteHandler = object: SwipeToDeleteCallback(this){
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    //adapter must be passed each time the Handler created
                    //to make sure it's not cached and uses the latest updated version
                    val adapter = binding.rvPlaces.adapter as HappyPlacesAdapter
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setIcon(R.drawable.ic_dialog_alert)
                    //Use the custom-made "getTitle" function in HappyPlacesAdapter
                    //to get a record title and usi it in Alert Dialog Title
                    builder.setTitle("Delete record \"${adapter.getTitle(viewHolder.adapterPosition)}\" ?")
                    //Set action for "Yes" button of Alert Dialog choice
                    builder.setPositiveButton("Yes"){dialogInterface, _ ->
              //get the record to delete using custom-made "removeAt" function in HappyPlacesAdapter
                        val itemToDelete = adapter.removeAt(viewHolder.adapterPosition)
                        lifecycleScope.launch {
                            happyDAO.delete(itemToDelete)
                        }
                        //close the alert dialog once the record is deleted
                        dialogInterface.dismiss()
                        //Give a user an option to restore a deleted record with Snackbar
                        Snackbar.make(viewHolder.itemView, "Record \"${itemToDelete.title}\" deleted",
                            Snackbar.LENGTH_LONG).setAction("Undo") {
                            lifecycleScope.launch {
                                happyDAO.insert(itemToDelete)
                                Toast.makeText(this@MainActivity,"Record \"${itemToDelete.title}\" restored",Toast.LENGTH_SHORT).show()
                            }
                                adapter.notifyItemInserted(viewHolder.adapterPosition)
                            }.show()
                    }
                    //Set action for "No" button of Alert Dialog choice
                    builder.setNegativeButton("No"){ dialogInterface, _->
                        //recover the view of item on the screen(otherwise it'll remain with swiped background)
                        adapter.notifyItemChanged(viewHolder.adapterPosition)
                        //close the alert dialog
                        dialogInterface.dismiss()
                    }
                    //Create and show the AlertDialog itself
                    val alertDialog: AlertDialog = builder.create()
                    //Restrict the dialog to disappear by clicking on remaining screen area
                    alertDialog.setCancelable(false)
                    alertDialog.show()
                    }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeToDeleteHandler)
        //bind the delete feature to the RecyclerView
        itemTouchDeleteHelper.attachToRecyclerView(binding.rvPlaces)
    }
}

