package com.virasaya.data

object SampleLandmarks {
    private val unesco = SourceReference(
        title = "Red Fort Complex nomination assessment, WHC-07/31.COM/INF.8B1",
        organization = "UNESCO World Heritage Centre / ICOMOS",
        url = "https://whc.unesco.org/document/8815"
    )

    val all = listOf(
        Landmark(
            "lahori-gate", "Lahori Gate",
            "Lahori Gate was the Red Fort's ceremonial public entrance. It leads into Chhatta Chowk, the former palace market.",
            listOf(unesco), "lahori-gate"
        ),
        Landmark(
            "naubat-khana", "Naubat Khana",
            "Naubat Khana, also called the Drum House, stands beyond Chhatta Chowk. Musicians welcomed grand arrivals here.",
            listOf(unesco), "naubat-khana"
        ),
        Landmark(
            "diwan-i-aam", "Diwan-i-Aam",
            "Diwan-i-Aam was the Hall of Public Audience. The hall has a raised plinth and openings on three sides; the royal throne stood beneath a marble canopy at its back.",
            listOf(unesco), "diwan-i-aam"
        )
    )

    fun byId(id: String?): Landmark? = all.firstOrNull { it.id == id }
}
