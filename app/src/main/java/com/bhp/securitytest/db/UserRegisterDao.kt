package com.bhp.securitytest.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.os.Parcel
import android.os.Parcelable
import org.joda.time.DateTime
import java.util.*

@Dao
interface UserRegisterDao {

    @Query("Select ur.date as dateUser, user.id as userId, user.name as userName, ur.hour as hourUser, register.description as descState " +
            "from user_register as ur " +
            "INNER JOIN user ON user.id = ur.user_id " +
            "INNER JOIN register ON register.id = ur.register_id " +
            "WHERE register.state = :state " +
            "AND dateUser BETWEEN :from AND :to")
    fun getUsersRegisterByState(state: String, from: Date, to: Date): List<UserRegisterQuery>

//    @Query("SELECT * FROM user_register AS ur " +
//            "INNER JOIN user ON user.id = ur.user_id " +
//            "INNER JOIN register ON register.id = ur.register_id " +
//            "WHERE register.state = :state " +
//            "AND ur.date = :date")
//    fun getUserRegisterByStateDate(state: String, date: String): List<UserRegister>


    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insert(userRegister: UserRegister)

    class UserRegisterQuery() : Parcelable {
        var dateUser: Date? = null
        var userId: String = ""
        var userName: String = ""
        var hourUser: DateTime? = null
        var descState = ""

        constructor(parcel: Parcel) : this() {
            dateUser = Date(parcel.readLong())
            userId = parcel.readString()
            userName = parcel.readString()
            hourUser = DateTime(parcel.readLong())
            descState = parcel.readString()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeLong(dateUser!!.time)
            parcel.writeString(userId)
            parcel.writeString(userName)
            parcel.writeLong(hourUser!!.millis)
            parcel.writeString(descState)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<UserRegisterQuery> {
            override fun createFromParcel(parcel: Parcel): UserRegisterQuery {
                return UserRegisterQuery(parcel)
            }

            override fun newArray(size: Int): Array<UserRegisterQuery?> {
                return arrayOfNulls(size)
            }
        }

        override fun toString(): String {
            return "UserRegisterQuery(dateUser='$dateUser', userId='$userId', userName='$userName', hourUser='$hourUser', descState='$descState')"
        }
    }
}