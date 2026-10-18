package com.virasaya.navigation

object Routes {
    const val HOME = "home"
    const val SELECT = "select"
    const val DETAILS = "details/{landmarkId}"
    const val ROUTE = "route"
    fun details(id: String) = "details/$id"
}
