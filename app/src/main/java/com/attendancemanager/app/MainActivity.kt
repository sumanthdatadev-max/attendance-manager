package com.attendancemanager.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.attendancemanager.app.ui.navigation.AppNavGraph
import com.attendancemanager.app.ui.theme.AttendanceManagerTheme
import com.attendancemanager.app.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AttendanceApp
        val viewModelFactory = ViewModelFactory.from(app)

        setContent {
            AttendanceManagerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(viewModelFactory = viewModelFactory)
                }
            }
        }
    }
}
