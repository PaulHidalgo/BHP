package com.bhp.securitytest.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import org.joda.time.DateTime
import java.util.*

@Entity(tableName = "user_register",
        foreignKeys = arrayOf(
                ForeignKey(entity = User::class, parentColumns = arrayOf("id"), childColumns = arrayOf("user_id")),
                ForeignKey(entity = Register::class, parentColumns = arrayOf("id"), childColumns = arrayOf("register_id"))))
class UserRegister(
        var user_id: String = "",
        var register_id: Long = 0,
        var date: Date = Date(),
        var hour: DateTime = DateTime()) : Parcelable {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    constructor(parcel: Parcel) : this() {
        user_id = parcel.readString()
        register_id = parcel.readLong()
        date = Date(parcel.readLong())
        hour = DateTime(parcel.readLong())

    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest!!.writeString(user_id)
        dest.writeLong(register_id)
        dest.writeLong(date.time)
        dest.writeLong(hour.millis)
    }

    override fun describeContents(): Int {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR
    }

    override fun toString(): String {
        return "UserRegister(id='$id',user_id='$user_id', register_id='$register_id', date='$date', hour='$hour')"
    }

    companion object CREATOR : Parcelable.Creator<UserRegister> {
        override fun createFromParcel(parcel: Parcel): UserRegister {
            return UserRegister(parcel)
        }

        override fun newArray(size: Int): Array<UserRegister?> {
            return arrayOfNulls(size)
        }
    }
}