package com.bhp.securitytest.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.FAIL
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update

@Dao
interface UserDao {

    @Query("Select * from user where id != '1792739942'")
    fun getAll(): List<User>

    @Insert(onConflict = FAIL)
    fun insert(user: User)

    @Query("Select * from user Where email=:email")
    fun findByEmail(email: String): User?

    @Query("Select * from user Where id=:id")
    fun findById(id: String): User?

    @Query("Select * from user Where id=:id and idType=:type")
    fun findByIdAndType(id: String, type: String): User?

    @Update
    fun update(user: User)
}