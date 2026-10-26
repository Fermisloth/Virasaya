package com.virasaya.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.virasaya.data.Landmark
import com.virasaya.data.SampleLandmarks
import com.virasaya.ui.theme.*
import org.json.JSONObject

@Composable
fun RoundAction(icon: String, description: String, onClick: () -> Unit, active: Boolean = false) {
    IconButton(onClick, Modifier.size(44.dp).clip(CircleShape).background(if (active) Orange else Color(0xB332392D)).border(1.dp, Color.White.copy(alpha=.14f), CircleShape)) {
        AppIcon(icon, description, Modifier.size(20.dp), if (active) Ink else Color.White)
    }
}

@Composable
fun PrimaryAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick, modifier.fillMaxWidth().heightIn(min=52.dp), shape = RoundedCornerShape(28.dp), contentPadding = PaddingValues(horizontal=22.dp,vertical=14.dp)) {
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
        AppIcon("arrow", modifier=Modifier.size(20.dp), color=Ink)
    }
}

@Composable
fun ExploreScreen(savedIds: Set<String>, toggleSaved: (String) -> Unit, onDetails: (String) -> Unit, onMap: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var savedOnly by rememberSaveable { mutableStateOf(false) }
    val landmarks = SampleLandmarks.all.filter { (!savedOnly || it.id in savedIds) && it.name.contains(query,ignoreCase=true) }
    val pager = rememberPagerState(pageCount={landmarks.size})
    val heroHeight = (LocalConfiguration.current.screenHeightDp * .49f).coerceIn(285f,430f).dp
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF354230),Ink),endY=1100f))) {
        LazyColumn(Modifier.weight(1f),contentPadding=PaddingValues(bottom=18.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
            item {
                Row(Modifier.fillMaxWidth().padding(start=24.dp,end=24.dp,top=20.dp),verticalAlignment=Alignment.CenterVertically) {
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)) {
                        Text("Your heritage companion",style=MaterialTheme.typography.bodySmall,color=Muted)
                        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                            AppIcon("pin",modifier=Modifier.size(18.dp))
                            Text("Red Fort, Delhi",style=MaterialTheme.typography.titleLarge)
                        }
                    }
                    Box(Modifier.size(42.dp).clip(CircleShape).background(Orange),contentAlignment=Alignment.Center) {
                        Text("V",fontWeight=FontWeight.SemiBold,fontSize=23.sp,color=Ink)
                    }
                }
            }
            item {
                OutlinedTextField(query,{query=it},Modifier.fillMaxWidth().padding(horizontal=24.dp),
                    placeholder={Text("Find a landmark",style=MaterialTheme.typography.bodyMedium)},
                    leadingIcon={AppIcon("search",modifier=Modifier.size(19.dp),color=Muted)},singleLine=true,shape=RoundedCornerShape(30.dp),
                    colors=OutlinedTextFieldDefaults.colors(unfocusedBorderColor=Color.White.copy(alpha=.14f),unfocusedContainerColor=Color.White.copy(alpha=.04f)))
            }
            item {
                Row(Modifier.padding(horizontal=24.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    FilterChip(!savedOnly,onClick={savedOnly=false},label={Text("Discover")},leadingIcon={AppIcon("home",modifier=Modifier.size(16.dp),color=if(!savedOnly) Orange else Muted)},shape=RoundedCornerShape(24.dp))
                    FilterChip(savedOnly,onClick={savedOnly=true},label={Text("Saved")},leadingIcon={AppIcon("bookmark",modifier=Modifier.size(16.dp),color=if(savedOnly) Orange else Muted)},shape=RoundedCornerShape(24.dp))
                    AssistChip(onClick=onMap,label={Text("Site map")},leadingIcon={AppIcon("map",modifier=Modifier.size(16.dp))},shape=RoundedCornerShape(24.dp))
                }
            }
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal=24.dp),verticalAlignment=Alignment.Bottom) {
                    Column(Modifier.weight(1f)) {
                        Text(if(savedOnly) "Your saved places" else "Stories in stone.",style=MaterialTheme.typography.headlineMedium)
                        Text(if(savedOnly) "Keep your next stop close." else "Explore the heart of Shahjahanabad.",style=MaterialTheme.typography.bodySmall,color=Muted,modifier=Modifier.padding(top=5.dp))
                    }
                    Text("${landmarks.size} ${if (landmarks.size == 1) "place" else "places"}",style=MaterialTheme.typography.bodySmall,color=Muted)
                }
            }
            if(landmarks.isEmpty()) {
                item { Text(if(savedOnly) "Save a landmark with its bookmark button to find it here." else "No matching landmark. Try Lahori Gate, Naubat Khana, or Diwan-i-Aam.",Modifier.padding(24.dp),color=Muted) }
            } else {
                item {
                    HorizontalPager(pager,contentPadding=PaddingValues(horizontal=24.dp),pageSpacing=14.dp,key={landmarks[it].id}) { index ->
                        val landmark=landmarks[index]
                        LandmarkCard(landmark,landmark.id in savedIds,{toggleSaved(landmark.id)},{onDetails(landmark.id)},Modifier.height(heroHeight))
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center) {
                        repeat(landmarks.size) { index -> Box(Modifier.padding(horizontal=3.dp).size(width=if(index==pager.currentPage) 22.dp else 6.dp,height=6.dp).clip(CircleShape).background(if(index==pager.currentPage) Orange else Color(0xFF4F5947))) }
                    }
                }
            }
            item {
                Row(Modifier.padding(horizontal=24.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha=.05f)).clickable(onClick=onMap).padding(17.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                    AppIcon("map",modifier=Modifier.size(25.dp),color=Orange)
                    Column(Modifier.weight(1f)) { Text("Find your way through the fort",style=MaterialTheme.typography.titleMedium);Text("Offline map & walking routes",style=MaterialTheme.typography.bodySmall,color=Muted) }
                    AppIcon("arrow",modifier=Modifier.size(18.dp))
                }
            }
        }
        Row(Modifier.padding(start=24.dp,end=24.dp,bottom=12.dp,top=8.dp).fillMaxWidth().clip(RoundedCornerShape(32.dp)).background(Panel).padding(6.dp),horizontalArrangement=Arrangement.SpaceEvenly) {
            NavigationPill("home","Explore",!savedOnly){savedOnly=false}
            NavigationPill("map","Map",false,onMap)
            NavigationPill("bookmark","Saved",savedOnly){savedOnly=true}
        }
    }
}

