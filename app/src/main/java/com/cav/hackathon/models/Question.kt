package com.cav.hackathon.models

data class Question(
    var question: String = "",
    var possibleAnswers: List<String> = listOf(),
    var correctAnswer: String = ""
)
