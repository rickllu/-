package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("手表课表", appName)
  }

  @Test
  fun `verify default courses count and 40-min duration`() {
    val courses = com.example.data.DefaultScheduleData.getDefaultCourses()
    // Mon(13) + Tue(13) + Wed(13) + Thu(13) + Fri(13) + Sat(10) + Sun(4) = 79 courses
    assertEquals(79, courses.size)

    // Check weekday normal class duration: 07:40 to 08:20 is exactly 40 minutes
    val mondayPeriod2 = courses.first { it.dayOfWeek == 1 && it.periodIndex == 2 }
    assertEquals("07:40", mondayPeriod2.startTime)
    assertEquals("08:20", mondayPeriod2.endTime)

    // Check Saturday schedule with 20min big recess: period 4 starts at 09:30, ends at 10:10
    val satPeriod4 = courses.first { it.dayOfWeek == 6 && it.periodIndex == 4 }
    assertEquals("09:30", satPeriod4.startTime)
    assertEquals("10:10", satPeriod4.endTime)

    // Check Sunday schedule: 4 afternoon periods of 40 minutes each
    val sunCourses = courses.filter { it.dayOfWeek == 7 }
    assertEquals(4, sunCourses.size)
    assertEquals("14:30", sunCourses[0].startTime)
    assertEquals("15:10", sunCourses[0].endTime)
    assertEquals("17:40", sunCourses[3].endTime)
  }

  @Test
  fun `verify holiday override switches courses correctly`() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.WatchScheduleViewModel(context)

    // Override today to Wednesday (3)
    viewModel.setHolidayOverrideDay(3)
    val stateWithOverride = viewModel.uiState.value
    assertEquals(3, stateWithOverride.effectiveTodayDayOfWeek)
    assertEquals(true, stateWithOverride.isHolidayOverridden)
    assertEquals(3, stateWithOverride.selectedDayOfWeek)

    // Reset back to normal
    viewModel.setHolidayOverrideDay(null)
    val stateReset = viewModel.uiState.value
    assertEquals(null, stateReset.holidayOverrideDayOfWeek)
    assertEquals(false, stateReset.isHolidayOverridden)
  }

  @Test
  fun `verify activity launch`() {
    val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).create()
    org.junit.Assert.assertNotNull(controller.get())
  }
}
