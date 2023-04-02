package com.cav.hackathon.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.cav.hackathon.models.Question
import com.cav.hackathon.models.Session
import com.cav.hackathon.models.User
import com.cav.hackathon.ui.theme.HackathonCSTTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
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
        val userCollection = Firebase.firestore.collection("users")
        isHost.value = intent.getBooleanExtra("isHost", false)
        val questions = listOf(
            Question("intrebare 1 - asta este foarte lunga", listOf("a este probabil bun", "b nu minte niciodata", "c este posibil", "nu cred ca este d"), "a este probabil bun"),
            Question("intrebare 2", listOf("a", "b", "c", "d"), "a"),
            Question("intrebare 3", listOf("a", "b", "c", "d"), "a"),
            Question("intrebare 4", listOf("a", "b", "c", "d"), "a"),
            Question("intrebare 5", listOf("a", "b", "c", "d"), "a"),
            Question("intrebare 6", listOf("a", "b", "c", "d"), "a"),
            Question("intrebare 7", listOf("a", "b", "c", "d"), "a"),
            Question("intrebare 8", listOf("a", "b", "c", "d"), "a"),
            Question("intrebare 9", listOf("a", "b", "c", "d"), "a"),
            Question("intrebare 10", listOf("a", "b", "c", "d"), "a"),
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
        }
        else {
            lifecycleScope.launch {

                Log.d("TestSuccess",currentSession.value.participants.toString())

                var crtUser = ""
                userCollection.whereEqualTo("userUID",auth.currentUser!!.uid).get().addOnCompleteListener { task ->
                            if (task.isSuccessful)
                            {
                                crtUser = task.result.documents[0].toObject(User::class.java)!!.displayName
                            }
                        }

                sessionCollection.whereEqualTo("sessionCode" ,intent.getStringExtra("sessionCode")).get().addOnCompleteListener { task ->
                    if(task.isSuccessful ) {
                        currentSession.value = task.result.documents[0].toObject(Session::class.java)!!
                        val newParticipants = currentSession.value.participants.toMutableList()
                        newParticipants.add(crtUser)
                        sessionCollection.document(task.result.documents[0].reference.id).update(mapOf(
                            "participants" to newParticipants
                        )).addOnSuccessListener { currentSession.value.participants = newParticipants }
                    }
                    else{
                        Log.d("TestSuccess","Failed")
                    }
                }

                sessionCollection.whereEqualTo("sessionCode" ,intent.getStringExtra("sessionCode")).addSnapshotListener { value, error ->
                    if (error == null)
                    {
                        if (value != null)
                        {
                            currentSession.value = value.documents[0].toObject(Session::class.java)!!
                            if (currentSession.value.isStarted) {
                                startActivity(Intent(this@GameActivity, QuizActivity::class.java).putExtra("currentSession", currentSession.value.sessionCode).putExtra("isHost", isHost.value))
                                finish()
                            }
                        }
                    }

                }
            }
        }

        setContent {
            HackathonCSTTheme {
                val context = LocalContext.current
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
                        ListOfParticipants(currentSession.value.participants)

                        if (isHost.value) {
                            Spacer(Modifier.height(40.dp))
                            Button(
                                onClick = {
                                    sessionCollection.whereEqualTo("sessionCode" , currentSession.value.sessionCode).get().addOnCompleteListener { task ->
                                        if(task.isSuccessful ) {
                                            currentSession.value = task.result.documents[0].toObject(Session::class.java)!!
                                            sessionCollection.document(task.result.documents[0].reference.id).update(mapOf(
                                                "started" to true
                                            ))
                                            startActivity(Intent(context, QuizActivity::class.java).putExtra("currentSession", currentSession.value.sessionCode).putExtra("isHost", isHost.value))
                                            finish()
                                        }
                                    }
                                },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListOfParticipants(stringList: List<String>) {
    LazyColumn {
        items(stringList) { item ->
            Card(shape = RoundedCornerShape(24.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)) {

                Text(text = item, fontSize = 24.sp, modifier = Modifier.fillMaxWidth(0.8f).padding(16.dp).align(Alignment.CenterHorizontally))
            }
        }
    }
}