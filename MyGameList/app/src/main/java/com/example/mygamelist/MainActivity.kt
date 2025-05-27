package com.example.mygamelist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.mygamelist.ui.navigation.BottomNavigationBar
import com.example.mygamelist.ui.navigation.MyGameListNavHost
import com.example.mygamelist.ui.theme.MyGameListTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyGameListTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = {
                        BottomNavigationBar(navController = navController)
                    }
                ) { innerPadding ->
                    MyGameListNavHost(navController = navController, padding = innerPadding)
                }
            }
        }
    }
}



@Preview
@Composable
fun PreviewApp(){
    MyGameListTheme {
        val navController = rememberNavController()
        Scaffold(
            bottomBar = {
                BottomNavigationBar(navController = navController)
            }
        ) { innerPadding ->
            MyGameListNavHost(navController = navController, padding = innerPadding)
        }
    }
}