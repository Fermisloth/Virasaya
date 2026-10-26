# Red Fort map and media

## Geographic data

- Source: OpenStreetMap API 0.6, downloaded 22 October 2026: https://api.openstreetmap.org/api/0.6/map?bbox=77.235,28.651,77.245,28.660
- Raw source: `red-fort.osm`. Copyright OpenStreetMap contributors; ODbL 1.0: https://www.openstreetmap.org/copyright
- Reproduce bundled geometry and routing data: `python scripts/prepare_map.py`.
- Display extract: 517 features. Routing extract: 400 nodes, 439 edges before bidirectional expansion.
- Landmark footprints: [Lahori Gate, way 80429907](https://www.openstreetmap.org/way/80429907), [Naubat Khana, way 80429900](https://www.openstreetmap.org/way/80429900), [Diwan-i-Aam, way 80429794](https://www.openstreetmap.org/way/80429794).
- Marker positions are means of each footprint's source vertices. Routing endpoints are nearest mapped walking nodes, respectively 9.8, 7.4, and 14.1 metres from those marker positions. They are not surveyed entrances.
- Edges follow consecutive nodes on OSM footways, paths, pedestrian ways, and steps. Closed areas are not converted to traversable diagonals. Ways/nodes explicitly tagged `access` or `foot` as `no`, `private`, or `military` are excluded. `oneway:foot` is respected. No links are invented to repair disconnected data.
- Edge lengths are computed from source coordinates using great-circle distance. They are approximate mapped distances, not measured walking distances. Geometry-derived turn instructions are manual previews; no live positioning or rerouting is implemented.
- OSM does not establish present-day visitor access or closures. On-site path and access verification remains required.
- The derived map and network remain under ODbL. The raw extract and importer are distributed in this repository; visible attribution is displayed in the map.

## Map software

OpenLayers 10.6.1, bundled locally from https://cdn.jsdelivr.net/npm/ol@10.6.1/dist/ol.js and https://cdn.jsdelivr.net/npm/ol@10.6.1/ol.css. BSD 2-Clause license included at `app/src/main/assets/map/OPENLAYERS-LICENSE.md`. The app uses local vector features and does not download map tiles.

## Photos

Photos are bundled at 1280px width from Wikimedia Commons, cropped in the UI. Attribution and license links are visible under each landmark's Sources & photo credits. Original source metadata is retained alongside this file.

| Asset | Author | Source | License |
| --- | --- | --- | --- |
| `lahori_gate.jpg` | Tamjeed Ahmed | https://commons.wikimedia.org/wiki/File:Lahori_Gate_of_Red_Fort,_Delhi.jpg | https://creativecommons.org/licenses/by-sa/4.0/ |
| `naubat_khana.jpg` | Radosław Botev | https://commons.wikimedia.org/wiki/File:Naubat_Khana_Red_Fort_Delhi.jpg | https://creativecommons.org/licenses/by/3.0/pl/deed.en |
| `diwan_i_aam.jpg` | Nahid Sultan | https://commons.wikimedia.org/wiki/File:Diwan-i_Am,_Red_Fort,_New_Delhi_(02).jpg | https://creativecommons.org/licenses/by-sa/4.0/ |

Photos and any displayed crops retain their respective licenses. No AI-generated photographs are used.

## Optional AR

The app uses Google's ARCore SDK (`com.google.ar:core`) for its optional camera-local AR preview. See Google's [ARCore additional terms](https://developers.google.com/ar/develop/terms) and [user privacy requirements](https://developers.google.com/ar/develop/privacy-requirements). The AR screen displays the required Google Play Services for AR privacy disclosure before requesting camera permission.

## History

The three concise history texts cite the UNESCO / ICOMOS Red Fort nomination assessment: https://whc.unesco.org/document/8815. Historical layout is not used as evidence of modern walking access.
