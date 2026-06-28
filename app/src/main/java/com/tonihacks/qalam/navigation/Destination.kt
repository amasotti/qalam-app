package com.tonihacks.qalam.navigation

data object Home : Destination
data object WordList : Destination
data object RootList : Destination
data object TextList    : Destination
data class  WordDetail(val wordId: String)   : Destination
data class  RootDetail(val rootId: String)   : Destination
data class  TextDetail(val textId: String)   : Destination
data object Training    : Destination
data object TrainingSummary : Destination

sealed interface Destination