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

    @Query("Select ur.date as dateUser, user.idType,user.id as userId, user.name as userName, user.lastname as userLastName, ur.hour as hourUser, register.description as descState " +
            "from user_register as ur " +
            "INNER JOIN user ON user.id = ur.user_id " +
            "INNER JOIN register ON register.id = ur.register_id " +
            "WHERE register.state = :state " +
            "AND dateUser BETWEEN :from AND :to " +
            "ORDER BY dateUser")
    fun getUsersRegisterByState(state: String, from: Date, to: Date): List<UserRegisterQuery>

    @Query("Select *  " +
            "from user_register as ur " +
            "WHERE ur.date BETWEEN :from AND :to " +
            "ORDER BY ur.date " )
    fun getAllUsersRegisterGroupBy(from: Date, to: Date): List<UserRegister>

    @Query("Select ur.date as dateUser,user.idType, user.id as userId, user.name as userName, user.lastname as userLastName, ur.hour as hourUser, register.description as descState " +
            "from user_register as ur " +
            "INNER JOIN user ON user.id = ur.user_id " +
            "INNER JOIN register ON register.id = ur.register_id " +
            "ORDER BY dateUser")
    fun getAllUsersRegister(): List<UserRegisterQuery>

    @Query("SELECT COUNT(*) FROM user_register AS ur " +
            "INNER JOIN user ON user.id = ur.user_id " +
            "INNER JOIN register ON register.id = ur.register_id " +
            "WHERE ur.user_id = :id " +
            "AND ur.date = :date")
    fun getCountUserDay(id: String, date: Date): Int

    //Duda --> El mismo día puede tener un ingreso y salida dos veces

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insert(userRegister: UserRegister)

    class UserRegisterQuery() : Parcelable {
        var dateUser: Date? = null
        var idType: String = ""
        var userId: String = ""
        var userName: String = ""
        var userLastName: String = ""
        var hourUser: DateTime? = null
        var descState = ""

        constructor(parcel: Parcel) : this() {
            dateUser = Date(parcel.readLong())
            idType = parcel.readString()
            userId = parcel.readString()
            userName = parcel.readString()
            userLastName = parcel.readString()
            hourUser = DateTime(parcel.readLong())
            descState = parcel.readString()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeLong(dateUser!!.time)
            parcel.writeString(idType)
            parcel.writeString(userId)
            parcel.writeString(userName)
            parcel.writeString(userLastName)
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
            return "UserRegisterQuery(dateUser='$dateUser', userId='$userId', userName='$userName',userLastName='$userLastName', hourUser='$hourUser', descState='$descState')"
        }
    }
}