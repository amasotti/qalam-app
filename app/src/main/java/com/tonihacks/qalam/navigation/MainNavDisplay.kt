package com.tonihacks.qalam.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.tonihacks.qalam.ui.exercise.ExerciseRoute
import com.tonihacks.qalam.ui.home.HomeScreen
import com.tonihacks.qalam.ui.practice.PracticeScreen
import com.tonihacks.qalam.ui.productionpractice.ProductionPracticeRoute
import com.tonihacks.qalam.ui.roots.RootDetailScreen
import com.tonihacks.qalam.ui.roots.RootListScreen
import com.tonihacks.qalam.ui.settings.SettingsScreen
import com.tonihacks.qalam.ui.texts.TextListScreen
import com.tonihacks.qalam.ui.texts.TextReaderScreen
import com.tonihacks.qalam.ui.training.TrainingRoute
import com.tonihacks.qalam.ui.wordlists.WordListDetailScreen
import com.tonihacks.qalam.ui.wordlists.WordListsScreen
import com.tonihacks.qalam.ui.words.WordDetailScreen
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
            entry<Home> {
                HomeScreen(
                    onNavigateToSettings = { backStack.add(Settings) },
                    onStartPractice = { backStack.add(Practice) },
                    onNavigateToWords = { backStack.add(WordList) },
                    onNavigateToRoots = { backStack.add(RootList) },
                    onNavigateToTexts = { backStack.add(TextList) },
                    onNavigateToWord = { id -> backStack.add(WordDetail(id)) },
                )
            }
            entry<WordList> {
                WordListScreen(
                    onNavigateToWord = { id -> backStack.add(WordDetail(id)) },
                    onNavigateToLists = { backStack.add(VocabularyLists) },
                )
            }
            entry<VocabularyLists> {
                WordListsScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onNavigateToList = { id -> backStack.add(VocabularyListDetail(id)) },
                )
            }
            entry<VocabularyListDetail> { dest ->
                WordListDetailScreen(
                    listId = dest.listId,
                    onBack = { backStack.removeLastOrNull() },
                    onNavigateToWord = { id -> backStack.add(WordDetail(id)) },
                )
            }
            entry<WordDetail> { dest ->
                WordDetailScreen(
                    wordId = dest.wordId,
                    onBack = { backStack.removeLastOrNull() },
                    onNavigateToRoot = { id -> backStack.add(RootDetail(id)) },
                    onNavigateToWord = { id -> backStack.add(WordDetail(id)) },
                )
            }
            entry<RootList> {
                RootListScreen(onNavigateToRoot = { id -> backStack.add(RootDetail(id)) })
            }
            entry<RootDetail> { dest ->
                RootDetailScreen(
                    rootId = dest.rootId,
                    onBack = { backStack.removeLastOrNull() },
                    onNavigateToWord = { id -> backStack.add(WordDetail(id)) },
                )
            }
            entry<TextList> {
                TextListScreen(onNavigateToText = { id -> backStack.add(TextDetail(id)) })
            }
            entry<TextDetail> { dest ->
                TextReaderScreen(
                    textId = dest.textId,
                    onBack = { backStack.removeLastOrNull() },
                    onNavigateToWord = { id -> backStack.add(WordDetail(id)) },
                )
            }
            entry<Training> {
                TrainingRoute(onClose = { backStack.removeLastOrNull() })
            }
            entry<Practice> {
                PracticeScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onFlashcards = { backStack.add(Training) },
                    onMultipleChoice = { backStack.add(Exercise) },
                    onSentenceProduction = { backStack.add(ProductionPractice) },
                )
            }
            entry<Exercise> {
                ExerciseRoute(onClose = { backStack.removeLastOrNull() })
            }
            entry<ProductionPractice> {
                ProductionPracticeRoute(onClose = { backStack.removeLastOrNull() })
            }
            entry<Settings> { SettingsScreen(onBack = { backStack.removeLastOrNull() }) }
        }

    )
}
