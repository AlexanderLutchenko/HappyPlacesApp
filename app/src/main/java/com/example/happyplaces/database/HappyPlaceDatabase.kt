package com.example.happyplaces.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//An abstract Class that inherits from Room Database class
//Extends the Room Database - part of Android JetPack that replaced standard SQLite
//Contains the database setup and serves as the main access point to the database
//Must contain the version of the Database and the Entities to use. Must have at least one entity
//Must be annotated with @Database
@Database(entities = [HappyPlaceEntity::class], version = 1)


abstract class HappyPlaceDatabase: RoomDatabase() {
    //function to connect database to its DAO (creates an instance of a DAO),
    //used in MainActivity to create "happyDAO"
    abstract fun createHappyDAO():HappyPlaceDAO

    companion object{

        @Volatile //value of a volatile variable isn't cashed, reed/write is done via main memory
        //It means the changes done by one thread is shared and visible to other threads

        //variable to keep a reference to any Database returned via getInstance function below,
        // helps to avoid repeatedly initialising the Database
        private var DATABASEINSATNCE: HappyPlaceDatabase? = null



        //get an instance of the database that will be a singleton
        // (single instance shared through the entire app)
        fun getHaPlDatabase(context: Context):HappyPlaceDatabase{
            synchronized(this){ //Only one thread can enter a synchronized method at a time

                // Copy the current value of DATABASEINSATNCE to a local variable,
                //as smartcast is only available to a local variables
                var instance = DATABASEINSATNCE

                if(instance == null){ //Only initialises a Database instance once (one single time - "singleton")
                    instance = Room.databaseBuilder(
                        context.applicationContext, HappyPlaceDatabase::class.java,
                        "happy-place-database").fallbackToDestructiveMigration().build()
//fallbackToDestructiveMigration() - builds the database rebuilding it each time it changes,
// no "migration" used

                    DATABASEINSATNCE = instance
                }
                return instance
            }
        }
    }

}