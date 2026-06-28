package com.tonihacks.qalam.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.tonihacks.qalam.navigation.Destination
import com.tonihacks.qalam.navigation.Home
import com.tonihacks.qalam.navigation.RootList
import com.tonihacks.qalam.navigation.TextList
import com.tonihacks.qalam.navigation.WordList
import com.tonihacks.qalam.ui.theme.QalamInk3
import com.tonihacks.qalam.ui.theme.QalamPaper
import com.tonihacks.qalam.ui.theme.QalamPrimary
import com.tonihacks.qalam.ui.theme.QalamPrimaryC

private data class NavItem(
    val destination: Destination,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val navItems = listOf(
    NavItem(Home, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    NavItem(WordList, "Words", Icons.Filled.School, Icons.Outlined.School),
    NavItem(RootList, "Roots", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook),
    NavItem(TextList, "Texts", Icons.AutoMirrored.Filled.TextSnippet, Icons.AutoMirrored.Outlined.TextSnippet),
)


@Composable
fun QalamBottomNav(
    currentDst: Any?,
    onNavigate: (Destination) -> Unit
) {
    NavigationBar(containerColor = QalamPaper) {
        val colors = NavigationBarItemDefaults.colors(
            selectedIconColor = QalamPrimary,
            selectedTextColor = QalamPrimary,
            indicatorColor = QalamPrimaryC,
            unselectedIconColor = QalamInk3,
            unselectedTextColor = QalamInk3
        )
        navItems.forEach { item ->
            val selected = currentDst == item.destination
            val iconImageVector = if (selected) item.selectedIcon else item.unselectedIcon
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.destination) },
                icon = { Icon(imageVector = iconImageVector, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = colors
            )
        }
    }
}