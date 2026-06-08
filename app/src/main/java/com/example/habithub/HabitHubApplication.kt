package com.habithub

import android.app.Application
import com.habithub.data.database.HabitDatabase

class HabitHubApplication : Application() {
    val database: HabitDatabase by lazy {
        HabitDatabase.getDatabase(this)
    }
}
