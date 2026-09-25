package com.evrenhouse.trackscooter

import com.evrenhouse.trackscooter.util.VersionUtils
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionUtilsTest {

    @Test
    fun higherPatchReturnsTrue() {
        assertTrue(VersionUtils.isVersionHigher("2.7.1", "2.7.0"))
    }

    @Test
    fun higherMinorReturnsTrue() {
        assertTrue(VersionUtils.isVersionHigher("2.8.0", "2.7.0"))
    }

    @Test
    fun higherMajorReturnsTrue() {
        assertTrue(VersionUtils.isVersionHigher("3.0.0", "2.7.0"))
    }

    @Test
    fun sameVersionReturnsFalse() {
        assertFalse(VersionUtils.isVersionHigher("2.7.0", "2.7.0"))
        assertFalse(VersionUtils.isVersionHigher("v2.7.0", "2.7.0"))
        assertFalse(VersionUtils.isVersionHigher("2.7.0", "v2.7.0"))
    }

    @Test
    fun lowerVersionReturnsFalse() {
        assertFalse(VersionUtils.isVersionHigher("2.6.9", "2.7.0"))
        assertFalse(VersionUtils.isVersionHigher("1.9.9", "2.7.0"))
        assertFalse(VersionUtils.isVersionHigher("2.7.0", "2.7.1"))
    }

    @Test
    fun versionWithVPrefixReturnsCorrectly() {
        assertTrue(VersionUtils.isVersionHigher("v2.7.1", "v2.7.0"))
        assertTrue(VersionUtils.isVersionHigher("V2.8.0", "2.7.0"))
    }

    @Test
    fun nullOrBlankReturnsFalse() {
        assertFalse(VersionUtils.isVersionHigher(null, "2.7.0"))
        assertFalse(VersionUtils.isVersionHigher("", "2.7.0"))
        assertFalse(VersionUtils.isVersionHigher("2.7.1", null))
        assertFalse(VersionUtils.isVersionHigher("2.7.1", ""))
    }

    @Test
    fun unevenLengthVersions() {
        assertTrue(VersionUtils.isVersionHigher("2.8", "2.7.0"))
        assertTrue(VersionUtils.isVersionHigher("2.7.1.1", "2.7.1"))
        assertFalse(VersionUtils.isVersionHigher("2.7", "2.7.0"))
    }
}
