package com.khw.computervision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.khw.computervision.main.ClosetScreen
import com.khw.computervision.main.HomeScreen
import com.khw.computervision.chat.ChatListScreen
import com.khw.computervision.chat.MessageScreen
import com.khw.computervision.login.LoginScreen
import com.khw.computervision.login.SignUpScreen
import com.khw.computervision.uploadProduct.AiImgGenScreen
import com.khw.computervision.profile.MyLikedScreen
import com.khw.computervision.profile.MyUploadedScreen
import com.khw.computervision.profile.ProfileScreen
import com.khw.computervision.ui.theme.ComputerVisionTheme
import com.khw.computervision.uploadProduct.DetailScreen
import com.khw.computervision.uploadProduct.InsertScreen
import com.khw.computervision.uploadProduct.SalesScreen
import java.net.URLEncoder

class AppNavigator : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComputerVisionTheme {
                val navController = rememberNavController() as NavHostController
                val aiViewModel: AiViewModel = viewModel()
                val closetViewModel: ClosetViewModel = viewModel()
                val productsViewModel: ProductViewModel = viewModel()
                val chatViewModel: ChatViewModel = viewModel()
                val salesViewModel: SalesViewModel = viewModel()

                Scaffold(
                    topBar = {},
                    bottomBar = {
                        if (shouldShowBottomBar(navController)) {
                            BottomNavigationBar(navController)
                        }
                    }
                ) { innerPadding ->
                    NavigationHost(
                        navController = navController,
                        innerPadding = innerPadding,
                        aiViewModel = aiViewModel,
                        closetViewModel = closetViewModel,
                        productsViewModel = productsViewModel,
                        chatViewModel = chatViewModel,
                        salesViewModel = salesViewModel
                    )
                }
            }
        }
    }

    @Composable
    private fun NavigationHost(
        navController: NavHostController,
        innerPadding: PaddingValues,
        aiViewModel: AiViewModel,
        closetViewModel: ClosetViewModel,
        productsViewModel: ProductViewModel,
        chatViewModel: ChatViewModel,
        salesViewModel: SalesViewModel
    ) {
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    navController,
                    closetViewModel,
                    productsViewModel
                )
            }
            composable("signUp") {
                SignUpScreen(navController)
            }
            composable("sales") {
                HomeScreen(navController, productsViewModel)
            }
            composable(
                "detailProduct/{productKey}",
                arguments = listOf(navArgument("productKey") { type = NavType.StringType })
            ) { backStackEntry ->
                DetailScreen(
                    navController,
                    productsViewModel,
                    backStackEntry.arguments?.getString("productKey")
                )
            }
            composable(
                "closet/{beforeScreen}",
                arguments = listOf(navArgument("beforeScreen") { type = NavType.StringType })
            ) { backStackEntry ->
                ClosetScreen(
                    closetViewModel,
                    onBackClick = { navController.popBackStack() },
                    navController,
                    salesViewModel,
                    backStackEntry.arguments?.getString("beforeScreen"),
                    "true"
                )
            }
            composable(
                "closet/{beforeScreen}/{backIconVisible}",
                arguments = listOf(
                    navArgument("beforeScreen") { type = NavType.StringType },
                    navArgument("backIconVisible") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                ClosetScreen(
                    closetViewModel,
                    onBackClick = { navController.popBackStack() },
                    navController,
                    salesViewModel,
                    backStackEntry.arguments?.getString("beforeScreen"),
                    backStackEntry.arguments?.getString("backIconVisible")
                )
            }
            composable(
                "decorate/{encodedClickedUrl}/{clickedCategory}",
                arguments = listOf(
                    navArgument("encodedClickedUrl") { type = NavType.StringType },
                    navArgument("clickedCategory") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                SalesScreen(
                    navController,
                    salesViewModel,
                    backStackEntry.arguments?.getString("encodedClickedUrl") ?: " ",
                    backStackEntry.arguments?.getString("clickedCategory") ?: " "
                )
            }
            composable(
                "aiImgGen/{encodedExtraClickedUrl}",
                arguments = listOf(navArgument("encodedExtraClickedUrl") { type = NavType.StringType })
            ) { backStackEntry ->
                AiImgGenScreen(
                    navController,
                    salesViewModel,
                    backStackEntry.arguments?.getString("encodedExtraClickedUrl") ?: "",
                    aiViewModel
                )
            }
            composable("insert") {
                InsertScreen(
                    navController,
                    aiViewModel,
                    productsViewModel,
                    salesViewModel
                )
            }
            composable(
                "profile/{profileUrl}",
                arguments = listOf(navArgument("profileUrl") { type = NavType.StringType })
            ) {
                ProfileScreen(navController)
            }
            composable("myUploaded") {
                MyUploadedScreen(navController, productsViewModel)
            }
            composable("myLiked") {
                MyLikedScreen(navController, productsViewModel)
            }
            composable("chatListScreen") {
                ChatListScreen(navController, chatViewModel)
            }
            composable(
                "messageScreen/{otherUserID}/{otherUserProfile}",
                arguments = listOf(
                    navArgument("otherUserID") { type = NavType.StringType },
                    navArgument("otherUserProfile") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                MessageScreen(
                    navController,
                    chatViewModel,
                    backStackEntry.arguments?.getString("otherUserID") ?: "",
                    backStackEntry.arguments?.getString("otherUserProfile") ?: ""
                )
            }
        }
    }

    @Composable
    private fun BottomNavigationBar(navController: NavHostController) {
        val items = listOf(
            BottomNavItem.RouteItem("sales", Icons.Default.Home, "홈"),
            BottomNavItem.PainterItem(
                "closet/bottomNav/false",
                painterResource(id = R.drawable.closet_icon),
                "옷장"
            ),
            BottomNavItem.RouteItem("decorate/ / ", Icons.Default.AddCircle, "판매글 등록"),
            BottomNavItem.RouteItem("chatListScreen", Icons.AutoMirrored.Filled.Send, "메세지"),
            BottomNavItem.RouteItem("profile/{profileUrl}", Icons.Default.Person, "프로필")
        )

        BottomNavigation(
            backgroundColor = Color(0xFFF9F4EE),
            modifier = Modifier
                .height(60.dp)
                .border(
                    2.dp,
                    color = Color(0xFFFFA500),
                    shape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp)
                )
        ) {
            val currentRoute = currentRoute(navController)
            items.forEach { item ->
                BottomNavigationItem(
                    icon = {
                        when (item) {
                            is BottomNavItem.RouteItem -> Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(30.dp)
                            )
                            is BottomNavItem.PainterItem -> Icon(
                                painter = item.iconPainter,
                                contentDescription = item.label,
                                modifier = Modifier
                                    .padding(3.dp)
                                    .size(25.dp)
                            )
                        }
                    },
                    label = {
                        Text(
                            item.label,
                            maxLines = if (item.label == "판매글 등록") Int.MAX_VALUE else 1,
                            fontSize = 10.sp
                        )
                    },
                    selectedContentColor = Color(0xFFFFA500),
                    unselectedContentColor = Color(0xFFD3D3D3).copy(alpha = .5f),
                    selected = when (item.route) {
                        "closet/bottomNav/false" -> currentRoute?.startsWith("closet") == true
                        "decorate/ / " -> currentRoute?.startsWith("decorate") == true
                        else -> currentRoute == item.route
                    },
                    onClick = {
                        val route = if (item.route.contains("{messageMap}")) {
                            val emptyMessageMap = emptyMap<String, String>()
                            val messageMapJson =
                                URLEncoder.encode(Gson().toJson(emptyMessageMap), "UTF-8")
                            item.route.replace("{messageMap}", messageMapJson)
                        } else {
                            item.route
                        }
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = if (item.label == "판매글 등록") {
                        Modifier
                            .padding(2.dp)
                            .weight(1.5f)
                    } else {
                        Modifier.padding(5.dp)
                    }
                )
            }
        }
    }

    @Composable
    private fun currentRoute(navController: NavHostController): String? {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        return navBackStackEntry?.destination?.route
    }

    @Composable
    private fun shouldShowBottomBar(navController: NavHostController): Boolean {
        val currentRoute = currentRoute(navController)
        val routesToShowBottomBar = listOf(
            "sales",
            "closet/{beforeScreen}/{backIconVisible}",
            "decorate/{encodedClickedUrl}/{clickedCategory}",
            "chatListScreen",
            "profile/{profileUrl}"
        )
        return routesToShowBottomBar.any { currentRoute?.startsWith(it.substringBefore('/')) == true }
    }
}

sealed class BottomNavItem(val route: String, val label: String) {
    class RouteItem(route: String, val icon: ImageVector, label: String) : BottomNavItem(route, label)
    class PainterItem(route: String, val iconPainter: Painter, label: String) : BottomNavItem(route, label)
}
