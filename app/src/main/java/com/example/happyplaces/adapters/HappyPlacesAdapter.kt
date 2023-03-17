package com.example.happyplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.activities.AddHappyPlaceActivity
import com.example.happyplaces.activities.HappyPlaceDetailActivity
import com.example.happyplaces.database.HappyPlaceEntity
import com.example.happyplaces.databinding.ItemRecyclerViewBinding

//Step24 Create a RecyclerView Adapter

open class HappyPlacesAdapter(private var context:Context, private var list:ArrayList<HappyPlaceEntity>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    //Step24.1 What should happen once the RecyclerView ViewHolder is created
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder{
        //Inflates the item view which is designed in xml layout file
        return MainViewHolder(ItemRecyclerViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false))
    }
    /**
     * Binds each item in the ArrayList to a view ( view element on the screen)
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    //Step24.2 Position HappyPlace record elements within the layout of RecyclerView
    // for each individual record
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val record = list[position]
        if (holder is MainViewHolder) {
            //context = holder.binding.root.context
            holder.binding.ivCVPlaceImage.setImageURI(Uri.parse(record.image))
            holder.binding.tvTitle.text = record.title
            holder.binding.tvDescription.text = record.description

            //Step27 Add OnClickListener to display HappyPlace details on a separate screen
            holder.binding.root.setOnClickListener {
                val intent = Intent(context, HappyPlaceDetailActivity::class.java)
                intent.putExtra("Record Details",record)
                context.startActivity(intent)
            }
        }
    }


    //Step24.3 Gets the number of items in the list
    override fun getItemCount(): Int {
        return list.size
    }

//Step24.4 ViewHolder describes an item view and metadata about its place within the RecyclerView item layout
    class MainViewHolder(val binding:ItemRecyclerViewBinding) : RecyclerView.ViewHolder(binding.root)


    //Step35 Function to switch to AddHappyPlaces activity and edit the populated entries there
    fun editItem(activity: Activity, position:Int, requestCode: Int){
            val intent = Intent(activity, AddHappyPlaceActivity::class.java)
        //send the HappyPlaces record details into AddHappyPlaces activity to populate the fields
            intent.putExtra("Edit Record Details", list[position])
        activity.startActivityForResult(intent, requestCode)
        //inform the adapter that item at specific position is updated
        notifyItemChanged(position)
    }

    //Step41 Function to get title of the RecyclerView item
    fun getTitle(position: Int) : String?{
        return list[position].title
    }

    //Step42 Function to get item to be removed from HappyPlaceDataBase and remove it from RecyclerView
    fun removeAt(position: Int) : HappyPlaceEntity{
        notifyItemRemoved(position)
        return list[position]
    }
}