# Red Fort content and pilot-route audit

Audited: 27 October 2026. This note checks the three landmark summaries used by the app and documents the source and limits of its current walking network.

## Historical claims

The app's `SampleLandmarks` summaries are paraphrases of the UNESCO World Heritage Centre / ICOMOS Red Fort Complex nomination assessment, **WHC-07/31.COM/INF.8B1**, printed pages 97–98 ([assessment](https://whc.unesco.org/document/8815)). The same passages appear in the [official ICOMOS advisory-body evaluation PDF](https://whc.unesco.org/archive/advisory_body_evaluation/231rev.pdf), printed pages 97–98.

| App landmark | Supported claims in the app | Source location |
| --- | --- | --- |
| Lahori Gate | Ceremonial public entrance; leads directly into Chhatta Chowk; the arcade was formerly the palace market. | p. 97 |
| Naubat Khana | Also called Naqqarkhana / Drum House; access from the market led to it; musicians serenaded grand arrivals there. | p. 97 |
| Diwan-i-Aam | Hall of Public Audience; raised plinth; openings on three sides; the throne stood beneath a marble canopy at the back. | pp. 97–98 |

These are historical descriptions. They do not establish present-day visitor access, open gates, permitted paths, accessibility, or current operating conditions.

## Pilot walking network

The reproducible source is the checked-in OpenStreetMap extract, `data/red-fort.osm`, retrieved 22 October 2026 from the [OSM API bounding-box endpoint](https://api.openstreetmap.org/api/0.6/map?bbox=77.235,28.651,77.245,28.660), under the [OpenStreetMap ODbL](https://www.openstreetmap.org/copyright). Run `python scripts/prepare_map.py` to rebuild `app/src/main/assets/map/network.tsv`.

The importer emits 400 walking nodes and 439 consecutive-node edge records. It includes only mapped `footway`, `path`, `pedestrian`, and `steps` ways, filters explicitly restricted `access`/`foot` tags, respects `oneway:foot`, and does not bridge disconnected geometry. The three landmark endpoints are snapped to nearest mapped walking nodes 9.8 m (Lahori Gate), 7.4 m (Naubat Khana), and 14.1 m (Diwan-i-Aam) from the OSM footprint centroids; they are not surveyed entrances.

Computed shortest routes between the three pilot landmarks:

| Route | Mapped distance | Route nodes |
| --- | ---: | ---: |
| Lahori Gate → Naubat Khana | 281.0 m | 10 |
| Naubat Khana → Diwan-i-Aam | 206.2 m | 7 |
| Lahori Gate → Diwan-i-Aam | 423.9 m | 12 |

These are graph-derived estimates from the dated OSM snapshot, not surveyed walking distances. The tests check that every route segment comes from a source edge and that route length matches the summed edge lengths. OSM geometry is not proof of current access or safety; all paths, endpoint snaps, and closures need field verification before presenting the graph as visitor-ready navigation.
