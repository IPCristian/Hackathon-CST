package com.cav.hackathon.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cav.hackathon.R
import com.cav.hackathon.models.User
import com.cav.hackathon.ui.theme.HackathonCSTTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AuthenticateActivity : ComponentActivity() {

    val signUp: (String, String, String) -> Unit = { fullName, email, password ->
        val auth = FirebaseAuth.getInstance()
        val usersCollection = Firebase.firestore.collection("users")
        try {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        auth.currentUser?.let {
                            val newUser = User(it.uid, fullName, 0, listOf(), 0, 0)
                            try {
                                usersCollection.add(newUser)
                            } catch (e: Exception) {
                                Toast.makeText(this, getString(R.string.Error), Toast.LENGTH_SHORT).show()
                            }
                        }
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        val exception = task.exception
                        Toast.makeText(this, getString(R.string.Error), Toast.LENGTH_SHORT).show()
                        Log.e("SignUpError", exception!!.printStackTrace().toString())
                    }
                }
        } catch(e: Exception) {
            Toast.makeText(this, getString(R.string.Error), Toast.LENGTH_SHORT).show()
            Log.e("SignUpError", e.printStackTrace().toString())
        }
    }

    val login: (String, String) -> Unit = { email, password ->
        val auth = FirebaseAuth.getInstance()
        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        val exception = task.exception
                        Toast.makeText(this, getString(R.string.Error), Toast.LENGTH_SHORT).show()
                        Log.e("SignUpError", exception!!.printStackTrace().toString())
                    }
                }
        } catch(e: Exception) {
            Toast.makeText(this, getString(R.string.Error), Toast.LENGTH_SHORT).show()
            Log.e("SignUpError", e.printStackTrace().toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        setContent {
            HackathonCSTTheme {
                // State of Sign Up / Log In component
                var isOnLoginPage = rememberSaveable() {
                    mutableStateOf(true)
                }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        WaveHeader()
                        AnimatedVisibility(
                            visible = isOnLoginPage.value,
                            enter = fadeIn() + slideInHorizontally { -30 },
                            exit = fadeOut() + slideOutHorizontally { 30 }
                        ) {
                            LoginScreen(isOnLoginPage, login)
                        }
                        AnimatedVisibility(
                            visible = !isOnLoginPage.value,
                            enter = fadeIn() + slideInHorizontally { -30 },
                            exit = fadeOut() + slideOutHorizontally { 30 }
                        ) {
                            SignUpScreen(isOnLoginPage, signUp)
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun SignUpScreen(isOnLoginPage: MutableState<Boolean>, signUp: (String, String, String) -> Unit) {
    val nameState = remember { mutableStateOf("") }
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val confirmPasswordState = remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp, horizontal = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Sign Up",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(36.dp))

        OutlinedTextField(
            value = nameState.value,
            onValueChange = { nameState.value = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)

        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = emailState.value,
            onValueChange = { emailState.value = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = passwordState.value,
            onValueChange = { passwordState.value = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPasswordState.value,
            onValueChange = { confirmPasswordState.value = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = { signUp(nameState.value, emailState.value, passwordState.value) },
        ) {
            Text(text = "Sign Up", modifier = Modifier.padding(16.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { isOnLoginPage.value = true },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Already have an account? Log in")
        }
    }
}

@Composable
fun LoginScreen(isOnLoginPage: MutableState<Boolean>, login: (String, String) -> Unit) {
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp, horizontal = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Log In", fontSize = 48.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(100.dp))

        OutlinedTextField(
            value = emailState.value,
            onValueChange = { emailState.value = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = passwordState.value,
            onValueChange = { passwordState.value = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
        Spacer(modifier = Modifier.height(128.dp))

        Button(
            onClick = { login(emailState.value, passwordState.value) },
        ) {
            Text(text = "Log In", modifier = Modifier.padding(16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { isOnLoginPage.value = false },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Don't have an account? Sign up")
        }
    }
}

@Composable
fun WaveHeader(modifier: Modifier = Modifier) {

    // Draw the SVG image with the tint color
    Icon(
        modifier = modifier,
        painter = rememberVectorPainter(
            image = ImageVector.vectorResource(id = R.drawable.wave)
        ),
        contentDescription = "Dynamic SVG Image",
        tint = MaterialTheme.colorScheme.primary
    )
}

