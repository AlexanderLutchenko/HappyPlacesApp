package com.example.happyplaces.utilities

import android.content.Context
import android.location.Address
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import java.util.*

//Step67 Crate a helper-class to transform a user Location(latitude/longitude) into a human-readable address

class GetAddressFromLatLng (context: Context, private val lattd: Double,private val lngtd: Double) {

    //to decode the latitude and longitude value to text address
    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())

    //Variable for interface object
    private lateinit var addressListener: AddressListener

    //Function to get interface-object required actions(functions) provided from a calling activity
    fun setCustomAddressListener (providedAddressListener:AddressListener){
        addressListener=providedAddressListener
    }


    //Get the address in background
    suspend fun getAddressInBckgrnd(){
        try {
            //maxResults - get only limited number of results of multiple addresses for latitude/longitude pair
            val addressList: List<Address>? = geocoder.getFromLocation(lattd, lngtd, 1)
            if(!addressList.isNullOrEmpty()){//If addresses were successfully received
                val address : Address = addressList[0]
                val stringBuilder = StringBuilder()
                for (i in 0..address.maxAddressLineIndex ){ //Address datatype consists of multiple lines (street, city, country, etc)
                    //get all lines of Address into one line of string "StringBuilder" divided by " " space character
                    stringBuilder.append(address.getAddressLine(i)).append(" ")

                }
                //remove unnecessary last space character from stringBuilder string
                stringBuilder.deleteCharAt(stringBuilder.length-1)
                //switch to Main thread to update the UI-related values
                withContext(Main){
                addressListener.onAddressFound(stringBuilder.toString())
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
            //switch to Main thread to update the UI-related values
            withContext(Main){
            addressListener.onError("No address found") //Store an App-purpose-acceptable record
            }
        }
    }

    //Create an interface to receive required for the current class functions (must be overridden in a calling function)
    interface AddressListener{
        fun onAddressFound(address: String?)
        fun onError(address: String?)
    }


}