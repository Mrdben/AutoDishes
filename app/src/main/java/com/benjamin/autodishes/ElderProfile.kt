package com.benjamin.autodishes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ElderProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Room uses Int IDs for local storage
    val name: String,
    val diseases: String, // Store as a comma-separated string for simplicity
    val allergies: String,
    val preferences: String
)