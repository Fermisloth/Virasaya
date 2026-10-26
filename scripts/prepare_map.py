"""Build offline display geometry and a walking network from the checked-in OSM extract.

No synthetic links are added. Only consecutive nodes of mapped walking ways become edges.
Run: python scripts/prepare_map.py
"""
import json
import math
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'app/src/main/assets/map'
osm = ET.parse(ROOT / 'data/red-fort.osm').getroot()
nodes = {e.attrib['id']: e for e in osm.findall('node')}
ways = {e.attrib['id']: e for e in osm.findall('way')}

def tags(e):
    return {t.attrib['k']: t.attrib['v'] for t in e.findall('tag')}

def coord(node_id):
    e = nodes[node_id]
    return [float(e.attrib['lon']), float(e.attrib['lat'])]

def meters(a, b):
    lon1, lat1, lon2, lat2 = map(math.radians, [*a, *b])
    h = math.sin((lat2-lat1)/2)**2 + math.cos(lat1)*math.cos(lat2)*math.sin((lon2-lon1)/2)**2
    return 6371008.8 * 2 * math.asin(min(1, math.sqrt(h)))

features, edges, network_nodes = [], [], set()
restricted = {'no', 'private', 'military'}
for way_id, way in ways.items():
    t = tags(way)
    refs = [n.attrib['ref'] for n in way.findall('nd')]
    if len(refs) < 2 or not all(n in nodes for n in refs):
        continue
    coords = [coord(n) for n in refs]
    polygon = refs[0] == refs[-1] and not t.get('barrier') and (not t.get('highway') or t.get('area') == 'yes')
    features.append({'type': 'Feature', 'id': 'way/'+way_id, 'properties': t,
                     'geometry': {'type': 'Polygon' if polygon else 'LineString', 'coordinates': [coords] if polygon else coords}})
    walking = t.get('highway') in {'footway', 'path', 'pedestrian', 'steps'}
    if not walking or t.get('area') == 'yes' or t.get('access') in restricted or t.get('foot') in restricted:
        continue
    for a, b in zip(refs, refs[1:]):
        if any(tags(nodes[n]).get('access') in restricted or tags(nodes[n]).get('foot') in restricted for n in (a,b)):
            continue
        distance = meters(coord(a), coord(b))
        if distance < 0.01:
            continue
        direction = t.get('oneway:foot', 'no')
        if direction == '-1': a,b = b,a
        edges.append([a, b, round(distance, 3), way_id, t.get('highway'), direction in {'yes','1','-1'}])
        network_nodes.update((a,b))

landmarks = []
for landmark_id, name, way_id in [
    ('lahori-gate','Lahori Gate','80429907'),
    ('naubat-khana','Naubat Khana','80429900'),
    ('diwan-i-aam','Diwan-i-Aam','80429794')
]:
    refs = [n.attrib['ref'] for n in ways[way_id].findall('nd')][:-1]
    coordinates = [coord(n) for n in refs]
    center = [sum(c[i] for c in coordinates)/len(coordinates) for i in range(2)]
    nearest = min(network_nodes, key=lambda n: meters(center, coord(n)))
    gap = meters(center, coord(nearest))
    if gap > 60: raise ValueError(f'{name}: nearest path too far away ({gap:.1f}m)')
    landmarks.append({'id':landmark_id,'name':name,'coordinate':center,'nodeId':nearest,'osmWay':way_id,'snapMeters':round(gap,1)})

for node_id, node in nodes.items():
    t=tags(node)
    if t.get('natural') == 'tree' or (t.get('name') and t.get('tourism') != 'information'):
        features.append({'type':'Feature','id':'node/'+node_id,'properties':t,'geometry':{'type':'Point','coordinates':coord(node_id)}})

geojson = {'type':'FeatureCollection','features':features}
(OUT/'site-data.js').write_text('window.SITE_DATA='+json.dumps(geojson,separators=(',',':'))+';\nwindow.LANDMARKS='+json.dumps(landmarks,separators=(',',':'))+';\n',encoding='utf-8')
lines = ['# OpenStreetMap contributors, ODbL 1.0; downloaded 2026-10-22. Coordinates are lon/lat from OSM.']
for n in sorted(network_nodes):
    lon,lat=coord(n);lines.append(f'N\t{n}\t{lat}\t{lon}')
for a,b,d,w,kind,oneway in edges:
    lines.append(f'E\t{a}\t{b}\t{d}\t{w}\t{kind}\t{str(oneway).lower()}')
for l in landmarks:
    lon,lat=l['coordinate'];lines.append(f'L\t{l["id"]}\t{l["nodeId"]}\t{lat}\t{lon}\t{l["snapMeters"]}')
(OUT/'network.tsv').write_text('\n'.join(lines)+'\n',encoding='utf-8')
print(json.dumps({'features':len(features),'nodes':len(network_nodes),'edges':len(edges),'landmarks':landmarks},indent=2))
