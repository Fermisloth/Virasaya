package com.virasaya

import com.virasaya.navigation.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class WalkingNetworkTest {
    private fun realNetwork()=WalkingNetwork.parse(File("src/main/assets/map/network.tsv").readText())

    @Test fun allPilotPairsFollowActualMappedEdges() {
        val network=realNetwork()
        for(start in network.landmarks.keys) for(end in network.landmarks.keys) {
            val route=requireNotNull(network.route(start,end))
            assertEquals(network.landmarks.getValue(start).nodeId,route.nodeIds.first())
            assertEquals(network.landmarks.getValue(end).nodeId,route.nodeIds.last())
            var summed=0.0
            route.nodeIds.zipWithNext().forEach { (a,b) ->
                val edge=network.edges.firstOrNull { it.from==a && it.to==b || !it.oneWay && it.from==b && it.to==a }
                assertNotNull("Every route segment must exist in OSM",edge)
                summed+=edge!!.meters
            }
            assertEquals(summed,route.meters,.001)
            assertEquals(route.meters,route.points.zipWithNext().sumOf{(a,b)->distanceMeters(a,b)},.1)
            if(start!=end)assertTrue("Route should follow detailed geometry",route.points.size>2)
        }
    }
    @Test fun shortestRouteUsesDistanceAndHonorsOneWayEdges() {
        val nodes=mapOf("a" to GeoPoint(0.0,0.0),"b" to GeoPoint(0.0,1.0),"c" to GeoPoint(0.0,2.0))
        val landmarks=nodes.mapValues{(id,p)->MapLandmark(id,id,p,0.0)}
        val graph=WalkingNetwork(nodes,listOf(WalkingEdge("a","c",100.0,"1",false,true),WalkingEdge("a","b",10.0,"2",false,true),WalkingEdge("b","c",10.0,"3",false,true)),landmarks)
        assertEquals(listOf("a","b","c"),graph.route("a","c")!!.nodeIds)
        assertNull(graph.route("c","a"))
        assertNull(graph.route("a","unknown"))
    }
    @Test fun disconnectedGraphReturnsNoRoute() {
        val p=GeoPoint(0.0,0.0)
        val graph=WalkingNetwork(mapOf("a" to p,"b" to p),emptyList(),mapOf("a" to MapLandmark("a","a",p,0.0),"b" to MapLandmark("b","b",p,0.0)))
        assertNull(graph.route("a","b"))
        assertEquals(0.0,graph.route("a","a")!!.meters,0.0)
    }
    @Test fun directionsFollowRouteGeometry() {
        val route=WalkingRoute(listOf("a","b","c"),listOf(GeoPoint(28.0,77.0),GeoPoint(28.001,77.0),GeoPoint(28.001,77.001)),209.0,false)
        val steps=route.instructions("Destination")
        assertEquals("Follow the mapped walkway",steps[0].text)
        assertEquals("Turn right onto the walkway",steps[1].text)
        assertEquals("Arrive near Destination",steps.last().text)
    }
    @Test fun landmarkSnappingIsBoundedAndUsesRealNodes() {
        realNetwork().let { network ->
            assertEquals(3,network.landmarks.size)
            network.landmarks.values.forEach { landmark ->
                assertTrue(landmark.snapMeters<60)
                assertEquals(landmark.snapMeters,distanceMeters(landmark.location,network.nodes.getValue(landmark.nodeId)),.1)
            }
        }
    }

    @Test fun threeRedFortStopsUseTheAuditedMappedPaths() {
        val network = realNetwork()
        val routes = listOf(
            Triple("lahori-gate", "naubat-khana", 281.0 to 10),
            Triple("naubat-khana", "diwan-i-aam", 206.2 to 7),
            Triple("lahori-gate", "diwan-i-aam", 423.9 to 12),
        )
        for ((start, end, expected) in routes) {
            val route = requireNotNull(network.route(start, end))
            assertEquals(expected.first, route.meters, .2)
            assertEquals(expected.second, route.nodeIds.size)
        }
    }
}
