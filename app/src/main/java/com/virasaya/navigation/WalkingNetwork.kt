package com.virasaya.navigation

import java.util.PriorityQueue
import kotlin.math.*

data class GeoPoint(val latitude: Double, val longitude: Double)
data class WalkingEdge(val from: String, val to: String, val meters: Double, val osmWay: String, val steps: Boolean, val oneWay: Boolean = false)
data class MapLandmark(val id: String, val nodeId: String, val location: GeoPoint, val snapMeters: Double)
data class WalkingRoute(val nodeIds: List<String>, val points: List<GeoPoint>, val meters: Double, val hasSteps: Boolean)
data class WalkingInstruction(val text: String, val meters: Double, val location: GeoPoint)

/** Routes only over consecutive OSM walking-way nodes. Never joins disconnected geometry. */
class WalkingNetwork(
    val nodes: Map<String, GeoPoint>,
    val edges: List<WalkingEdge>,
    val landmarks: Map<String, MapLandmark>
) {
    private val adjacency = mutableMapOf<String, MutableList<WalkingEdge>>()
    init {
        require(edges.all { it.from in nodes && it.to in nodes && it.meters.isFinite() && it.meters > 0 })
        require(landmarks.values.all { it.nodeId in nodes })
        edges.forEach { edge ->
            adjacency.getOrPut(edge.from) { mutableListOf() }.add(edge)
            if (!edge.oneWay) adjacency.getOrPut(edge.to) { mutableListOf() }.add(edge.copy(from = edge.to, to = edge.from))
        }
    }

    fun route(startId: String, destinationId: String): WalkingRoute? {
        val start = landmarks[startId]?.nodeId ?: return null
        val destination = landmarks[destinationId]?.nodeId ?: return null
        val distance = mutableMapOf(start to 0.0)
        val previous = mutableMapOf<String, WalkingEdge>()
        val queue = PriorityQueue(compareBy<Pair<String, Double>> { it.second })
        queue.add(start to 0.0)
        while (queue.isNotEmpty()) {
            val (current, cost) = queue.remove()
            if (cost > distance.getValue(current)) continue
            if (current == destination) break
            for (edge in adjacency[current].orEmpty()) {
                val candidate = cost + edge.meters
                if (candidate < (distance[edge.to] ?: Double.POSITIVE_INFINITY)) {
                    distance[edge.to] = candidate
                    previous[edge.to] = edge
                    queue.add(edge.to to candidate)
                }
            }
        }
        val cost = distance[destination] ?: return null
        val path = mutableListOf(destination)
        var steps = false
        while (path.last() != start) {
            val edge = previous[path.last()] ?: return null
            steps = steps || edge.steps
            path.add(edge.from)
        }
        path.reverse()
        return WalkingRoute(path, path.map { nodes.getValue(it) }, cost, steps)
    }

    companion object {
        fun parse(text: String): WalkingNetwork {
            val nodes = mutableMapOf<String, GeoPoint>()
            val edges = mutableListOf<WalkingEdge>()
            val landmarks = mutableMapOf<String, MapLandmark>()
            text.lineSequence().filter { it.isNotBlank() && !it.startsWith('#') }.forEach { row ->
                val p = row.split('\t')
                when (p[0]) {
                    "N" -> nodes[p[1]] = GeoPoint(p[2].toDouble(), p[3].toDouble())
                    "E" -> edges.add(WalkingEdge(p[1], p[2], p[3].toDouble(), p[4], p[5] == "steps", p[6].toBooleanStrict()))
                    "L" -> landmarks[p[1]] = MapLandmark(p[1], p[2], GeoPoint(p[3].toDouble(), p[4].toDouble()), p[5].toDouble())
                    else -> error("Unknown map record")
                }
            }
            return WalkingNetwork(nodes, edges, landmarks)
        }
    }
}

fun distanceMeters(a: GeoPoint, b: GeoPoint): Double {
    val lat1 = Math.toRadians(a.latitude)
    val lat2 = Math.toRadians(b.latitude)
    val dLat = lat2 - lat1
    val dLon = Math.toRadians(b.longitude - a.longitude)
    val h = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
    return 6371008.8 * 2 * asin(sqrt(h.coerceIn(0.0, 1.0)))
}

private fun bearing(a: GeoPoint, b: GeoPoint): Double {
    val lat1 = Math.toRadians(a.latitude)
    val lat2 = Math.toRadians(b.latitude)
    val dLon = Math.toRadians(b.longitude - a.longitude)
    return Math.toDegrees(atan2(sin(dLon) * cos(lat2), cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)))
}

/** Geometry-derived preview instructions; these do not assert a live visitor position. */
fun WalkingRoute.instructions(destinationName: String): List<WalkingInstruction> {
    if (points.size < 2) return listOf(WalkingInstruction("Already at the selected stop", 0.0, points.first()))
    val result = mutableListOf<WalkingInstruction>()
    var startIndex = 0
    var action = "Follow the mapped walkway"
    var length = 0.0
    for (i in 1 until points.size) {
        length += distanceMeters(points[i - 1], points[i])
        if (i < points.lastIndex) {
            val turn = ((bearing(points[i], points[i + 1]) - bearing(points[i - 1], points[i]) + 540) % 360) - 180
            if (abs(turn) >= 35 && length >= 8) {
                result.add(WalkingInstruction(action, length, points[startIndex]))
                action = if (abs(turn) > 145) "Follow the bend" else if (turn > 0) "Turn right onto the walkway" else "Turn left onto the walkway"
                startIndex = i
                length = 0.0
            }
        }
    }
    result.add(WalkingInstruction(action, length, points[startIndex]))
    result.add(WalkingInstruction("Arrive near $destinationName", 0.0, points.last()))
    return result
}
