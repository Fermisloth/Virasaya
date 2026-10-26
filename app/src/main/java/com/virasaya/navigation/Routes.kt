package com.virasaya.navigation

object Routes {
    const val HOME = "home"
    const val SELECT = "select"
    const val DETAILS = "details/{landmarkId}"
    const val ROUTE = "route/{startId}"
    const val AR = "ar/{startId}/{destinationId}/{stepIndex}"
    fun details(id: String) = "details/$id"
    fun route(startId: String) = "route/$startId"
    fun ar(startId: String, destinationId: String, stepIndex: Int) = "ar/$startId/$destinationId/$stepIndex"
}
