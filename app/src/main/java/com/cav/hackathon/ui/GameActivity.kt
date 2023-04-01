package com.cav.hackathon.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.cav.hackathon.models.Question
import com.cav.hackathon.models.Session
import com.cav.hackathon.ui.theme.HackathonCSTTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameActivity : ComponentActivity() {

    private var isHost = mutableStateOf(false)
    private var isLoading = mutableStateOf(false)
    private var currentSession = mutableStateOf(Session())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        val sessionCollection = Firebase.firestore.collection("sessions")
        isHost.value = intent.getBooleanExtra("isHost", false)
        val questions = listOf(
            Question("intrebare 1", listOf("a", "b", "c", "d"), "a"),
            Question("intrebare 1", listOf("a", "b", "c", "d"), "a"),
            Question("intrebare 1", listOf("a", "b", "c", "d"), "a"),
            Question("intrebare 1", listOf("a", "b", "c", "d"), "a"),
            Question("intrebare 1", listOf("a", "b", "c", "d"), "a"),
            Question("intrebare 1", listOf("a", "b", "c", "d"), "a"),
            Question("intrebare 1", listOf("a", "b", "c", "d"), "a"),
            Question("intrebare 1", listOf("a", "b", "c", "d"), "a"),
            Question("intrebare 1", listOf("a", "b", "c", "d"), "a"),
            Question("intrebare 1", listOf("a", "b", "c", "d"), "a"),
        )

        if (isHost.value) {
            val session = Session(
                generateRandomString(),
                auth.currentUser!!.uid,
                questions = questions
            )

            currentSession.value = session

            CoroutineScope(Dispatchers.IO).launch {
                sessionCollection.add(session).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        isLoading.value = false
                    }
                }
            }

            lifecycleScope.launch {
                // Log.d("sessionCode",currentSession.value.sessionCode.toString())
                sessionCollection.whereEqualTo("sessionCode", currentSession.value.sessionCode)
                    .addSnapshotListener { value, error ->
                        if (error == null) {
                            if (value != null) {
                                currentSession.value =
                                    value.documents[0].toObject(Session::class.java)!!
                            }
                        } else {
                            Toast.makeText(
                                this@GameActivity,
                                "There was an error creating the session",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                sessionCollection.whereEqualTo("sessionCode" ,intent.getStringExtra("sessionCode")).get().addOnCompleteListener { task ->
                    if(task.isSuccessful ) {
                        currentSession.value = task.result.documents[0].toObject(Session::class.java)!!
                        val newParticipants = currentSession.value.participants.toMutableList()
                        newParticipants.add("Test")
                        sessionCollection.document(task.result.documents[0].reference.id).update(mapOf(
                            "participants" to newParticipants
                        )).addOnSuccessListener {
                            // Log.d("TestSuccess","Worked")
                            currentSession.value = currentSession.value.copy(participants = newParticipants)
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(text = "Session code", fontSize = 30.sp)
                        Spacer(Modifier.height(0.dp))
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = currentSession.value.sessionCode,
                                    fontSize = 48.sp,
                                    modifier = Modifier.padding(24.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(40.dp))
                        ListOfParticipants(currentSession)

                        if (isHost.value) {
                            Spacer(Modifier.height(40.dp))
                            Button(
                                onClick = {},
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    text = "Start Game",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                    }
                }
            }
        }
    }

    fun generateRandomString(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = Random(System.currentTimeMillis())
        return (1..4).map { chars[random.nextInt(chars.length)] }.joinToString("")
    }
}

@Composable
fun ListOfParticipants(currentSession: MutableState<Session>) {
    var session by currentSession

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(session.participants) { element ->
            ParticipantItem(name = element)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantItem(name: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = name,
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}