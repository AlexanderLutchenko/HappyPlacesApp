package com.example.happyplaces

import android.app.Application
import com.example.happyplaces.database.HappyPlaceDatabase


//In order to create DAO, the Application class is required where the database will be initialised

//Note - every application class must be declared in the AndroidManifest.xml as well,
//so that it can be used for any activity of the application

/*<application
android:name=".HappyPlacesApp"*/
class HappyPlacesApp: Application() {
    // create/initialise database lazily - loads the values to db only when needed
    val db by lazy{
        HappyPlaceDatabase.getHaPlDatabase(this) //this - HappyPlaceApp application context
    }
}