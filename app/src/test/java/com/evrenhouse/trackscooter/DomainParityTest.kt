package com.evrenhouse.trackscooter

import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.data.ScooterType
import com.evrenhouse.trackscooter.data.toUserMessage
import com.evrenhouse.trackscooter.util.DateUtils
import com.evrenhouse.trackscooter.util.Outlets
import com.evrenhouse.trackscooter.util.StatusLabels
import com.evrenhouse.trackscooter.util.TypeLabels
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class DomainParityTest {

    @Test
    fun testFleetDomainTypeLabels() {
        assertEquals("SD (Utara)", TypeLabels.of(ScooterType.SD))
        assertEquals("SJ (Jumbo Utara)", TypeLabels.of(ScooterType.SJ))
        assertEquals("SB (Barat)", TypeLabels.of(ScooterType.SB))
        assertEquals("SJB (Jumbo Barat)", TypeLabels.of(ScooterType.SJB))
        assertEquals("SM (Utara Motor)", TypeLabels.of(ScooterType.SM))
        assertEquals("SJM (Jumbo Utara Motor)", TypeLabels.of(ScooterType.SJM))
    }

    @Test
    fun testFleetDomainHomeOutlets() {
        assertEquals("utara", Outlets.getHomeOutletForType("sd"))
        assertEquals("utara", Outlets.getHomeOutletForType("sj"))
        assertEquals("barat", Outlets.getHomeOutletForType("sb"))
        assertEquals("barat", Outlets.getHomeOutletForType("sjb"))
        assertEquals("utara-motor", Outlets.getHomeOutletForType("sm"))
        assertEquals("utara-motor", Outlets.getHomeOutletForType("sjm"))
        assertEquals("utara", Outlets.getHomeOutletForType("unknown"))
    }

    @Test
    fun testStatusLabels_alignWithContextDoc() {
        assertEquals("Unit Ready", StatusLabels.of(ScooterStatus.AVAILABLE))
        assertEquals("Unit Diluar", StatusLabels.of(ScooterStatus.IN_USE))
        assertEquals("Unit Kendala", StatusLabels.of(ScooterStatus.MAINTENANCE))
    }

    @Test
    fun testDurationFormatting() {
        assertEquals("15 mnt", DateUtils.formatDuration(900))
        assertEquals("45 mnt", DateUtils.formatDuration(2700))
        assertEquals("1 j 0 mnt", DateUtils.formatDuration(3600))
        assertEquals("1 j 30 mnt", DateUtils.formatDuration(5400))
        assertEquals("1 hari 2 j", DateUtils.formatDuration(93600))
    }

    @Test
    fun testIndonesianErrorMessages() {
        val networkError = IOException("Failed to connect")
        assertEquals(
            "Tidak dapat terhubung ke server API. Periksa koneksi internet.",
            networkError.toUserMessage()
        )

        val badGateway = HttpException(
            Response.error<Any>(502, "Bad Gateway".toResponseBody("text/plain".toMediaType()))
        )
        assertTrue(badGateway.toUserMessage().contains("Server backend sedang sibuk"))

        val serviceUnavailable = HttpException(
            Response.error<Any>(503, "Service Unavailable".toResponseBody("text/plain".toMediaType()))
        )
        assertTrue(serviceUnavailable.toUserMessage().contains("Server backend sedang sibuk"))
    }
}
