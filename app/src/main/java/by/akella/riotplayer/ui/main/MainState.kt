package by.akella.riotplayer.ui.main

import by.akella.riotplayer.ui.model.SongUiModel

class MainState (
    val loading: Boolean = false,
    val songs: List<SongUiModel> = listOf()
)