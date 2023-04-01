package com.cav.hackathon

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cav.hackathon.ui.theme.HackathonCSTTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



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
                            LoginScreen(isOnLoginPage)
                        }
                        AnimatedVisibility(
                            visible = !isOnLoginPage.value,
                            enter = fadeIn() + slideInHorizontally { -30 },
                            exit = fadeOut() + slideOutHorizontally { 30 }
                        ) {
                            SignUpScreen(isOnLoginPage)
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun SignUpScreen(isOnLoginPage: MutableState<Boolean>) {
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
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    val client = OkHttpClient()

                    val urlBuilder = HttpUrl.parse("https://translated-mymemory---translation-memory.p.rapidapi.com/get")?.newBuilder()
                    urlBuilder?.addQueryParameter("langpair", "en|fr")
                    urlBuilder?.addQueryParameter("q", "Test for change!")
                    urlBuilder?.addQueryParameter("mt", "1")
                    urlBuilder?.addQueryParameter("onlyprivate", "0")
                    urlBuilder?.addQueryParameter("de", "a@b.c")
                    val url = urlBuilder?.build()

                    val request = Request.Builder()
                        .url(url)
                        .get()
                        .addHeader("X-RapidAPI-Key", "242fa97af6msha9e1941d0bfe38cp1d09bajsnbd2a7d5b6be0")
                        .addHeader("X-RapidAPI-Host", "translated-mymemory---translation-memory.p.rapidapi.com")
                        .build()

                    val response = client.newCall(request).execute()
                    response.body()?.let { Log.d("httpreq",JSONObject(JSONObject(it.string()).getString("responseData"))
                        .getString("translatedText")) }
                }
            },
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
fun LoginScreen(isOnLoginPage: MutableState<Boolean>) {
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
            onClick = { /* Handle login action */ },
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