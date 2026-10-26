'use strict';
const ORANGE='#fb823e', INK='#202b23';
const S=ol.style;
const siteSource=new ol.source.Vector({features:new ol.format.GeoJSON().readFeatures(SITE_DATA,{featureProjection:'EPSG:3857'})});
const simple=(fill,stroke,width=1)=>new S.Style({fill:fill?new S.Fill({color:fill}):undefined,stroke:stroke?new S.Stroke({color:stroke,width}):undefined});
const styles={ground:simple('#e1e4d5','#c9cebc'),garden:simple('#c9d7b7','#bbcca8'),water:simple('#aaccc9','#91b8b5'),building:simple('#d5c9b5','#b9aa94',1),monument:simple('#ccb69b','#b39a7b',1.3),wall:simple(null,'#a2937b',3),road:[simple(null,'#c7c8bc',8),simple(null,'#faf9ef',6)],path:[simple(null,'#c3c5b5',4),simple(null,'#fffef7',2.6)],tree:new S.Style({image:new S.Circle({radius:3,fill:new S.Fill({color:'#a7be8c'})})})};
const labelCache=new Map();
styles.garden.setZIndex(1);styles.water.setZIndex(2);styles.building.setZIndex(3);styles.monument.setZIndex(3);styles.wall.setZIndex(7);styles.tree.setZIndex(4);
styles.road.forEach(s=>s.setZIndex(5));styles.path.forEach(s=>s.setZIndex(6));
const labels=new Set(['Chhatta Chowk','Diwan-e-Khaas','Moti Masjid','Rang Mahal','Mumtaz Mahal','Hayat Bakhsh Bagh','Delhi Gate']);
const base=new ol.layer.Vector({source:siteSource,declutter:true,style:(f,res)=>{
 const p=f.getProperties(),type=f.getGeometry().getType();let style;
 if(type==='Point')return p.natural==='tree'?styles.tree:undefined;
 if(p.building)style=p.historic?styles.monument:styles.building;
 else if(p.water||p.waterway||p.natural==='water')style=styles.water;
 else if(p.highway)style=['footway','path','pedestrian','steps'].includes(p.highway)?styles.path:styles.road;
 else if(p.barrier)style=styles.wall;
 else if(p.landuse==='grass'||p.leisure==='garden'||p.leisure==='park'||p.natural==='wood')style=styles.garden;
 else if(type==='Polygon')style=styles.ground;
 if(labels.has(p.name)&&res<2.8){
  if(!labelCache.has(p.name))labelCache.set(p.name,new S.Style({text:new S.Text({text:p.name,font:'500 11px Arial',fill:new S.Fill({color:'#58604b'}),stroke:new S.Stroke({color:'#f7f8ed',width:3}),overflow:false})}));
  return [...(Array.isArray(style)?style:style?[style]:[]),labelCache.get(p.name)];
 }return style;
}});
const routeSource=new ol.source.Vector();
const routeLayer=new ol.layer.Vector({source:routeSource,style:[new S.Style({stroke:new S.Stroke({color:'#fff9f0',width:9})}),new S.Style({stroke:new S.Stroke({color:ORANGE,width:5.5})})]});
const markerSource=new ol.source.Vector();
const markerLayer=new ol.layer.Vector({source:markerSource,declutter:false});
const focusSource=new ol.source.Vector();
const focusLayer=new ol.layer.Vector({source:focusSource,style:new S.Style({image:new S.Circle({radius:9,fill:new S.Fill({color:'#217b99'}),stroke:new S.Stroke({color:'#fff',width:3})})})});
const view=new ol.View({center:ol.proj.fromLonLat(LANDMARKS[1].coordinate),zoom:17.4,minZoom:15.7,maxZoom:21,extent:ol.proj.transformExtent([77.233,28.649,77.248,28.662],'EPSG:4326','EPSG:3857'),constrainOnlyCenter:true});
const map=new ol.Map({target:'map',layers:[base,routeLayer,markerLayer,focusLayer],view,pixelRatio:Math.min(window.devicePixelRatio||1,2),controls:[new ol.control.ScaleLine()],interactions:ol.interaction.defaults.defaults({altShiftDragRotate:false,pinchRotate:false})});
let lastState={},lastKey='',frameGeometry=null,initialFit=false;
function fit(animate=true){
 map.updateSize();
 const size=map.getSize();
 if(!frameGeometry||!size||size[0]<2||size[1]<2)return;
 const side=Math.min(86,size[0]*.22),vertical=Math.min(64,size[1]*.23);
 view.fit(frameGeometry,{padding:[vertical,side,vertical,side],maxZoom:18.8,duration:animate?300:0});
 initialFit=true;
}
function marker(l,state){
 const isStart=state.start===l.id,isEnd=state.destination===l.id,selected=state.selected===l.id;
 const f=new ol.Feature(new ol.geom.Point(ol.proj.fromLonLat(l.coordinate)));
 const badge=isStart?'Start':isEnd?'Destination':'';
 const text=(badge?badge+'\n':'')+l.name;
 f.setStyle(new S.Style({zIndex:isStart||isEnd||selected?20:10,image:new S.Circle({radius:isStart||isEnd||selected?7:5,fill:new S.Fill({color:isEnd||selected?ORANGE:INK}),stroke:new S.Stroke({color:'#fff',width:2.5})}),text:new S.Text({text,font:'600 12px Arial',offsetX:isEnd?-12:0,offsetY:isEnd||selected?-30:30,fill:new S.Fill({color:INK}),backgroundFill:new S.Fill({color:'#fffffff0'}),backgroundStroke:new S.Stroke({color:'#d2d8c8',width:1}),padding:[6,8,6,8],overflow:true})}));return f;
}
window.setMapState=function(state){
 const key=JSON.stringify({...state,focus:null});
 if(key!==lastKey){
  lastKey=key;routeSource.clear();markerSource.clear();
  markerSource.addFeatures(LANDMARKS.map(l=>marker(l,state)));
  if(state.points&&state.points.length>1){frameGeometry=new ol.geom.LineString(state.points.map(p=>ol.proj.fromLonLat(p)));routeSource.addFeature(new ol.Feature(frameGeometry));}
  else if(state.selected){const l=LANDMARKS.find(x=>x.id===state.selected);frameGeometry=new ol.geom.Point(ol.proj.fromLonLat(l.coordinate));}
  else frameGeometry=new ol.geom.MultiPoint(LANDMARKS.map(l=>ol.proj.fromLonLat(l.coordinate)));
  fit(Boolean(lastState.start||lastState.selected));
 }
 focusSource.clear();
 if(state.focus){const p=ol.proj.fromLonLat(state.focus);focusSource.addFeature(new ol.Feature(new ol.geom.Point(p)));view.animate({center:p,zoom:19,duration:250});}
 else if(lastState.focus){fit();}
 lastState=state;
};
document.getElementById('north').onclick=()=>view.animate({rotation:0,duration:200});
document.getElementById('zoom-in').onclick=()=>view.animate({zoom:Math.min(21,view.getZoom()+1),duration:180});
document.getElementById('zoom-out').onclick=()=>view.animate({zoom:Math.max(15.7,view.getZoom()-1),duration:180});
document.getElementById('fit').onclick=()=>fit();
new ResizeObserver(()=>{map.updateSize();if(!initialFit)fit(false);}).observe(document.getElementById('map'));
window.setMapState({});
window.mapReady=true;
