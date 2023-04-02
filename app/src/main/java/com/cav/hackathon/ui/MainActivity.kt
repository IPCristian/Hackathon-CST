package com.cav.hackathon.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import com.cav.hackathon.R
import com.cav.hackathon.models.User
import com.cav.hackathon.ui.theme.HackathonCSTTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sessionCollection = Firebase.firestore.collection("sessions")
        val userCollection = Firebase.firestore.collection("users")
        val auth = FirebaseAuth.getInstance()
        var crtUser = mutableStateOf(User())

        userCollection.whereEqualTo("userUID", auth.currentUser!!.uid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    crtUser.value = task.result.documents[0].toObject(User::class.java)!!
                }

                setContent {
                    HackathonCSTTheme {
                        val selectedPage = remember { mutableStateOf(1) }
                        val context = LocalContext.current
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            Column(
                                modifier = Modifier.align(Alignment.TopCenter)
                            ) {
                                WaveHeader()
                                when (selectedPage.value) {
                                    1 -> GameMenu(
                                        onHostGameClicked = {
                                            startActivity(
                                                Intent(
                                                    context,
                                                    GameActivity::class.java
                                                ).putExtra("isHost", true)
                                            )
                                        },
                                        onJoinGameClicked = {
                                            sessionCollection.get()
                                                .addOnSuccessListener { querySnapshot ->
                                                    for (document in querySnapshot) {
                                                        val data = document.getString("sessionCode")
                                                        if (data == it)
                                                            startActivity(
                                                                Intent(
                                                                    context,
                                                                    GameActivity::class.java
                                                                ).putExtra("sessionCode", data)
                                                            )
                                                    }
                                                }
                                                .addOnFailureListener { exception ->
                                                    Log.e("Error", exception.stackTrace.toString())
                                                }
                                        })

                                    2 -> StatsMenu(crtUser)
                                }
                            }
                            BottomNavigation(
                                selectedPage = selectedPage,
                                Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }
                }
            }
    }
}

@Composable
fun BottomNavigation(
    selectedPage: MutableState<Int>,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .height(80.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier
                .height(40.dp)
                .background(MaterialTheme.colorScheme.primary))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable {
                        selectedPage.value = 1
                    }) {
                Icon(
                    imageVector = Icons.Filled.SportsEsports,
                    contentDescription = "Play",
                    tint = if (selectedPage.value == 1) MaterialTheme.colorScheme.primary else Color.Gray
                )
                if (selectedPage.value == 1) {
                    Text(text = "Play", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp)
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable {
                        selectedPage.value = 2
                    }) {
                Icon(
                    imageVector = Icons.Filled.Analytics,
                    contentDescription = "Stats",
                    tint = if (selectedPage.value == 2) MaterialTheme.colorScheme.primary else Color.Gray
                )
                if (selectedPage.value == 2) {
                    Text(
                        text = "Stats",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun GameMenu(
    onHostGameClicked: () -> Unit,
    onJoinGameClicked: (String) -> Unit
) {
    var codeInput by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Image(painter = painterResource(id = R.drawable.logo) , contentDescription = null,
                Modifier
                    .size(250.dp)
                    .align(Alignment.CenterHorizontally))
            Button(
                onClick = onHostGameClicked,
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Host Game", fontSize = 16.sp, modifier = Modifier.padding(16.dp))
            }

            Spacer(modifier = Modifier.height(80.dp))

            OutlinedTextField(
                value = codeInput,
                onValueChange = { codeInput = it.take(4) },
                shape = RoundedCornerShape(24.dp),
                label = { Text("Game Code") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = { onJoinGameClicked(codeInput) },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .align(Alignment.CenterHorizontally),
                enabled = codeInput.length == 4
            ) {
                Text("Join Game with Code", fontSize = 16.sp, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsMenu(user: MutableState<User>) {

    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                Modifier
                    .size(250.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Card(
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
            ){
                Text(
                    text = "Games played: ${user.value.nrOfGames}",
                    fontSize = 24.sp,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Games won: ${user.value.nrOfWins}",
                    fontSize = 24.sp,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    context.startActivity(Intent(context,AuthenticateActivity::class.java))
                    (context as Activity).finish()
                }
            ) {
                Text(text = "Sign out", modifier = Modifier.padding(16.dp))
            }
        }
    }
}