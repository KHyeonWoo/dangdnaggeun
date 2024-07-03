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
import androidx.compose.material.TopAppBar
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
                val viewModel: SharedViewModel = viewModel()
                Scaffold(
                    topBar = {
                        if (shouldShowTopBar(navController)) {
                            LogoScreen("Detail") { navController.popBackStack() }
                        }
                    },
                    bottomBar = {
                        if (shouldShowBottomBar(navController)) {
                            BottomNavigationBar(navController)
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") { LoginScreen(navController) }
                        composable("sales") { SaleScreen(navController) }
                        composable(
                            "detailProduct/{productId}",
                            arguments = listOf(navArgument("productId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            DetailScreen(navController, backStackEntry.arguments?.getString("productId"))
                        }
                        composable(
                            "decorate/{encodedClickedUri}",
                            arguments = listOf(navArgument("encodedClickedUri") { type = NavType.StringType })
                        ) { backStackEntry ->
                            DecorateScreen(navController, backStackEntry.arguments?.getString("encodedClickedUri") ?: "")
                        }
                        composable(
                            "decorate"
                        ) {
                            DecorateScreen(navController, "")
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
                                viewModel
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
                                viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

data class BottomNavItem(val route: String, val icon: ImageVector, val label: String)

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("login", Icons.Default.Search, "Home"), //판매리스트
        BottomNavItem("sales", Icons.Default.List, "SalesList"), //옷장
        BottomNavItem("decorate", Icons.Default.AddCircle, "SalesList"), //글쓰기
        BottomNavItem("", Icons.Default.MailOutline, "Message"), //메일or채팅
        BottomNavItem("", Icons.Default.AccountCircle, "Profile") //프로필
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
                        // 시작 목적지로 팝업하여 큰 네비게이션 스택을 피합니다.
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // 동일한 항목을 다시 선택할 때 동일한 목적지의 여러 복사본을 피합니다.
                        launchSingleTop = true
                        // 이전에 선택된 항목을 다시 선택할 때 상태를 복원합니다.
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
