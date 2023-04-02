package com.cav.hackathon.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cav.hackathon.models.Session
import com.cav.hackathon.ui.theme.HackathonCSTTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class QuizActivity : ComponentActivity() {

    val currentSession = mutableStateOf(Session())
    var timeRemaining by mutableStateOf(10)
    var selectedAnswer by mutableStateOf("")
    var currentQuestion by mutableStateOf(0)
    var numberOfCorrectAnswers = 0

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionCollection = Firebase.firestore.collection("sessions")
        val auth = FirebaseAuth.getInstance()

        sessionCollection.whereEqualTo("sessionCode", intent.getStringExtra("currentSession"))
            .addSnapshotListener { value, error ->
                if (error == null && value != null) {
                    value.documents[0]?.let {
                        currentSession.value = it.toObject(Session::class.java)!!
                    }
                    if (currentSession.value.isFinished) {
                        /* fa ceva */
                    }
                }
            }

        CoroutineScope(Dispatchers.Default).launch {
            while (timeRemaining > 0) {
                if (currentSession.value.questions.isNotEmpty())
                {
                    delay(1000)
                    timeRemaining -= 1
                    if (timeRemaining == 0) {
                        if (selectedAnswer == currentSession.value.questions[currentQuestion].correctAnswer) {
                            numberOfCorrectAnswers += 1
                        }
                        selectedAnswer = ""
                        if (currentQuestion < currentSession.value.questions.size - 1) {
                            currentQuestion += 1
                            timeRemaining = 10
                        } else {
                            sessionCollection.whereEqualTo("sessionCode", currentSession.value.sessionCode).get().addOnCompleteListener { task ->
                                if (task.isSuccessful) {

                                }
                            }
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@QuizActivity,
                                    numberOfCorrectAnswers.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
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
                    if (currentSession.value.questions.size != 0) {
                        Column(Modifier.fillMaxSize()) {
                            Spacer(Modifier.height(60.dp))
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .align(Alignment.CenterHorizontally)
                            ) {


                                CircularProgressIndicator(
                                    progress = timeRemaining.toFloat() / 10,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(500.dp)
                                )
                                Text(
                                    text = timeRemaining.toString(),
                                    fontSize = 24.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }

                            Spacer(Modifier.height(50.dp))

                            Text(
                                text = currentSession.value.questions[currentQuestion].question,
                                fontSize = 24.sp,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )

                            Spacer(Modifier.height(150.dp))

                            Row(Modifier.fillMaxWidth()) {
                                Card(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .weight(1f)
                                        .height(150.dp),
                                    colors = CardDefaults.cardColors(Color.Red, Color.White),
                                    border = if (selectedAnswer == currentSession.value.questions[currentQuestion].possibleAnswers[0]) BorderStroke(4.dp, MaterialTheme.colorScheme.secondary) else null,
                                    onClick = {
                                        selectedAnswer = currentSession.value.questions[currentQuestion].possibleAnswers[0]
                                    }
                                ) {
                                    Box(Modifier.fillMaxSize())
                                    {
                                        Text(
                                            text = currentSession.value.questions[currentQuestion].possibleAnswers[0],
                                            fontSize = 24.sp,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }

                                Card(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .weight(1f)
                                        .height(150.dp),
                                    colors = CardDefaults.cardColors(Color.Blue, Color.White),
                                    border = if (selectedAnswer == currentSession.value.questions[currentQuestion].possibleAnswers[1]) BorderStroke(4.dp, MaterialTheme.colorScheme.secondary) else null,
                                    onClick = {
                                        selectedAnswer = currentSession.value.questions[currentQuestion].possibleAnswers[1]
                                    }
                                ) {
                                    Box(Modifier.fillMaxSize())
                                    {
                                        Text(
                                            text = currentSession.value.questions[currentQuestion].possibleAnswers[1],
                                            fontSize = 24.sp,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                            }

                            Row(Modifier.fillMaxWidth()) {
                                Card(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .weight(1f)
                                        .height(150.dp),
                                    colors = CardDefaults.cardColors(Color.Green, Color.White),
                                    border = if (selectedAnswer == currentSession.value.questions[currentQuestion].possibleAnswers[2]) BorderStroke(4.dp, MaterialTheme.colorScheme.secondary) else null,
                                    onClick = {
                                        selectedAnswer = currentSession.value.questions[currentQuestion].possibleAnswers[2]
                                    }
                                ) {
                                    Box(Modifier.fillMaxSize())
                                    {
                                        Text(
                                            text = currentSession.value.questions[currentQuestion].possibleAnswers[2],
                                            fontSize = 24.sp,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }

                                Card(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .weight(1f)
                                        .height(150.dp),
                                    colors = CardDefaults.cardColors(Color.Yellow, Color.White),
                                    border = if (selectedAnswer == currentSession.value.questions[currentQuestion].possibleAnswers[3]) BorderStroke(4.dp, MaterialTheme.colorScheme.secondary) else null,
                                    onClick = {
                                        selectedAnswer = currentSession.value.questions[currentQuestion].possibleAnswers[3]
                                    }
                                ) {
                                    Box(Modifier.fillMaxSize())
                                    {
                                        Text(
                                            text = currentSession.value.questions[currentQuestion].possibleAnswers[3],
                                            fontSize = 24.sp,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}