package com.tonihacks.qalam.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.tonihacks.qalam.ui.home.HomeScreen
import com.tonihacks.qalam.ui.roots.RootListScreen
import com.tonihacks.qalam.ui.settings.SettingsScreen
import com.tonihacks.qalam.ui.texts.TextListScreen
import com.tonihacks.qalam.ui.words.WordListScreen

@Composable
fun MainNavDisplay(
    backStack: MutableList<Any>,
    modifier: Modifier = Modifier,
) {
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
        entryProvider = entryProvider {
            entry<Home>     { HomeScreen(onNavigateToSettings = { backStack.add(Settings) }) }
            entry<WordList> { WordListScreen() }
            entry<RootList> { RootListScreen() }
            entry<TextList> { TextListScreen() }
            entry<Settings> { SettingsScreen(onBack = { backStack.removeLastOrNull() }) }
        }

    )
}
