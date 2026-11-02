package com.mgismart.india

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.chaquo.python.android.AndroidPlatform
import com.chaquo.python.Python
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

private val Bg=Color(0xFF080B12); private val Panel=Color(0xFF121925)
private val Muted=Color(0xFF91A0B5); private val Accent=Color(0xFFB7D9FF)
private val Good=Color(0xFF8BE0B0)

class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);if(!Python.isStarted())Python.start(AndroidPlatform(this));setContent{MgApp()}}
}
@Composable fun MgApp(){
 val activity=LocalContext.current as ComponentActivity
 var phone by remember{mutableStateOf("")};var password by remember{mutableStateOf("")}
 var vin by remember{mutableStateOf("")};var connected by remember{mutableStateOf(false)}
 var busy by remember{mutableStateOf(false)};var message by remember{mutableStateOf("")}
 var vehicle by remember{mutableStateOf("MG Windsor EV Pro")};var battery by remember{mutableStateOf("--")}
 var range by remember{mutableStateOf("--")};var odo by remember{mutableStateOf("--")}
 var locked by remember{mutableStateOf<Boolean?>(null)};var climate by remember{mutableStateOf<Boolean?>(null)}
 var cabin by remember{mutableStateOf("--")};var charging by remember{mutableStateOf("Not reported")}
 var pending by remember{mutableStateOf<String?>(null)};var showPin by remember{mutableStateOf(false)};var pin by remember{mutableStateOf("")}
 fun apply(o:JSONObject){
  vehicle=o.optString("vehicle",vehicle);vin=o.optString("vin",vin)
  battery=o.txt("battery","%");range=o.txt("range_km"," km");odo=o.txt("odometer_km"," km")
  locked=if(o.isNull("locked"))null else o.optBoolean("locked")
  climate=if(o.isNull("climate_running"))null else o.optBoolean("climate_running")
  cabin=o.txt("interior_temperature","°C")
  val c=o.optJSONObject("charging")
  charging=if(c==null)"No charge data" else if(c.optBoolean("is_charging"))"Charging • "+String.format("%.1f kW",c.optDouble("power_kw")) else if(c.optBoolean("is_plugged_in"))"Plugged in • not charging" else "Not charging"
 }
 fun login(){
  busy=true;message="Connecting to MG India…"
  activity.lifecycleScope.launch{val raw=withContext(Dispatchers.IO){Python.getInstance().getModule("mg_bridge").callAttr("connect",phone,password).toString()}
   val o=JSONObject(raw);busy=false;if(o.optBoolean("ok")){apply(o);connected=true;message="Connected. Data is from the community MG India service."}else message=o.optString("error","Connection failed.")}
 }
 fun refresh(){
  if(busy)return;busy=true;message="Refreshing vehicle status…"
  activity.lifecycleScope.launch{val raw=withContext(Dispatchers.IO){Python.getInstance().getModule("mg_bridge").callAttr("connect",phone,password).toString()}
   val o=JSONObject(raw);busy=false;if(o.optBoolean("ok")){apply(o);message="Vehicle status updated."}else message=o.optString("error","Refresh failed.")}
 }
 fun send(action:String,secret:String){
  busy=true;showPin=false;pending=null;pin="";message="Sending command and verifying vehicle response…"
  activity.lifecycleScope.launch{val raw=withContext(Dispatchers.IO){Python.getInstance().getModule("mg_bridge").callAttr("control",phone,password,secret,vin,action).toString()}
   val o=JSONObject(raw);busy=false;if(o.optBoolean("ok")){apply(o);message=if(o.optBoolean("verified"))"Command confirmed by fresh vehicle status." else o.optString("message","Command sent; status not confirmed.")}else message=o.optString("error","Command failed.")}
 }
 MaterialTheme(colorScheme=darkColorScheme(primary=Accent,background=Bg,surface=Panel)){
  Surface(Modifier.fillMaxSize(),color=Bg){
   if(!connected) Connect(phone,{phone=it},password,{password=it},message,busy,::login)
   else Dashboard(vehicle,vin,battery,range,odo,locked,climate,cabin,charging,message,busy,::refresh,{pending=it})
   if(pending!=null)AlertDialog(onDismissRequest={pending=null},title={Text("Confirm "+pending!!.label()+"?")},
    text={Text("This sends a remote command to your MG Windsor EV Pro. Ensure the vehicle is parked safely. Your 4-digit vehicle control PIN is required.")},
    confirmButton={TextButton(onClick={showPin=true}){Text("Continue")}},
    dismissButton={TextButton(onClick={pending=null}){Text("Cancel")}})
   if(showPin)AlertDialog(onDismissRequest={showPin=false;pin=""},title={Text("Vehicle control PIN")},
    text={OutlinedTextField(value=pin,onValueChange={pin=it.filter(Char::isDigit).take(4)},label={Text("4-digit PIN")},singleLine=true,visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword))},
    confirmButton={TextButton(enabled=pin.length==4&&!busy,onClick={val a=pending;showPin=false;if(a!=null)send(a,pin)}){Text("Send command")}},
    dismissButton={TextButton(onClick={showPin=false;pin=""}){Text("Cancel")}})
  }
 }
}
private fun JSONObject.txt(k:String,s:String):String{val v=opt(k);return if(v==null||v==JSONObject.NULL)"--" else "$v$s"}
private fun String.label()=when(this){"lock"->"Lock doors";"unlock"->"Unlock doors";"climate_on"->"Turn AC ON";else->"Turn AC OFF"}

