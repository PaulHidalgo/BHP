package com.bhp.securitytest.db

import android.arch.persistence.room.Entity
import android.os.Parcel
import android.os.Parcelable

@Entity(tableName = "register", primaryKeys = ["id"])

class Register(
        var id: Long = 0,
        var state: String = "",
        var description: String = "") : Parcelable {

    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        state = parcel.readString()
        description = parcel.readString()

    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest!!.writeLong((id))
        dest.writeString(state)
        dest.writeString(description)
    }

    override fun describeContents(): Int {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR
    }

    override fun toString(): String {
        return "Register(id='$id', state='$state', description='$description')"
    }

    companion object CREATOR : Parcelable.Creator<Register> {
        override fun createFromParcel(parcel: Parcel): Register {
            return Register(parcel)
        }

        override fun newArray(size: Int): Array<Register?> {
            return arrayOfNulls(size)
        }
    }
}