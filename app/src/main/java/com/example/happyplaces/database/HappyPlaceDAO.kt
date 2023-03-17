package com.example.happyplaces.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao //Data Access Object

//The "Interface" Class that contains queries to be performed on the database
//Has simplified annotations for frequent operations
//Must be annotated with @Dao

    interface HappyPlaceDAO {

        @Insert
        //use suspend function to run it in coroutine mode, in background
        suspend fun insert(happyPlaceEntity: HappyPlaceEntity)

        @Update
        //use suspend function to run it in coroutine mode, in background
        suspend fun update(happyPlaceEntity: HappyPlaceEntity)

        @Delete
        //use suspend function to run it in coroutine mode, in background
        suspend fun delete(happyPlaceEntity: HappyPlaceEntity)


        //Below "Flow" is a part of coroutine class (kotlin.coroutines.flow) that used to hold
        //values that change in runtime,
        //provides live update while performing operations in a different thread
        // Uses "collect" method further in code for live update the value of its variable
        //happyDAO.fetchAllPlaces().collectvc - example of code in this project's MainActivity

        @Query("SELECT * FROM `happy-places-table`")
        fun fetchAllPlaces(): Flow<List<HappyPlaceEntity>>

        @Query("SELECT * FROM `happy-places-table` where id=:id")
        fun fetchPlaceByID(id:Int): Flow<HappyPlaceEntity>
}