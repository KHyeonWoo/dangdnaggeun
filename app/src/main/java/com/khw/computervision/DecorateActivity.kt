package com.khw.computervision

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.khw.computervision.ui.theme.ComputerVisionTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DecorateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComputerVisionTheme {
                DecorateScreen()
            }
        }
    }

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    fun DecorateScreen() {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            val context = LocalContext.current
            LogoScreen("Decorate")
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(1f))
                FunTextButton("저장") {
                    context.startActivity(Intent(context, InsertActivity::class.java))
                }
                Spacer(modifier = Modifier.padding(20.dp, 20.dp))
            }
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.character1),
                    contentDescription = "",
                    modifier = Modifier
                        .size(320.dp)
                )
            }
            Text(
                text = "판매 제품",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(colorDang)
                    .padding(top = 10.dp),
                textAlign = TextAlign.Center,
                color = Color.White,
            )
            val pages = listOf("상의", "하의")
            val pagerState = rememberPagerState()
            val coroutineScope = rememberCoroutineScope()

            CustomTabRow(pages, pagerState, coroutineScope)

        }
    }

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    private fun CustomTabRow(
        pages: List<String>,
        pagerState: PagerState,
        coroutineScope: CoroutineScope
    ) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                )
            },
            backgroundColor = Color.White,
            contentColor = colorDang
        ) {
            pages.forEachIndexed { index, title ->
                Tab(
                    text = {
                        Text(
                            text = title,
                            color = Color.White,
                            modifier = Modifier
                                .background(colorDang)
                                .height(20.dp)
                        )
                    },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.scrollToPage(index)
                        }
                    }
                )
            }
        }
        HorizontalPager(
            count = pages.size,
            state = pagerState,
        ) { page ->
            Text(
                modifier = Modifier.wrapContentSize(),
                text = page.toString(),
                textAlign = TextAlign.Center,
                fontSize = 30.sp
            )
        }

    }

}