package com.bhp.securitytest.db

import android.arch.persistence.room.Entity
import android.os.Parcel
import android.os.Parcelable

//@Entity(tableName = "user", primaryKeys = arrayOf("id", "email"))
@Entity(tableName = "user", primaryKeys = ["id"])
class User(
        var id: String = "",
        var idType: String = "C",
        var name: String = "",
        var lastname: String = "",
        var email: String = "",
        var company: String = "",
        var position: String = "",
        var q1: Boolean? = null,
        var q2: Boolean? = null,
        var date: Long = 0,
        var success: Boolean? = null) : Parcelable {

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        idType = parcel.readString()
        name = parcel.readString()
        lastname = parcel.readString()
        email = parcel.readString()
        company = parcel.readString()
        position = parcel.readString()
        q1 = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        q2 = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        date = parcel.readLong()
        success = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest!!.writeString(id)
        dest!!.writeString(idType)
        dest.writeString(name)
        dest.writeString(lastname)
        dest.writeString(email)
        dest.writeString(company)
        dest.writeString(position)
        dest.writeValue(q1)
        dest.writeValue(q2)
        dest.writeLong(date)
        dest.writeValue(success)
    }

    override fun describeContents(): Int {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR
    }

    override fun toString(): String {
        return "User(id='$id', idType='$idType', name='$name', lastname='$lastname', email='$email', company='$company', position='$position', q1=$q1, q2=$q2, date=$date, success=$success)"
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}