package com.bhp.securitytest.db

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.migration.Migration
import android.content.Context
import com.bhp.securitytest.Converters

@Database(entities = arrayOf(User::class, Register::class, UserRegister::class), version = 3)
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
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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
            //Create new table
            database.execSQL("CREATE TABLE users_new (id TEXT NOT NULL, idType TEXT NOT NULL DEFAULT 'C', name TEXT NOT NULL, lastname TEXT NOT NULL, email TEXT NOT NULL, company TEXT NOT NULL, position TEXT NOT NULL, q1 INTEGER, q2 INTEGER, date INTEGER NOT NULL, success INTEGER, PRIMARY KEY(id))")

            //Copy data
            database.execSQL("INSERT INTO users_new (id, idType, name, lastname, email, company, position, q1, q2, date, success) " +
                    "SELECT id, idType, name, lastname, email, company, position, q1, q2, date, success FROM user")

            // Remove the old table
            database.execSQL("DROP TABLE user")

            // Change the table name to the correct one
            database.execSQL("ALTER TABLE users_new RENAME TO user")

            //Create new table register
            database.execSQL("CREATE TABLE register (id INTEGER NOT NULL, state TEXT NOT NULL, description TEXT NOT NULL, PRIMARY KEY(id))")

            //Create new table user_register
            database.execSQL("CREATE TABLE user_register (id INTEGER NOT NULL, user_id TEXT NOT NULL, register_id INTEGER NOT NULL, date INTEGER NOT NULL, hour INTEGER NOT NULL, PRIMARY KEY(id), " +
                    "FOREIGN KEY(user_id) REFERENCES user(id) ON DELETE NO ACTION ON UPDATE NO ACTION, " +
                    "FOREIGN KEY(register_id) REFERENCES register(id) ON DELETE NO ACTION ON UPDATE NO ACTION)")

        }
    }
}