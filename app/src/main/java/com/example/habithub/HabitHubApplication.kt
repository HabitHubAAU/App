package com.example.habithub

import android.app.Application
import com.example.habithub.data.database.HabitDatabase

class HabitHubApplication : Application()
{
    val database: HabitDatabase by lazy {
        HabitDatabase.getDatabase(this)
    }
}
