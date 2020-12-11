package com.example.challengetracker

import android.os.Parcel
import android.os.Parcelable

data class ActivityEntity
    (var name : String,
     var points : Float
     ) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString().toString(),
            parcel.readFloat()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeFloat(points)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ActivityEntity> {
        override fun createFromParcel(parcel: Parcel): ActivityEntity {
            return ActivityEntity(parcel)
        }

        override fun newArray(size: Int): Array<ActivityEntity?> {
            return arrayOfNulls(size)
        }
    }

}