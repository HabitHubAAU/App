package com.example.habithub.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home   : Screen("home",             "Today",  Icons.Filled.Home)
    object Add    : Screen("add",              "Add",    Icons.Filled.Add)
    object Stats  : Screen("stats",            "Stats",  Icons.Filled.BarChart)
    object Detail : Screen("detail/{habitId}", "Detail", Icons.Filled.Info) {
        fun route(id: Int) = "detail/$id"
    }
    object Edit   : Screen("edit/{habitId}",   "Edit",   Icons.Filled.Edit) {
        fun route(id: Int) = "edit/$id"
    }
    object Pulse  : Screen("pulse",            "Puls",   Icons.Filled.Info)
    object Pomodoro : Screen("pomodoro",       "Pomodoro", Icons.Filled.Info)
}
