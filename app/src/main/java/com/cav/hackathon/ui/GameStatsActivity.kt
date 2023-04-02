package com.cav.hackathon.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cav.hackathon.models.Session
import com.cav.hackathon.models.SessionScore
import com.cav.hackathon.models.User
import com.cav.hackathon.ui.theme.HackathonCSTTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.Integer.min

class GameStatsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var currentSessionScores = mutableStateOf(listOf<SessionScore>())
        val currentSession = mutableStateOf(Session())
        val sessionCollection = Firebase.firestore.collection("sessions")
        sessionCollection.whereEqualTo("sessionCode", intent.getStringExtra("sessionCode")).get().addOnCompleteListener {
            if (it.isSuccessful) {
                currentSession.value = it.result.documents[0].toObject(Session::class.java)!!
            }
        }
        val sessionScoreCollection = Firebase.firestore.collection("sessionScores")
        sessionScoreCollection.whereEqualTo("sessionCode", intent.getStringExtra("sessionCode")).get().addOnCompleteListener {
            if (it.isSuccessful) {
                currentSessionScores.value = it.result.documents.map {
                    it.toObject(SessionScore::class.java)!!
                }
                    .sortedByDescending { it.score }
            }
        }
        val users = mutableStateOf(listOf<User>())
        var crtUser by mutableStateOf(User())
        val userCollection = Firebase.firestore.collection("users")
        userCollection.get().addOnCompleteListener {
            if (it.isSuccessful) {
                users.value = it.result.documents
                    .map { it.toObject(User::class.java)!! }
            }
            crtUser = users.value.find {
                it.userUID == FirebaseAuth.getInstance().currentUser!!.uid
            }!!
            userCollection.whereEqualTo("userUID", crtUser.userUID).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    crtUser.nrOfGames += 1
                    if (currentSessionScores.value[0].userUID == crtUser.userUID) {
                        crtUser.nrOfWins += 1
                    }
                    userCollection.document(it.result.documents[0].id).update(
                        mapOf(
                            "nrOfGames" to crtUser.nrOfGames,
                            "nrOfWins" to crtUser.nrOfWins
                        )
                    )
                }
            }
        }

        setContent {
            HackathonCSTTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(36.dp))
                        Text(text = "Quiz Leaderboard", fontSize = 36.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(24.dp))
                        currentSessionScores.value.subList(0, min(10, currentSessionScores.value.size)).forEachIndexed { index, sessionScore ->
                            Text(text = "${index + 1}. ${users.value.find { it.userUID == sessionScore.userUID }!!.displayName}")
                        }
                    }
                }
            }
        }
    }
}