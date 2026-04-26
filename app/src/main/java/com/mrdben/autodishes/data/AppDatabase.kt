package com.mrdben.autodishes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

import com.mrdben.autodishes.familygraphs.data.Family
import com.mrdben.autodishes.familygraphs.data.Elder
import com.mrdben.autodishes.familygraphs.data.ElderDao
import com.mrdben.autodishes.familygraphs.data.FamilyDao
import com.mrdben.autodishes.dishgraphs.data.Dish
import com.mrdben.autodishes.dishgraphs.data.DishDao
import com.mrdben.autodishes.ordergraphs.data.Order
import com.mrdben.autodishes.ordergraphs.data.OrderDao

@Database(
    entities = [Family::class, Elder::class, Dish::class, Order::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun familyDao(): FamilyDao
    abstract fun elderDao(): ElderDao
    abstract fun dishDao(): DishDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase?= null
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "autodish_database"
                )
                .fallbackToDestructiveMigration(false)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}