@Composable fun Connect(phone:String,onPhone:(String)->Unit,password:String,onPassword:(String)->Unit,message:String,busy:Boolean,onLogin:()->Unit){
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp)){
  Spacer(Modifier.height(28.dp));Text("MG  iSMART",color=Color.White,fontSize=25.sp,fontWeight=FontWeight.Bold)
  Text("INDIA  •  WINDSOR EV PRO",color=Muted,fontSize=11.sp);Spacer(Modifier.height(32.dp))
  Text("Connect your MG account",color=Color.White,fontSize=24.sp,fontWeight=FontWeight.Bold)
  Text("Use the mobile number and password registered with MG iSMART India.",color=Muted,fontSize=13.sp)
  Spacer(Modifier.height(22.dp))
  OutlinedTextField(phone,{onPhone(it.filter(Char::isDigit).take(10))},Modifier.fillMaxWidth(),label={Text("Registered mobile number")},singleLine=true)
  Spacer(Modifier.height(12.dp));OutlinedTextField(password,onPassword,Modifier.fillMaxWidth(),label={Text("MG account password")},visualTransformation=PasswordVisualTransformation(),singleLine=true)
  Spacer(Modifier.height(18.dp));Button(onClick=onLogin,enabled=!busy&&phone.length==10&&password.isNotBlank(),modifier=Modifier.fillMaxWidth().height(52.dp),colors=ButtonDefaults.buttonColors(containerColor=Accent,contentColor=Bg)){Text(if(busy)"CONNECTING…" else "CONNECT MG ACCOUNT",fontWeight=FontWeight.Bold)}
  if(message.isNotBlank()){Spacer(Modifier.height(16.dp));Text(message,color=Color(0xFFFFD59A),fontSize=12.sp)}
  Spacer(Modifier.height(18.dp));Text("Unofficial community connection; MG may change the service. Credentials remain in app memory only and are not saved. OTP/MFA is not currently handled. Never share your password, OTP or PIN in chat.",color=Muted,fontSize=11.sp,lineHeight=16.sp)
 }
}
@Composable fun Dashboard(vehicle:String,vin:String,battery:String,range:String,odo:String,locked:Boolean?,climate:Boolean?,cabin:String,charging:String,message:String,busy:Boolean,onRefresh:()->Unit,onAction:(String)->Unit){
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)){
  Spacer(Modifier.height(20.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column{Text("MG  iSMART",color=Color.White,fontSize=24.sp,fontWeight=FontWeight.Bold);Text("INDIA  •  WINDSOR EV PRO",color=Muted,fontSize=10.sp)};TextButton(onClick=onRefresh,enabled=!busy){Text(if(busy)"…" else "REFRESH")}}
  Spacer(Modifier.height(16.dp));Text(vehicle,color=Color.White,fontSize=24.sp,fontWeight=FontWeight.SemiBold);Text("VIN: $vin",color=Muted,fontSize=10.sp)
  Spacer(Modifier.height(14.dp));Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){Metric("BATTERY",battery,Modifier.weight(1f));Metric("RANGE",range,Modifier.weight(1f))}
  Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){Metric("ODOMETER",odo,Modifier.weight(1f));Metric("CABIN TEMP",cabin,Modifier.weight(1f))}
  Metric("DOOR STATUS",locked?.let{if(it)"Locked" else "Unlocked"}?:"Not reported",Modifier.fillMaxWidth())
  Metric("CLIMATE",climate?.let{if(it)"ON" else "OFF"}?:"Not reported",Modifier.fillMaxWidth());Metric("CHARGING",charging,Modifier.fillMaxWidth())
  Spacer(Modifier.height(16.dp));Text("REMOTE CONTROLS",color=Color.White,fontSize=13.sp,fontWeight=FontWeight.Bold);Text("Each command requires confirmation and your vehicle control PIN.",color=Muted,fontSize=11.sp);Spacer(Modifier.height(8.dp))
  Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){Action("LOCK",Modifier.weight(1f),busy){onAction("lock")};Action("UNLOCK",Modifier.weight(1f),busy){onAction("unlock")}}
  Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){Action("AC ON",Modifier.weight(1f),busy){onAction("climate_on")};Action("AC OFF",Modifier.weight(1f),busy){onAction("climate_off")}}
  Spacer(Modifier.height(12.dp));Text("Cabin temperature is read-only; no verified target-temperature command is available in this client. Charging data is read-only; start/stop charging is not exposed.",color=Muted,fontSize=11.sp,lineHeight=16.sp)
  if(message.isNotBlank()){Spacer(Modifier.height(14.dp));Text(message,color=Good,fontSize=12.sp)};Spacer(Modifier.height(24.dp))
 }
}
@Composable fun Metric(title:String,value:String,modifier:Modifier=Modifier){Column(modifier.padding(vertical=5.dp).background(Panel,RoundedCornerShape(15.dp)).padding(14.dp)){Text(title,color=Muted,fontSize=9.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(5.dp));Text(value,color=Color.White,fontSize=19.sp,fontWeight=FontWeight.SemiBold)}}
@Composable fun Action(label:String,modifier:Modifier,busy:Boolean,onClick:()->Unit){OutlinedButton(onClick=onClick,enabled=!busy,modifier=modifier.height(50.dp),shape=RoundedCornerShape(12.dp)){Text(label,fontWeight=FontWeight.Bold)}}
