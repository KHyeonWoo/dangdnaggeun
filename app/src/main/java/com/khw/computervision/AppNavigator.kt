package com.khw.computervision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.khw.computervision.ui.theme.ComputerVisionTheme

class AppNavigator : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComputerVisionTheme {
                val navController = rememberNavController()
                val aiViewModel: AiViewModel = viewModel() // ViewModel 인스턴스 생성
                val closetViewModel: ClosetViewModel = viewModel() // ViewModel 인스턴스 생성

                Scaffold(
                    topBar = {
                        if (shouldShowTopBar(navController)) {
                            LogoScreen("Detail") { navController.popBackStack() }
                        }
                    },
                    bottomBar = {
                        if (shouldShowBottomBar(navController)) {
                            BottomNavigationBar(navController, aiViewModel) // ViewModel 전달
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") { LoginScreen(navController, closetViewModel) }
                        composable("sales") { SaleScreen(navController) }
                        composable(
                            "detailProduct/{productId}",
                            arguments = listOf(navArgument("productId") {
                                type = NavType.StringType
                            })
                        ) { backStackEntry ->
                            DetailScreen(
                                navController,
                                backStackEntry.arguments?.getString("productId")
                            )
                        }
//                        composable(
//                            "decorate/{encodedClickedUri}",
//                            arguments = listOf(navArgument("encodedClickedUri") {
//                                type = NavType.StringType
//                            })
//                        ) { backStackEntry ->
//                            DecorateScreen(
//                                navController,
//                                backStackEntry.arguments?.getString("encodedClickedUri") ?: "",
//                                closetViewModel
//                            )
//                        }
                        composable(
                            "decorate"
                        ) {
                            DecorateScreen(navController, "", closetViewModel)
                        }
                        composable(
                            "aiImgGen/{encodedClickedUri}/{clickedCategory}",
                            arguments = listOf(
                                navArgument("encodedClickedUri") { type = NavType.StringType },
                                navArgument("clickedCategory") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            AiImgGenScreen(
                                navController,
                                backStackEntry.arguments?.getString("encodedClickedUri") ?: "",
                                backStackEntry.arguments?.getString("clickedCategory") ?: "",
                                aiViewModel,
                                closetViewModel
                            )
                        }
                        composable(
                            "insert/{encodedClickedUri}",
                            arguments = listOf(
                                navArgument("encodedClickedUri") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            InsertScreen(
                                navController,
                                backStackEntry.arguments?.getString("encodedClickedUri") ?: "",
                                aiViewModel
                            )
                        }
                        composable(
                            "profile/{profileUri}",
                            arguments = listOf(navArgument("profileUri") {
                                type = NavType.StringType
                            })
                        ) { _ ->
                            ProfileScreen()
                        }
                    }
                }
            }
        }
    }
}

data class BottomNavItem(val route: String, val icon: ImageVector, val label: String)

@Composable
fun BottomNavigationBar(navController: NavController, viewModel: AiViewModel) {
    val items = listOf(
        BottomNavItem("sales", Icons.Default.Search, "Home"),
        BottomNavItem("", Icons.Default.List, "Closet"),
        BottomNavItem("decorate", Icons.Default.AddCircle, "SalesList"),
        BottomNavItem("", Icons.Default.MailOutline, "Message"),
        BottomNavItem("profile/{profileUri}", Icons.Default.AccountCircle, "Profile")
    )
    BottomNavigation {
        val currentRoute = currentRoute(navController)
        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}


@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

@Composable
fun shouldShowBottomBar(navController: NavController): Boolean {
    val currentRoute = currentRoute(navController)
    return currentRoute != "login"
}

@Composable
fun shouldShowTopBar(navController: NavController): Boolean {
    val currentRoute = currentRoute(navController)
    return currentRoute == "sales"
}
