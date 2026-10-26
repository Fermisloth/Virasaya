package com.virasaya.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.virasaya.data.SampleLandmarks
import com.virasaya.navigation.WalkingNetwork
import com.virasaya.navigation.instructions
import com.virasaya.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

@Composable
private fun PlaceSelector(caption:String,id:String,onSelected:(String)->Unit,modifier:Modifier=Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        Column(Modifier.fillMaxWidth().clickable{expanded=true}.padding(horizontal=16.dp,vertical=9.dp)) {
            Text(caption,style=MaterialTheme.typography.bodySmall,color=Muted)
            Text(SampleLandmarks.byId(id)?.name ?: "Choose landmark",style=MaterialTheme.typography.titleMedium)
        }
        DropdownMenu(expanded,onDismissRequest={expanded=false}) {
            SampleLandmarks.all.forEach { landmark ->
                DropdownMenuItem(text={Text(landmark.name)},onClick={expanded=false;onSelected(landmark.id)})
            }
        }
    }
}

@Composable
fun RouteScreen(initialStart:String,network:WalkingNetwork?,mapFailed:Boolean,onBack:()->Unit,onArPreview:(String,String,Int)->Unit,restoredStepIndex:Int,onStepRestored:()->Unit) {
    val context=LocalContext.current
    var startId by rememberSaveable { mutableStateOf(initialStart) }
    var destinationId by rememberSaveable { mutableStateOf(if(initialStart=="diwan-i-aam") "lahori-gate" else "diwan-i-aam") }
    var preview by rememberSaveable { mutableStateOf(false) }
    var stepIndex by rememberSaveable { mutableIntStateOf(0) }
    val destination=SampleLandmarks.byId(destinationId) ?: SampleLandmarks.all.last()
    val route=remember(network,startId,destinationId){network?.route(startId,destinationId)}
    val instructions=remember(route,destination.name){route?.instructions(destination.name).orEmpty()}
    val activeStep=instructions.getOrNull(stepIndex)
    LaunchedEffect(restoredStepIndex) {
        if (restoredStepIndex >= 0 && instructions.isNotEmpty()) {
            stepIndex = restoredStepIndex.coerceIn(instructions.indices)
            preview = true
            onStepRestored()
        }
    }
    val mapState=remember(route,startId,destinationId,preview,stepIndex) {
        JSONObject().put("start",startId).put("destination",destinationId).apply {
            route?.let { put("points",JSONArray().apply { it.points.forEach { p->put(JSONArray(listOf(p.longitude,p.latitude))) } }) }
            if(preview && activeStep!=null)put("focus",JSONArray(listOf(activeStep.location.longitude,activeStep.location.latitude)))
        }.toString()
    }
    val resetPreview={preview=false;stepIndex=0}
    val header: @Composable () -> Unit = {
        Row(Modifier.fillMaxWidth().padding(horizontal=18.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
            RoundAction("back","Back",onBack)
            Column(Modifier.weight(1f)) { Text("Walk through history",style=MaterialTheme.typography.titleLarge);Text("Red Fort · Offline map",style=MaterialTheme.typography.bodySmall,color=Muted) }
            AppIcon("map",modifier=Modifier.size(22.dp),color=Orange)
        }
    }
    val selectors: @Composable () -> Unit = {
        Row(Modifier.fillMaxWidth().padding(start=18.dp,end=18.dp,bottom=14.dp).background(Panel,RoundedCornerShape(20.dp)),verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                PlaceSelector("Start landmark",startId,{next->val old=startId;startId=next;if(next==destinationId)destinationId=old;resetPreview()})
                HorizontalDivider(Modifier.padding(horizontal=16.dp),color=Color.White.copy(alpha=.09f))
                PlaceSelector("Destination",destinationId,{next->val old=destinationId;destinationId=next;if(next==startId)startId=old;resetPreview()})
            }
            IconButton(onClick={val old=startId;startId=destinationId;destinationId=old;resetPreview()},modifier=Modifier.padding(end=8.dp)) {
                AppIcon("swap","Swap start and destination",color=Orange)
            }
        }
    }
    val summary: @Composable () -> Unit = {
        Column(Modifier.fillMaxWidth().background(Panel).padding(horizontal=22.dp,vertical=16.dp),verticalArrangement=Arrangement.spacedBy(9.dp)) {
            if(route==null) {
                Text(if(mapFailed) "Local route data unavailable" else if(network==null) "Preparing the local map…" else "No mapped walking connection",style=MaterialTheme.typography.titleMedium)
                Text("You can also look up this destination in Google Maps.",color=Muted,style=MaterialTheme.typography.bodySmall)
            } else if(preview && activeStep!=null) {
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    Text("Step ${stepIndex+1} of ${instructions.size} · Manual preview",Modifier.weight(1f),color=Orange,style=MaterialTheme.typography.bodySmall)
                    IconButton(onClick={resetPreview()},modifier=Modifier.size(28.dp)){AppIcon("close","Close step preview",Modifier.size(18.dp))}
                }
                Text(activeStep.text,style=MaterialTheme.typography.titleLarge)
                if(activeStep.meters>0)Text("Continue for approximately ${activeStep.meters.roundToInt()} m",color=Muted,style=MaterialTheme.typography.bodyMedium)
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick={stepIndex--},enabled=stepIndex>0,modifier=Modifier.weight(1f)){Text("Previous")}
                    Button(onClick={if(stepIndex<instructions.lastIndex)stepIndex++ else resetPreview()},modifier=Modifier.weight(1f)){Text(if(stepIndex<instructions.lastIndex) "Next step" else "Finish preview")}
                }
                OutlinedButton(onClick={onArPreview(startId,destinationId,stepIndex)},modifier=Modifier.fillMaxWidth()) { Text("Try AR preview") }
                Text("Camera-local marker · align manually · 2D remains available",color=Muted,style=MaterialTheme.typography.bodySmall)
            } else {
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    AppIcon("walk",modifier=Modifier.size(25.dp),color=Orange)
                    Text("Approx. ${route.meters.roundToInt()} m",style=MaterialTheme.typography.headlineMedium)
                    Text("On foot",color=Muted,style=MaterialTheme.typography.bodySmall)
                }
                Text("${SampleLandmarks.byId(startId)?.name} to ${destination.name}",style=MaterialTheme.typography.bodyMedium,color=Muted)
                if(route.hasSteps)Text("This mapped route includes steps.",style=MaterialTheme.typography.bodySmall,color=Orange)
                PrimaryAction("Preview walking directions",{preview=true;stepIndex=0})
            }
            Text("Mapped paths · follow site signs · no live tracking",style=MaterialTheme.typography.bodySmall,color=Muted)
            TextButton(onClick={
                val point=network?.landmarks?.get(destinationId)?.location
                val target=if(point!=null) "${point.latitude},${point.longitude}" else "${destination.name}, Red Fort, Delhi"
                openLink(context,"https://www.google.com/maps/dir/?api=1&destination=${Uri.encode(target)}&travelmode=walking")
            },modifier=Modifier.align(Alignment.CenterHorizontally),contentPadding=PaddingValues(horizontal=8.dp,vertical=0.dp)) {
                Text("Open in Google Maps",style=MaterialTheme.typography.labelLarge)
                Spacer(Modifier.width(8.dp));AppIcon("source",modifier=Modifier.size(16.dp),color=Orange)
            }
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(Ink)) {
        if (maxWidth > maxHeight) {
            Row(Modifier.fillMaxSize()) {
                Column(
                    Modifier.width(340.dp).fillMaxHeight().verticalScroll(rememberScrollState())
                ) {
                    header()
                    selectors()
                    summary()
                }
                OfflineMap(mapState, Modifier.weight(1f).fillMaxHeight())
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                header()
                selectors()
                OfflineMap(mapState, Modifier.fillMaxWidth().weight(1f))
                summary()
            }
        }
    }
}
