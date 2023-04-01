package com.cav.hackathon.models

data class User(
    var userUID: String = "",
    val displayName: String = "",
    var maxScore: Int = 0,
    var achievements: List<Achievement> = listOf(),
    var nrOfGames: Int = 0,
    var nrOfWins: Int = 0
)
