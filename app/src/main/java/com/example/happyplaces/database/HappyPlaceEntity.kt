package com.example.happyplaces.database

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

//Entity is a data class that serves as a database table, contains variables to build out a column
//Must be annotated with @Entity and must have a @PrimaryKey annotated variable -
//property that has uniquely identifiable value among all database entries,
//never repeated through the entire database

@Entity(tableName = "happy-places-table")

data class HappyPlaceEntity (
    @PrimaryKey(autoGenerate = true) // Autogenerate the value of primary key ("id" in this table)
    val id: Int, //use coma to separate properties of an entity
    val title: String?,
    val description: String?,
    val date: String?,
    val location: String?,
    val image: String?,
    val latitude: Double,
    val longitude: Double
    //Step29 Transform the class to the datatype format that can be passed from one class to another
    //It will be required when passing a record of this datatype to intent that opens another activity in HappyPlacesAdapter
        ):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readDouble()
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(date)
        parcel.writeString(location)
        parcel.writeString(image)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
    }
    override fun describeContents(): Int {
        return 0
    }
    companion object CREATOR : Parcelable.Creator<HappyPlaceEntity> {
        override fun createFromParcel(parcel: Parcel): HappyPlaceEntity {
            return HappyPlaceEntity(parcel)
        }
        override fun newArray(size: Int): Array<HappyPlaceEntity?> {
            return arrayOfNulls(size)
        }
    }
}