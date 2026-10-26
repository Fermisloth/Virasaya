package com.virasaya

import com.virasaya.data.SampleLandmarks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SampleLandmarksTest {
    @Test fun containsThreePilotLandmarks() {
        assertEquals(3, SampleLandmarks.all.size)
        assertNotNull(SampleLandmarks.all.firstOrNull { it.name == "Lahori Gate" })
        assertNotNull(SampleLandmarks.all.firstOrNull { it.name == "Diwan-i-Aam" })
        assertNotNull(SampleLandmarks.all.firstOrNull { it.name == "Naubat Khana" })
        assertEquals(3, SampleLandmarks.all.count { it.history.isNotBlank() && it.sources.isNotEmpty() })
    }
}
