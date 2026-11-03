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
    fun turningAllAppsOnIsStricter() {
        val someApps = appsGroup(allApps = false, "a", "b")
        val allApps = appsGroup(allApps = true)
        assertTrue(RestrictionComparator.grayscaleGroups(listOf(someApps), listOf(allApps)))
    }

    @Test
    fun turningAllAppsOffIsLooserEvenWithAppsSelected() {
        val allApps = appsGroup(allApps = true, "a", "b")
        val someApps = appsGroup(allApps = false, "a", "b")
        assertFalse(RestrictionComparator.grayscaleGroups(listOf(allApps), listOf(someApps)))
    }

    @Test
    fun allAppsGroupAppliesToAnyPackage() {
        assertTrue(appsGroup(allApps = true).appliesTo("com.example.any"))
        assertFalse(appsGroup(allApps = false, "a").appliesTo("com.example.any"))
    }

    private fun group(schedule: AppTimeConfig) = GrayscaleGroup(
        groupId = "group", isActive = true, timeConfig = schedule
    )

    private fun appsGroup(allApps: Boolean, vararg packages: String) = GrayscaleGroup(
        groupId = "group", isActive = true, packages = hashSetOf(*packages), allApps = allApps
    )
}
