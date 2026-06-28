package com.tonihacks.qalam

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

import com.tonihacks.qalam.navigation.Home
import com.tonihacks.qalam.navigation.MainNavDisplay
import com.tonihacks.qalam.navigation.RootList
import com.tonihacks.qalam.navigation.TextList
import com.tonihacks.qalam.navigation.WordList
import com.tonihacks.qalam.ui.components.QalamBottomNav
import com.tonihacks.qalam.ui.theme.QalamBg
import com.tonihacks.qalam.ui.theme.QalamTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent {
            QalamTheme {
                val backStack = remember { mutableStateListOf<Any>(Home) }
                Scaffold(
                    containerColor = QalamBg,
                    bottomBar = {
                        QalamBottomNav(
                            currentDst = backStack.lastOrNull(),
                            onNavigate = { dest ->
                                // Tab switch: replace the top of the stack with the new root tab.
                                // This prevents stacking Home→Words→Home→Words ad infinitum.
                                val tabRoots = listOf(
                                    Home, WordList,
                                    RootList, TextList
                                )
                                if (dest != backStack.lastOrNull()) {
                                    backStack.removeAll { it in tabRoots }
                                    backStack.add(dest)
                                }
                            },
                        )
                    },
                ) { innerPadding ->
                    MainNavDisplay(
                        backStack = backStack,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
