package com.benjamin.autodishes

import androidx.room.*

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles")
    suspend fun getAllProfiles(): List<ElderProfile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ElderProfile)

    @Delete
    suspend fun deleteProfile(profile: ElderProfile)

    @Update
    suspend fun updateProfile(profile: ElderProfile)
}