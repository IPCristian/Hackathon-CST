package com.cav.hackathon.models

data class Session(
    var sessionCode: String = "",
    var hostID: String = "",
    var participants: List<String> = listOf(),
    var isStarted: Boolean = false,
    var isFinished: Boolean = false,
    var questions: List<Question> = listOf()
)
