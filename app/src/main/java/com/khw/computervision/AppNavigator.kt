package com.khw.computervision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.khw.computervision.ui.theme.ComputerVisionTheme
//네비게이션
class AppNavigator : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComputerVisionTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") { LoginScreen(navController) }
                    composable("sales") { SaleScreen(navController) }
//                    composable(
//                        "detailProduct/{productId}",
//                        arguments = listOf(navArgument("productId") { type = NavType.StringType })
//                    ) { backStackEntry ->
//                        DetailScreen(
//                            navController = navController,
//                            productId = backStackEntry.arguments?.getString("productId")
//                        )
//                    }
                }
            }
        }
    }
}