package com.virasaya.data

data class SourceReference(val title: String, val organization: String, val url: String)

data class Landmark(
    val id: String,
    val name: String,
    val history: String,
    val sources: List<SourceReference>,
    val waypointId: String
)
