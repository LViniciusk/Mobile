package com.example.taskliteapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.taskliteapp.ui.DesafioTaskListScreen
import com.example.taskliteapp.ui.theme.TaskLiteAppTheme
import com.example.taskliteapp.viewmodel.DesafioTaskViewModel

class MainActivity : ComponentActivity() {
    private val taskViewModel: DesafioTaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TaskLiteAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DesafioTaskListScreen(
                        viewModel = taskViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
