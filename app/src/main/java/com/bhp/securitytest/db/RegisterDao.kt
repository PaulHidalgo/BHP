package com.bhp.securitytest.db

import android.arch.persistence.room.*

@Dao
interface RegisterDao {

    @Query("Select * from register")
    fun getAll(): List<Register>

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insert(register: Register)

    @Query("Select * from register Where id=:id and state=:state")
    fun findByIdAndState(id: Long, state: String): Register?

    @Update
    fun update(register: Register)
}