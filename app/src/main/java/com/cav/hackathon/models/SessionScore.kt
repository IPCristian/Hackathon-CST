package com.cav.hackathon.models

data class SessionScore(
    var sessionCode: String = "",
    var userUID: String = "",
    var score: Int = 0
)