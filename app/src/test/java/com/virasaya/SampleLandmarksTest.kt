package com.virasaya

import com.virasaya.data.SampleLandmarks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SampleLandmarksTest {
    @Test fun containsThreePilotLandmarks() {
        assertEquals(3, SampleLandmarks.all.size)
        assertNotNull(SampleLandmarks.all.firstOrNull { it.name == "Lahori Gate" })
        assertNotNull(SampleLandmarks.all.firstOrNull { it.name == "Diwan-i-Aam" })
        assertNotNull(SampleLandmarks.all.firstOrNull { it.name == "Naubat Khana" })
        assertEquals(3, SampleLandmarks.all.count { it.history.isNotBlank() && it.sources.isNotEmpty() })
    }

    @Test fun historicalClaimsLinkToTheUNESCOAssessmentPageThatSupportsThem() {
        val claims = mapOf(
            "lahori-gate" to listOf("ceremonial public entrance", "Chhatta Chowk"),
            "naubat-khana" to listOf("beyond Chhatta Chowk", "musicians"),
            "diwan-i-aam" to listOf("Hall of Public Audience", "raised plinth", "marble canopy"),
        )
        for ((id, supportedTerms) in claims) {
            val landmark = requireNotNull(SampleLandmarks.byId(id))
            assertEquals(1, landmark.sources.size)
            val source = landmark.sources.single()
            assertEquals("https://whc.unesco.org/document/8815", source.url)
            assertTrue(source.title.contains("p. 97"))
            supportedTerms.forEach { term -> assertTrue("$id should cite '$term'", landmark.history.contains(term, ignoreCase = true)) }
        }
    }
}
