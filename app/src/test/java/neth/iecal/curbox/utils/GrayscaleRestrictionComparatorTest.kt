package neth.iecal.curbox.utils

import neth.iecal.curbox.data.models.AppTimeConfig
import neth.iecal.curbox.data.models.GrayscaleGroup
import neth.iecal.curbox.data.models.TimeInterval
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GrayscaleRestrictionComparatorTest {
    @Test
    fun emptyEverydayScheduleMeansAllDay() {
        val allDay = group(AppTimeConfig(everydayIntervals = mutableListOf()))
        val limited = group(AppTimeConfig(everydayIntervals = mutableListOf(TimeInterval(9, 0, 10, 0))))
        assertFalse(RestrictionComparator.grayscaleGroups(listOf(allDay), listOf(limited)))
        assertTrue(RestrictionComparator.grayscaleGroups(listOf(limited), listOf(allDay)))
    }

    @Test
    fun missingCustomDaysMeanAllDay() {
        val allDay = group(AppTimeConfig(isEveryday = false))
        val limited = group(AppTimeConfig(isEveryday = false, dailyIntervals = mutableMapOf(
            0 to mutableListOf(TimeInterval(9, 0, 10, 0))
        )))
        assertFalse(RestrictionComparator.grayscaleGroups(listOf(allDay), listOf(limited)))
        assertTrue(RestrictionComparator.grayscaleGroups(listOf(limited), listOf(allDay)))
    }

    @Test
    fun overnightCoverageUsesTheSameDayAsRuntime() {
        val overnight = group(AppTimeConfig(isEveryday = false, dailyIntervals = mutableMapOf(
            0 to mutableListOf(TimeInterval(22, 0, 2, 0))
        )))
        val split = group(AppTimeConfig(isEveryday = false, dailyIntervals = mutableMapOf(
            0 to mutableListOf(TimeInterval(0, 0, 2, 0), TimeInterval(22, 0, 24, 0))
        )))
        assertTrue(RestrictionComparator.grayscaleGroups(listOf(overnight), listOf(split)))
        assertTrue(RestrictionComparator.grayscaleGroups(listOf(split), listOf(overnight)))
    }

    @Test
    fun whitelistGraysOutEverythingExceptSelectedApps() {
        val whitelist = appsGroup(isWhitelistMode = true, "a")
        assertFalse(whitelist.graysOut("a"))
        assertTrue(whitelist.graysOut("com.android.systemui"))
        assertTrue(appsGroup(isWhitelistMode = false, "a").graysOut("a"))
    }

    @Test
    fun fewerAppsInColorIsStricter() {
        val twoInColor = appsGroup(isWhitelistMode = true, "a", "b")
        val oneInColor = appsGroup(isWhitelistMode = true, "a")
        assertTrue(RestrictionComparator.grayscaleGroups(listOf(twoInColor), listOf(oneInColor)))
        assertFalse(RestrictionComparator.grayscaleGroups(listOf(oneInColor), listOf(twoInColor)))
    }

    @Test
    fun switchingToWhitelistIsStricterOnlyIfGrayedAppsStayGray() {
        val grayAB = appsGroup(isWhitelistMode = false, "a", "b")
        assertTrue(RestrictionComparator.grayscaleGroups(listOf(grayAB), listOf(appsGroup(isWhitelistMode = true, "c"))))
        assertFalse(RestrictionComparator.grayscaleGroups(listOf(grayAB), listOf(appsGroup(isWhitelistMode = true, "a"))))
    }

    @Test
    fun leavingWhitelistModeIsLooser() {
        val whitelist = appsGroup(isWhitelistMode = true)
        val manyApps = appsGroup(isWhitelistMode = false, "a", "b", "c")
        assertFalse(RestrictionComparator.grayscaleGroups(listOf(whitelist), listOf(manyApps)))
    }

    private fun group(schedule: AppTimeConfig) = GrayscaleGroup(
        groupId = "group", isActive = true, timeConfig = schedule
    )

    private fun appsGroup(isWhitelistMode: Boolean, vararg packages: String) = GrayscaleGroup(
        groupId = "group", isActive = true, packages = hashSetOf(*packages), isWhitelistMode = isWhitelistMode
    )
}
