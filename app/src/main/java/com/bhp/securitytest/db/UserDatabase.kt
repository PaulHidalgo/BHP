package com.bhp.securitytest.db

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.migration.Migration
import android.content.Context
import com.bhp.securitytest.Converters

//TODO change version database
@Database(entities = arrayOf(User::class, Register::class, UserRegister::class), version = 2)
@TypeConverters(Converters::class)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun registerDao(): RegisterDao
    abstract fun userRegisterDao(): UserRegisterDao


    companion object {
        private var INSTANCE: UserDatabase? = null

        fun getInstance(context: Context): UserDatabase? {
            if (INSTANCE == null) {
                synchronized(UserDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            UserDatabase::class.java, "weather.db")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3 )
                            .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }

    object MIGRATION_1_2 : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE user ADD COLUMN idType TEXT NOT NULL DEFAULT 'C'")
        }
    }

    object MIGRATION_2_3 : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {

        }
    }
}