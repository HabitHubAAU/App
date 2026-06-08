package com.example.habithub.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String = "",
    val emoji: String = "⭐",
    val colorValue: Long = 0xFF6750A4L,
    val targetDays: Int = 0b1111111,
    val createdAt: Long = System.currentTimeMillis()
)
