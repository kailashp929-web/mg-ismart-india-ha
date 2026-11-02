package com.mgismart.india

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.chaquo.python.AndroidPlatform
import com.chaquo.python.Python
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

private val Bg = Color(0xFF080B12)
private val Panel = Color(0xFF121925)
private val Muted = Color(0xFF91A0B5)
private val Accent = Color(0xFFB7D9FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Python.isStarted()) Python.start(AndroidPlatform(this))
        setContent { MgApp() }
    }
}

@Composable
fun MgApp() {
    var connected by remember { mutableStateOf(false) }
    var vehicle by remember { mutableStateOf("MG Windsor EV Pro") }
    var vin by remember { mutableStateOf("") }
    var battery by remember { mutableStateOf("--") }
    var range by remember { mutableStateOf("--") }
    var odometer by remember { mutableStateOf("--") }
    var lock by remember { mutableStateOf("--") }
    var message by remember { mutableStateOf("") }
    MaterialTheme(colorScheme = darkColorScheme(primary = Accent, background = Bg, surface = Panel)) {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            if (!connected) ConnectScreen(
                message = message,
                onConnect = { phone, password, done ->
                    message = "Connecting securely to MG India…"
                    // Credentials are used only for this in-memory login attempt.
                    // They are not saved to preferences, files, or application logs.
                    done(false)
                },
                onLogin = { phone, password, setBusy ->
                    setBusy(true)
                    message = "Connecting securely to MG India…"
                    val activity = this@MgApp
                }
            ) else Dashboard(vehicle, vin, battery, range, odometer, lock)
        }
    }
}

@Composable
fun ConnectScreen(
    message: String,
    onConnect: (String, String, (Boolean) -> Unit) -> Unit,
    onLogin: (String, String, (Boolean) -> Unit) -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp)) {
        Spacer(Modifier.height(28.dp))
        Text("MG  iSMART", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("INDIA  •  WINDSOR EV PRO", color = Muted, fontSize = 11.sp)
        Spacer(Modifier.height(32.dp))
        Text("Connect your MG account", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Use the mobile number registered with MG iSMART India.", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(22.dp))
        OutlinedTextField(value = phone, onValueChange = { phone = it.filter(Char::isDigit).take(10) }, modifier = Modifier.fillMaxWidth(), label = { Text("Registered mobile number") }, placeholder = { Text("10-digit mobile") }, singleLine = true)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, modifier = Modifier.fillMaxWidth(), label = { Text("MG account password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = { onLogin(phone, password) { busy = it } },
            enabled = !busy && phone.length == 10 && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg)
        ) { Text(if (busy) "CONNECTING…" else "CONNECT MG ACCOUNT", fontWeight = FontWeight.Bold) }
        if (message.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Text(message, color = Color(0xFFFFD59A), fontSize = 12.sp)
        }
        Spacer(Modifier.height(18.dp))
        Text("Security: credentials are used for the login attempt and are not saved by this preview. OTP/MFA is not supported by the community client. Never share your password or OTP in chat.", color = Muted, fontSize = 11.sp, lineHeight = 16.sp)
    }
}

@Composable
fun Dashboard(vehicle: String, vin: String, battery: String, range: String, odometer: String, lock: String) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Spacer(Modifier.height(24.dp))
        Text("MG  iSMART", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("INDIA  •  WINDSOR EV PRO", color = Muted, fontSize = 10.sp)
        Spacer(Modifier.height(24.dp))
        Text(vehicle, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
        Text("VIN: $vin", color = Muted, fontSize = 10.sp)
        Spacer(Modifier.height(18.dp))
        MetricCard("BATTERY", battery)
        MetricCard("ESTIMATED RANGE", range)
        MetricCard("ODOMETER", odometer)
        MetricCard("DOOR LOCK", lock)
        Spacer(Modifier.height(18.dp))
        Text("Live values depend on MG account permissions and vehicle/server response. Remote commands are not enabled in this build.", color = Muted, fontSize = 12.sp)
    }
}

@Composable
fun MetricCard(title: String, value: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp).background(Panel, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Text(title, color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(value, color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
    }
}
