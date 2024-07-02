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

class AppNavigator : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComputerVisionTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") { LoginScreen(navController) }
                    composable("sales") { SaleScreen(navController) }
                    composable(
                        "detailProduct/{productId}",
                        arguments = listOf(navArgument("productId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        DetailScreen(navController, backStackEntry.arguments?.getString("productId"))
                    }
                    composable(
                        "decorate/{clickedUri}",
                        arguments = listOf(navArgument("clickedUri") { type = NavType.StringType })
                    ) { backStackEntry ->
                        DecorateScreen(navController, backStackEntry.arguments?.getString("clickedUri") ?: "")
                    }
                    composable(
                        "decorate",
                    ) {
                        DecorateScreen(navController, "")
                    }
                    composable(
                        "aiImgGen/{clickedUri}/{clickedCategory}",
                        arguments = listOf(
                            navArgument("clickedUri") { type = NavType.StringType },
                            navArgument("clickedCategory") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        AiImgGenScreen(
                            navController,
                            backStackEntry.arguments?.getString("clickedUri") ?: "",
                            backStackEntry.arguments?.getString("clickedCategory") ?: ""
                        )
                    }
                    composable(
                        "insert/{clickedUri}/{requestAiImg}",
                        arguments = listOf(
                            navArgument("clickedUri") { type = NavType.StringType },
                            navArgument("requestAiImg") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        InsertScreen(
                            navController,
                            backStackEntry.arguments?.getString("clickedUri") ?: "",
                            backStackEntry.arguments?.getString("requestAiImg") ?: ""
                        )
                    }
                }
            }
        }
    }
}
