package com.khw.computervision

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import com.khw.computervision.ui.theme.ComputerVisionTheme
import java.net.URLEncoder

class AppNavigator : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComputerVisionTheme {
                val navController = rememberNavController()
                val aiViewModel: AiViewModel = viewModel() // aiViewModel 인스턴스 생성
                val closetViewModel: ClosetViewModel = viewModel() // closetViewModel 인스턴스 생성
                val productsViewModel: ProductViewModel = viewModel() // productsViewModel 인스턴스 생성

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
                        composable("closet") {
                            CustomImageGridPage(
                                closetViewModel = closetViewModel,
                                onBackClick = {
                                    // 뒤로 가기 로직 추가
                                    navController.popBackStack()
                                },
                                navController
                            )
                        }
                        composable("login") {
                            LoginScreen(
                                navController,
                                closetViewModel,
                                productsViewModel
                            )
                        }
                        composable("sales") { SaleScreen(navController, productsViewModel) }
                        composable(
                            "detailProduct/{productKey}",
                            arguments = listOf(navArgument("productKey") {
                                type = NavType.StringType
                            })
                        ) { backStackEntry ->
                            DetailScreen(
                                navController,
                                productsViewModel,
                                backStackEntry.arguments?.getString("productKey")
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
                            "aiImgGen/{encodedClickedUrl}/{clickedCategory}",
                            arguments = listOf(
                                navArgument("encodedClickedUrl") { type = NavType.StringType },
                                navArgument("clickedCategory") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            AiImgGenScreen(
                                navController,
                                backStackEntry.arguments?.getString("encodedClickedUrl") ?: "",
                                backStackEntry.arguments?.getString("clickedCategory") ?: "",
                                aiViewModel,
                                closetViewModel
                            )
                        }
                        composable(
                            "insert/{encodedClickedUrl}/{clickedCategory}",
                            arguments = listOf(
                                navArgument("encodedClickedUrl") { type = NavType.StringType },
                                navArgument("clickedCategory") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            InsertScreen(
                                navController,
                                backStackEntry.arguments?.getString("encodedClickedUrl") ?: "",
                                backStackEntry.arguments?.getString("clickedCategory") ?: "",
                                aiViewModel,
                                productsViewModel
                            )
                        }
                        composable(
                            "profile/{profileUrl}",
                            arguments = listOf(navArgument("profileUrl") {
                                type = NavType.StringType
                            })
                        ) { _ ->
                            ProfileScreen()
                        }
                        composable("messageList") {
                            val messageMap = getMessage()
                            MessageScreen(messageMap, "User's Image")
                        }
                    }
                }
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: ImageVector? = null,
    val iconPainter: Painter? = null,
    val label: String
)

@Composable
fun BottomNavigationBar(navController: NavController, viewModel: AiViewModel) {

    val items = listOf(
        BottomNavItem("sales", icon = Icons.Default.Home, iconPainter = null, "홈"),
        BottomNavItem("closet",icon = null, iconPainter = painterResource(id = R.drawable.closet_icon), "옷장"),
        BottomNavItem("decorate", icon = Icons.Default.AddCircle, iconPainter = null, "판매글 등록"),
        BottomNavItem("messageList", icon = Icons.Default.MailOutline, iconPainter = null, "메시지"),
        BottomNavItem("profile/{profileUrl}", icon = Icons.Default.Person, iconPainter = null, "프로필")
    )

    BottomNavigation(
        backgroundColor = Color.White,
        modifier = Modifier
            .height(60.dp)
            .border(
                2.dp,
                color = colorDang,
                shape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp)
            )
    ) {
        val currentRoute = currentRoute(navController)
        items.forEach { item ->
            BottomNavigationItem(
                icon = {
                    when {
                        item.icon != null -> Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(30.dp)
                        )

                        item.iconPainter != null -> Icon(
                            painter = item.iconPainter,
                            contentDescription = item.label,
                            modifier = Modifier.padding(3.dp).size(25.dp)
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
                selectedContentColor = colorDang,
                unselectedContentColor = Color.Black.copy(alpha = .5f),
                selected = currentRoute == item.route,
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
                        .weight(1.5f) // Adjust the size for "판매글 등록"
                } else {
                    Modifier.padding(5.dp)
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

fun parseMessageMap(messageMapString: String): Map<String, String> {
    return try {
        Gson().fromJson(messageMapString, Map::class.java) as Map<String, String>
    } catch (e: Exception) {
        emptyMap()
    }
}

@Composable
fun getMessage(): Map<String, String> {
    val context = LocalContext.current
    val messageMap = produceState<Map<String, String>>(initialValue = emptyMap()) {
        Firebase.firestore.collection(UserIDManager.userID.value)
            .get()
            .addOnSuccessListener { result ->
                value = result.documents.associate {
                    it.id to "보낸일시: ${
                        it.getString("date").orEmpty()
                    }\n보낸사람: ${it.getString("sendUser").orEmpty()}\n메세지: ${
                        it.getString("message").orEmpty()
                    }"
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
            }
    }
    return messageMap.value
}