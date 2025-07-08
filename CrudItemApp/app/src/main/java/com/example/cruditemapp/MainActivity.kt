package com.example.cruditemapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.cruditemapp.ui.theme.CrudItemAppTheme
import com.example.cruditemapp.ui.view.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CrudItemAppTheme {
                MainScreen()
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    CrudItemAppTheme {
        MainScreen()
    }
}