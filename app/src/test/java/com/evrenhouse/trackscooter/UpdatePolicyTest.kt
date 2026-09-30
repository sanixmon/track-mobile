package com.evrenhouse.trackscooter

import com.evrenhouse.trackscooter.util.UpdatePolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdatePolicyTest {

    @Test
    fun forceFlagAlwaysForces() {
        assertTrue(UpdatePolicy.isForceUpdate(currentCode = 24, minVersionCode = 0, forceFlag = true))
    }

    @Test
    fun belowMinVersionIsForced() {
        assertTrue(UpdatePolicy.isForceUpdate(currentCode = 18, minVersionCode = 19, forceFlag = false))
    }

    @Test
    fun atOrAboveMinVersionIsNotForced() {
        assertFalse(UpdatePolicy.isForceUpdate(currentCode = 19, minVersionCode = 19, forceFlag = false))
        assertFalse(UpdatePolicy.isForceUpdate(currentCode = 24, minVersionCode = 19, forceFlag = false))
    }

    @Test
    fun zeroMinVersionMeansNoFloor() {
        assertFalse(UpdatePolicy.isForceUpdate(currentCode = 1, minVersionCode = 0, forceFlag = false))
    }

    @Test
    fun forceUpdateAlwaysShows() {
        assertTrue(
            UpdatePolicy.shouldShowUpdate(
                forceUpdate = true,
                latestCode = 24,
                dismissedCode = 24,
                dismissedAt = 1_000L,
                nowMillis = 2_000L,
            )
        )
    }

    @Test
    fun neverDismissedShows() {
        assertTrue(
            UpdatePolicy.shouldShowUpdate(
                forceUpdate = false,
                latestCode = 24,
                dismissedCode = 0,
                dismissedAt = 0L,
                nowMillis = 2_000L,
            )
        )
    }

    @Test
    fun newerVersionThanDismissedShowsImmediately() {
        assertTrue(
            UpdatePolicy.shouldShowUpdate(
                forceUpdate = false,
                latestCode = 25,
                dismissedCode = 24,
                dismissedAt = 1_000L,
                nowMillis = 2_000L,
            )
        )
    }

    @Test
    fun recentlyDismissedSameVersionHides() {
        assertFalse(
            UpdatePolicy.shouldShowUpdate(
                forceUpdate = false,
                latestCode = 24,
                dismissedCode = 24,
                dismissedAt = 1_000L,
                nowMillis = 1_000L + 60_000L,
            )
        )
    }

    @Test
    fun dismissedSameVersionShowsAgainAfterRemindInterval() {
        assertTrue(
            UpdatePolicy.shouldShowUpdate(
                forceUpdate = false,
                latestCode = 24,
                dismissedCode = 24,
                dismissedAt = 0L + 1_000L,
                nowMillis = 1_000L + UpdatePolicy.REMIND_INTERVAL_MS,
            )
        )
    }
}
