package com.example.ui

import com.example.data.local.CourseEntity

enum class ScheduleStatusType {
    IN_CLASS,       // Currently inside a class period
    IN_RECESS,      // In break between classes (short recess, long recess, lunch, dinner)
    BEFORE_SCHOOL,  // Before the day's first class
    AFTER_SCHOOL,   // After all classes of the day have finished
    NO_CLASS_TODAY  // No courses scheduled for today
}

data class RealtimeClassStatus(
    val statusType: ScheduleStatusType = ScheduleStatusType.BEFORE_SCHOOL,
    val currentCourse: CourseEntity? = null,
    val nextCourse: CourseEntity? = null,
    val statusTitle: String = "正在同步时间...",
    val statusSubtitle: String = "",
    val timeRemainingText: String = "",
    val progress: Float = 0f, // 0.0f to 1.0f
    val recessType: String = "" // "课间休息", "大课间", "午休", "晚餐时间"
)

data class WatchScheduleUiState(
    val selectedDayOfWeek: Int = 1, // 1 = Monday .. 7 = Sunday
    val todayDayOfWeek: Int = 1, // Actual day of the week according to system calendar
    val holidayOverrideDayOfWeek: Int? = null, // If set, today executes this day's courses (e.g. 周二上周三课)
    val currentTimeString: String = "00:00",
    val currentSecondsString: String = "00",
    val currentDateString: String = "", // e.g. "9月25日 星期四"
    val courses: List<CourseEntity> = emptyList(), // Courses for selected day
    val todayCourses: List<CourseEntity> = emptyList(), // Courses for today (accounting for holiday override)
    val realtimeStatus: RealtimeClassStatus = RealtimeClassStatus(),
    val isWatchFaceMode: Boolean = false, // Toggle full watchface view
    val isPureBlackAmoled: Boolean = true, // Watch ultra-clean OLED black vs soft dark/light
    val editingCourse: CourseEntity? = null, // Dialog course being edited
    val isCreatingNewCourse: Boolean = false,
    val showResetConfirmDialog: Boolean = false,
    val showHolidayOverrideDialog: Boolean = false,
    val messageSnackbar: String? = null
) {
    /**
     * The day of the week currently being used for today's real-time schedule tracking.
     * Defaults to todayDayOfWeek unless temporarily overridden for holidays/shift classes.
     */
    val effectiveTodayDayOfWeek: Int
        get() = holidayOverrideDayOfWeek ?: todayDayOfWeek

    val isHolidayOverridden: Boolean
        get() = holidayOverrideDayOfWeek != null && holidayOverrideDayOfWeek != todayDayOfWeek
}
