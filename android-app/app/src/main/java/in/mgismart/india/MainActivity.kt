package com.mgismart.india

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF080B12)
private val Panel = Color(0xFF121925)
private val Muted = Color(0xFF91A0B5)
private val Accent = Color(0xFFB7D9FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MgApp() }
    }
}

@Composable
fun MgApp() {
    var page by remember { mutableStateOf("home") }
    MaterialTheme(colorScheme = darkColorScheme(primary = Accent, background = Bg, surface = Panel)) {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            if (page == "connect") ConnectScreen(onBack = { page = "home" })
            else Dashboard(onConnect = { page = "connect" })
        }
    }
}

@Composable
fun Dashboard(onConnect: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(22.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("MG  iSMART", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text("INDIA  •  WINDSOR EV PRO", color = Muted, fontSize = 10.sp, letterSpacing = 1.7.sp)
            }
            Box(Modifier.size(42.dp).border(1.dp, Color(0xFF40536B), CircleShape), contentAlignment = Alignment.Center) {
                Text("MG", color = Accent, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth().background(Color(0xFF342719), RoundedCornerShape(12.dp)).padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(Color(0xFFFFC46B), CircleShape))
            Spacer(Modifier.width(9.dp))
            Column {
                Text("VEHICLE NOT CONNECTED", color = Color(0xFFFFD59A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("Connect your MG account to load live vehicle data", color = Color(0xFFD8C3A5), fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(18.dp))
        Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF1C2A3D), Color(0xFF101722))), RoundedCornerShape(24.dp)).padding(20.dp)) {
            Column {
                Text("YOUR ELECTRIC DRIVE", color = Muted, fontSize = 10.sp, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(7.dp))
                Text("Windsor EV Pro", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
                Text("Vehicle overview", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(22.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(116.dp).border(7.dp, Color(0xFF33465E), CircleShape).padding(7.dp).border(2.dp, Accent.copy(alpha = .65f), CircleShape), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("--", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Light)
                            Text("BATTERY", color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
                        }
                    }
                    Spacer(Modifier.width(22.dp))
                    Column {
                        Text("Estimated range", color = Muted, fontSize = 11.sp)
                        Text("-- km", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        Text("Charging status", color = Muted, fontSize = 11.sp)
                        Text("Not available", color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("VEHICLE STATUS", color = Muted, fontSize = 10.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatusCard("LOCK", "--", "Door status", Modifier.weight(1f))
            StatusCard("ODOMETER", "-- km", "Distance", Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatusCard("CABIN", "-- °C", "Temperature", Modifier.weight(1f))
            StatusCard("CHARGING", "--", "Power / time", Modifier.weight(1f))
        }
        Spacer(Modifier.height(20.dp))
        Button(onClick = onConnect, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color(0xFF101722))) {
            Text("CONNECT MG ACCOUNT", fontWeight = FontWeight.Bold, letterSpacing = .7.sp)
        }
        Spacer(Modifier.height(12.dp))
        Text("Remote controls are disabled until a verified, secure MG India API connection is implemented.", color = Muted, fontSize = 10.sp, lineHeight = 15.sp)
        Spacer(Modifier.height(26.dp))
    }
}

@Composable
fun StatusCard(title: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier.background(Panel, RoundedCornerShape(16.dp)).padding(15.dp)) {
        Text(title, color = Muted, fontSize = 9.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Text(subtitle, color = Muted, fontSize = 10.sp)
    }
}

@Composable
fun ConnectScreen(onBack: () -> Unit) {
    var mobile by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp)) {
        TextButton(onClick = onBack) { Text("‹  BACK", color = Accent) }
        Spacer(Modifier.height(20.dp))
        Text("Connect your MG", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Text("MG iSMART India account", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(25.dp))
        Text("MOBILE NUMBER", color = Muted, fontSize = 10.sp, letterSpacing = 1.sp)
        OutlinedTextField(value = mobile, onValueChange = { mobile = it.filter(Char::isDigit).take(10) }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("10-digit Indian mobile") }, singleLine = true)
        Spacer(Modifier.height(18.dp))
        Text("Connection setup is not active in this preview build.", color = Color(0xFFFFD59A), fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Text("Your MG password, OTP and vehicle control PIN are not requested or transmitted by this version. A verified API adapter and secure authentication flow must be completed before live connection or remote commands can be enabled.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg)) { Text("RETURN TO DASHBOARD") }
    }
}
