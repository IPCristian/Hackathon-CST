package com.cav.hackathon.ui

import android.os.Bundle
import android.widget.Space
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
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

    private var isHost: Boolean = false
    private var isLoading = mutableStateOf(false)
    private var currentSession = mutableStateOf(Session())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        val sessionCollection = Firebase.firestore.collection("sessions")
        isHost = intent.getBooleanExtra("isHost", false)
        val questions = listOf(
            Question("intrebare 1", listOf("a","b","c", "d"), "a"),
            Question("intrebare 1", listOf("a","b","c", "d"), "a"),
            Question("intrebare 1", listOf("a","b","c", "d"), "a"),
            Question("intrebare 1", listOf("a","b","c", "d"), "a"),
            Question("intrebare 1", listOf("a","b","c", "d"), "a"),
            Question("intrebare 1", listOf("a","b","c", "d"), "a"),
            Question("intrebare 1", listOf("a","b","c", "d"), "a"),
            Question("intrebare 1", listOf("a","b","c", "d"), "a"),
            Question("intrebare 1", listOf("a","b","c", "d"), "a"),
            Question("intrebare 1", listOf("a","b","c", "d"), "a"),
        )

        if (isHost) {
            val session = Session(
                generateRandomString(),
                auth.currentUser!!.uid,
                questions = questions
            )

            currentSession.value = session

            CoroutineScope(Dispatchers.IO).launch {
                sessionCollection.add(session).addOnCompleteListener {task ->
                    if (task.isSuccessful) {
                        isLoading.value = false
                    }
                }
            }

            lifecycleScope.launch {
                sessionCollection.whereEqualTo("sessionCode", currentSession.value.sessionCode).addSnapshotListener { value, error ->
                    if (error == null) {
                        if (value != null) {
                            currentSession.value = value.documents[0].toObject(Session::class.java)!!
                        }
                    } else {
                        Toast.makeText(this@GameActivity, "There was an error creating the session", Toast.LENGTH_SHORT).show()
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
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(text = "Session code", fontSize = 30.sp)
                        Spacer(Modifier.height(0.dp))
                        Surface(shape = RoundedCornerShape(24.dp), border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = currentSession.value.sessionCode, fontSize = 48.sp, modifier = Modifier.padding(24.dp))
                            }
                        }
                        ListOfParticipants(items = listOf("Item 1", "Item 2", "Item 3"))
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
fun ListOfParticipants(items: List<String>) {
    LazyColumn {
        items(items) { item ->
            Text(text = item, fontSize = 24.sp)
        }
    }
}