@Composable
private fun NavigationPill(icon: String,label: String,selected: Boolean,onClick:()->Unit) {
    Row(Modifier.clip(RoundedCornerShape(26.dp)).background(if(selected) Orange else Color.Transparent).clickable(onClick=onClick).padding(horizontal=17.dp,vertical=13.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(7.dp)) {
        AppIcon(icon,modifier=Modifier.size(19.dp),color=if(selected) Ink else Muted)
        Text(label,color=if(selected) Ink else Muted,style=MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun LandmarkCard(landmark: Landmark,saved:Boolean,onSave:()->Unit,onOpen:()->Unit,modifier:Modifier) {
    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).clickable(onClick=onOpen)) {
        Image(painterResource(landmark.photo().resource),landmark.name,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha=.08f),Color.Transparent,Color(0xF00E1915)))))
        Row(Modifier.fillMaxWidth().padding(18.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
            Text("Red Fort collection",Modifier.clip(RoundedCornerShape(20.dp)).background(Color(0x8A172319)).padding(horizontal=12.dp,vertical=8.dp),style=MaterialTheme.typography.bodySmall)
            RoundAction("bookmark",if(saved) "Remove ${landmark.name} from saved" else "Save ${landmark.name}",onSave,saved)
        }
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(22.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text(landmark.name,style=MaterialTheme.typography.headlineLarge)
            Text(landmark.photo().subtitle,style=MaterialTheme.typography.bodyMedium,color=Color(0xFFE4E4DA))
            Row(Modifier.padding(top=8.dp).clip(RoundedCornerShape(24.dp)).background(Color.White.copy(alpha=.13f)).border(1.dp,Color.White.copy(alpha=.16f),RoundedCornerShape(24.dp)).padding(horizontal=16.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                Text("Discover landmark",style=MaterialTheme.typography.labelLarge)
                AppIcon("arrow",modifier=Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun DetailScreen(landmark:Landmark,saved:Boolean,onSave:()->Unit,onBack:()->Unit,onMap:()->Unit) {
    val context=LocalContext.current
    var sourcesOpen by rememberSaveable(landmark.id) { mutableStateOf(false) }
    val photo=landmark.photo()
    Column(Modifier.fillMaxSize().background(Ink)) {
        LazyColumn(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(20.dp),contentPadding=PaddingValues(bottom=20.dp)) {
            item {
                Box(Modifier.fillMaxWidth().height(310.dp)) {
                    Image(painterResource(photo.resource),landmark.name,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha=.2f),Color.Transparent,Ink))))
                    Row(Modifier.fillMaxWidth().padding(20.dp),horizontalArrangement=Arrangement.SpaceBetween) { RoundAction("back","Back",onBack);RoundAction("bookmark","Save landmark",onSave,saved) }
                    Column(Modifier.align(Alignment.BottomStart).padding(horizontal=24.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        Text("Red Fort, Delhi",color=Color(0xFFDFE3D9),style=MaterialTheme.typography.bodySmall)
                        Text(landmark.name,style=MaterialTheme.typography.headlineLarge)
                        Text(photo.subtitle,color=Muted,style=MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item { Column(Modifier.padding(horizontal=24.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                Text("The story",style=MaterialTheme.typography.titleLarge)
                Text(landmark.history,style=MaterialTheme.typography.bodyLarge,color=Color(0xFFCFD2C6))
            } }
            item { Column(Modifier.padding(horizontal=24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) { Text("Find it on the map",style=MaterialTheme.typography.titleMedium);TextButton(onMap){Text("Expand map") } }
                OfflineMap(JSONObject().put("selected",landmark.id).toString(),Modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(22.dp)))
            } }
            item {
                Column(Modifier.padding(horizontal=24.dp)) {
                    TextButton(onClick={sourcesOpen=!sourcesOpen},contentPadding=PaddingValues(0.dp)) { Text(if(sourcesOpen) "Hide sources & photo credits" else "Sources & photo credits",color=Muted);Spacer(Modifier.width(8.dp));AppIcon("source",modifier=Modifier.size(15.dp),color=Muted) }
                    AnimatedVisibility(sourcesOpen) {
                        Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
                            landmark.sources.forEach { source -> Text(source.organization+"\n"+source.title,Modifier.clickable {openLink(context,source.url)},style=MaterialTheme.typography.bodySmall,color=Orange) }
                            Text("Photo: ${photo.credit}. Cropped for display.",Modifier.clickable {openLink(context,photo.page)},style=MaterialTheme.typography.bodySmall,color=Muted)
                            Text("Photo license",Modifier.clickable {openLink(context,photo.license)},style=MaterialTheme.typography.bodySmall,color=Orange)
                            Text("Map © OpenStreetMap contributors, ODbL",Modifier.clickable {openLink(context,"https://www.openstreetmap.org/copyright")},style=MaterialTheme.typography.bodySmall,color=Muted)
                        }
                    }
                }
            }
        }
        PrimaryAction("Plan a walk from here",onMap,Modifier.padding(horizontal=24.dp,vertical=12.dp))
    }
}
