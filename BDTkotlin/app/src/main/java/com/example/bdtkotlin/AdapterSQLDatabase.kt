package com.example.bdtkotlin

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import java.io.Serializable

@Entity(tableName = "SQLDatabasePersonal")
class sqlContainerPersonal(
    @PrimaryKey(autoGenerate = false)
    val id:Int = 0,
    val Name:String,
    val Email:String,
    val AP:String,
    val AM:String,
    val Phone:String,
    val Address:String,
    val Gender:Int,
):Serializable

@Entity(tableName = "SQLDatabaseHome")
class sqlContainerHome (
    @PrimaryKey(autoGenerate = false)
    val id:Int = 0,
    val Name:String,
    val Banner:String,
    val Score:Float,
    val JobsDone:Int,
    val Credits:Int,
    val Tag1:String,
    val Tag2:String,
    val Tag3:String
):Serializable



//Personal
@Dao
interface SQLPersonal {
    /*
    @Query("SELECT * FROM SQLDatabasePersonal")
    fun getAll(): LiveData<sqlContainerPersonal>
    */
    @Query("SELECT * FROM SQLDatabasePersonal WHERE id = 0")
    fun get(): LiveData<sqlContainerPersonal>

    @Query("SELECT * FROM SQLDatabasePersonal WHERE id = 0")
    fun getStatic(): sqlContainerPersonal

    @Insert
    fun insertAll(vararg SQLDatabasePersonal: sqlContainerPersonal)

    @Update
    fun update(SQLDatabasePersonal: sqlContainerPersonal)

    @Query("DELETE FROM SQLDatabasePersonal")
    fun nuke()
}

//Home
@Dao
interface SQLHome {
    /*
    @Query("SELECT * FROM SQLDatabaseHome")
    fun getAll(): LiveData<sqlContainerHome>
    */
    @Query("SELECT * FROM SQLDatabaseHome WHERE id = 0")
    fun get(): LiveData<sqlContainerHome>

    @Query("SELECT * FROM SQLDatabaseHome WHERE id = 0")
    fun getStatic(): sqlContainerHome

    @Insert
    fun insertAll(vararg SQLDatabaseHome: sqlContainerHome)

    @Update
    fun update(SQLDatabaseHome: sqlContainerHome)

    @Query("DELETE FROM SQLDatabaseHome")
    fun nuke()
}


@Database(entities = [sqlContainerPersonal::class, sqlContainerHome::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun userDataPersonal(): SQLPersonal
    abstract fun userDataHome(): SQLHome

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE

            if(tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()

                INSTANCE = instance

                return instance
            }
        }
    }
}