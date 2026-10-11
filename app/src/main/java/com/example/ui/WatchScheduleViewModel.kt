package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DefaultScheduleData
import com.example.data.local.AppDatabase
import com.example.data.local.CourseEntity
import com.example.data.repository.CourseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class WatchScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CourseRepository
    private val prefs = application.getSharedPreferences("watch_schedule_prefs", Context.MODE_PRIVATE)

    // In-memory cache for instant zero-latency UI rendering and day switching on watches
    private var cachedAllCourses: List<CourseEntity> = DefaultScheduleData.getDefaultCourses()

    private val _uiState: MutableStateFlow<WatchScheduleUiState>
    val uiState: StateFlow<WatchScheduleUiState>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = CourseRepository(database.courseDao())

        val actualToday = getCalendarDayOfWeek()

        // Check if there is an active holiday schedule override for today
        val savedDate = prefs.getString("override_date", "")
        val todayDateKey = getTodayDateKey()
        val savedOverrideDay = if (savedDate == todayDateKey) {
            val d = prefs.getInt("override_day", -1)
            if (d in 1..7) d else null
        } else null

        val effectiveToday = savedOverrideDay ?: actualToday

        val initialCourses = filterForDay(cachedAllCourses, effectiveToday)
        val initialTodayCourses = initialCourses

        val initialTimeString = getCurrentTimeString()
        val initialSecondsString = getCurrentSecondsString()
        val initialDateString = getCurrentDateString(actualToday)

        _uiState = MutableStateFlow(
            WatchScheduleUiState(
                selectedDayOfWeek = effectiveToday,
                todayDayOfWeek = actualToday,
                holidayOverrideDayOfWeek = savedOverrideDay,
                currentTimeString = initialTimeString,
                currentSecondsString = initialSecondsString,
                currentDateString = initialDateString,
                courses = initialCourses,
                todayCourses = initialTodayCourses
            )
        )
        uiState = _uiState.asStateFlow()

        // Perform initial status calculation right away
        recalculateRealtimeStatus()

        // Start Room observer in background to load any user modifications
        viewModelScope.launch(Dispatchers.IO) {
            repository.ensureDefaultCourses()
            observeSchedule()
        }

        // Start real-time second ticker
        startTimeTicker()
    }

    private fun getTodayDateKey(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }

    private fun filterForDay(list: List<CourseEntity>, dayOfWeek: Int): List<CourseEntity> {
        return list.filter { it.dayOfWeek == dayOfWeek }
            .sortedWith(compareBy({ it.periodIndex }, { parseTimeToMinutes(it.startTime) }))
    }

    private fun getCalendarDayOfWeek(): Int {
        val cal = Calendar.getInstance()
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    private fun observeSchedule() {
        viewModelScope.launch {
            repository.allCourses.collect { allList ->
                if (allList.isNotEmpty()) {
                    cachedAllCourses = allList
                    updateCoursesFromCache()
                    recalculateRealtimeStatus()
                }
            }
        }
    }

    private fun updateCoursesFromCache() {
        val selectedDay = _uiState.value.selectedDayOfWeek
        val effectiveToday = _uiState.value.effectiveTodayDayOfWeek

        val selectedCourses = filterForDay(cachedAllCourses, selectedDay)
        val todayCourses = filterForDay(cachedAllCourses, effectiveToday)

        _uiState.update { current ->
            current.copy(
                courses = selectedCourses,
                todayCourses = todayCourses
            )
        }
    }

    private fun startTimeTicker() {
        viewModelScope.launch {
            while (isActive) {
                updateTimeStrings()
                recalculateRealtimeStatus()
                delay(1000)
            }
        }
    }

    private fun getCurrentTimeString(): String {
        val cal = Calendar.getInstance()
        return String.format(Locale.getDefault(), "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
    }

    private fun getCurrentSecondsString(): String {
        val cal = Calendar.getInstance()
        return String.format(Locale.getDefault(), "%02d", cal.get(Calendar.SECOND))
    }

    private fun getCurrentDateString(dayOfWeekToday: Int): String {
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val dayName = dayName(dayOfWeekToday)
        return String.format(Locale.getDefault(), "%d月%d日 %s", month, day, dayName)
    }

    private fun updateTimeStrings() {
        val timeFormatted = getCurrentTimeString()
        val secFormatted = getCurrentSecondsString()
        val dayOfWeekToday = getCalendarDayOfWeek()
        val dateFormatted = getCurrentDateString(dayOfWeekToday)

        _uiState.update { current ->
            val hasDayChanged = current.todayDayOfWeek != dayOfWeekToday
            current.copy(
                currentTimeString = timeFormatted,
                currentSecondsString = secFormatted,
                currentDateString = dateFormatted,
                todayDayOfWeek = dayOfWeekToday,
                selectedDayOfWeek = if (hasDayChanged && current.holidayOverrideDayOfWeek == null) {
                    dayOfWeekToday
                } else current.selectedDayOfWeek
            )
        }
    }

    private fun recalculateRealtimeStatus() {
        val todayCourses = _uiState.value.todayCourses
        val cal = Calendar.getInstance()
        val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val currentSecond = cal.get(Calendar.SECOND)

        if (todayCourses.isEmpty()) {
            _uiState.update {
                it.copy(
                    realtimeStatus = RealtimeClassStatus(
                        statusType = ScheduleStatusType.NO_CLASS_TODAY,
                        statusTitle = "今日暂无课程",
                        statusSubtitle = "自由自习或休息时间",
                        timeRemainingText = "",
                        progress = 0f
                    )
                )
            }
            return
        }

        val sortedCourses = todayCourses.sortedBy { parseTimeToMinutes(it.startTime) }
        val firstCourse = sortedCourses.first()
        val lastCourse = sortedCourses.last()

        val firstStartMin = parseTimeToMinutes(firstCourse.startTime)
        val lastEndMin = parseTimeToMinutes(lastCourse.endTime)

        // 1. Before School (morning before first course)
        if (currentMinutes < firstStartMin) {
            val minsUntilFirst = firstStartMin - currentMinutes
            _uiState.update {
                it.copy(
                    realtimeStatus = RealtimeClassStatus(
                        statusType = ScheduleStatusType.BEFORE_SCHOOL,
                        nextCourse = firstCourse,
                        statusTitle = "早晨准备 · 尚未上课",
                        statusSubtitle = "首节课: ${firstCourse.subject} (${firstCourse.startTime})",
                        timeRemainingText = "距上课还剩 ${formatMinutes(minsUntilFirst)}",
                        progress = 0f
                    )
                )
            }
            return
        }

        // 2. After School (all courses of today finished)
        if (currentMinutes >= lastEndMin) {
            _uiState.update {
                it.copy(
                    realtimeStatus = RealtimeClassStatus(
                        statusType = ScheduleStatusType.AFTER_SCHOOL,
                        statusTitle = "今日课程已全部结束",
                        statusSubtitle = "辛苦了！明日课程即将开启",
                        timeRemainingText = "课程已完结",
                        progress = 1f
                    )
                )
            }
            return
        }

        // 3. Check if currently inside a course period
        for (course in sortedCourses) {
            val startMin = parseTimeToMinutes(course.startTime)
            val endMin = parseTimeToMinutes(course.endTime)

            if (currentMinutes in startMin until endMin) {
                val totalDurationSec = (endMin - startMin) * 60
                val elapsedSec = (currentMinutes - startMin) * 60 + currentSecond
                val progress = (elapsedSec.toFloat() / totalDurationSec.coerceAtLeast(1)).coerceIn(0f, 1f)

                val minsRemaining = (endMin - currentMinutes)
                val nextCourse = sortedCourses.firstOrNull { parseTimeToMinutes(it.startTime) >= endMin }

                val nextInfo = if (nextCourse != null) "下节: ${nextCourse.subject} (${nextCourse.startTime})" else "这是今日最后一节课"

                _uiState.update {
                    it.copy(
                        realtimeStatus = RealtimeClassStatus(
                            statusType = ScheduleStatusType.IN_CLASS,
                            currentCourse = course,
                            nextCourse = nextCourse,
                            statusTitle = "正在上课: ${course.subject}",
                            statusSubtitle = nextInfo,
                            timeRemainingText = "还剩 $minsRemaining 分钟下课",
                            progress = progress
                        )
                    )
                }
                return
            }
        }

        // 4. In recess between classes
        var prevCourse: CourseEntity? = null
        var nextCourse: CourseEntity? = null

        for (i in 0 until sortedCourses.size - 1) {
            val c1 = sortedCourses[i]
            val c2 = sortedCourses[i + 1]
            val c1End = parseTimeToMinutes(c1.endTime)
            val c2Start = parseTimeToMinutes(c2.startTime)

            if (currentMinutes in c1End until c2Start) {
                prevCourse = c1
                nextCourse = c2
                break
            }
        }

        if (nextCourse != null && prevCourse != null) {
            val breakStart = parseTimeToMinutes(prevCourse.endTime)
            val breakEnd = parseTimeToMinutes(nextCourse.startTime)
            val totalBreakSec = (breakEnd - breakStart) * 60
            val elapsedSec = (currentMinutes - breakStart) * 60 + currentSecond
            val progress = (elapsedSec.toFloat() / totalBreakSec.coerceAtLeast(1)).coerceIn(0f, 1f)
            val minsUntilNext = breakEnd - currentMinutes

            val breakTitle = when {
                prevCourse.note.contains("大课间") || (breakEnd - breakStart) in 20..35 -> "大课间休息"
                prevCourse.note.contains("午休") || (breakEnd - breakStart) >= 60 && currentMinutes in 720..870 -> "午休时间"
                prevCourse.note.contains("晚餐") || (breakEnd - breakStart) >= 40 && currentMinutes in 1050..1140 -> "晚餐休息时间"
                else -> "课间休息"
            }

            _uiState.update {
                it.copy(
                    realtimeStatus = RealtimeClassStatus(
                        statusType = ScheduleStatusType.IN_RECESS,
                        currentCourse = prevCourse,
                        nextCourse = nextCourse,
                        statusTitle = breakTitle,
                        statusSubtitle = "下一节: ${nextCourse.subject} (${nextCourse.startTime})",
                        timeRemainingText = "距上课还剩 ${formatMinutes(minsUntilNext)}",
                        progress = progress,
                        recessType = breakTitle
                    )
                )
            }
        }
    }

    private fun formatMinutes(minutes: Int): String {
        return if (minutes >= 60) {
            val h = minutes / 60
            val m = minutes % 60
            if (m > 0) "${h}小时${m}分钟" else "${h}小时"
        } else {
            "${minutes}分钟"
        }
    }

    /**
     * Instant zero-latency day selection using in-memory cache
     */
    fun selectDay(dayOfWeek: Int) {
        val selectedCourses = filterForDay(cachedAllCourses, dayOfWeek)
        _uiState.update { it.copy(selectedDayOfWeek = dayOfWeek, courses = selectedCourses) }
    }

    fun selectToday() {
        val effectiveToday = _uiState.value.effectiveTodayDayOfWeek
        selectDay(effectiveToday)
    }

    /**
     * Set Holiday / Shift schedule override (e.g. 周二上周三课)
     * If dayOfWeek == null, restores normal schedule according to the calendar
     */
    fun setHolidayOverrideDay(dayOfWeek: Int?) {
        if (dayOfWeek == null) {
            prefs.edit().remove("override_day").remove("override_date").apply()
            val actualToday = _uiState.value.todayDayOfWeek
            val selectedCourses = filterForDay(cachedAllCourses, actualToday)
            val todayCourses = selectedCourses
            _uiState.update {
                it.copy(
                    holidayOverrideDayOfWeek = null,
                    selectedDayOfWeek = actualToday,
                    courses = selectedCourses,
                    todayCourses = todayCourses,
                    showHolidayOverrideDialog = false
                )
            }
            showSnackbar("已恢复正常课程表 (星期${dayName(actualToday)})")
        } else {
            prefs.edit()
                .putInt("override_day", dayOfWeek)
                .putString("override_date", getTodayDateKey())
                .apply()
            val selectedCourses = filterForDay(cachedAllCourses, dayOfWeek)
            val todayCourses = selectedCourses
            _uiState.update {
                it.copy(
                    holidayOverrideDayOfWeek = dayOfWeek,
                    selectedDayOfWeek = dayOfWeek,
                    courses = selectedCourses,
                    todayCourses = todayCourses,
                    showHolidayOverrideDialog = false
                )
            }
            showSnackbar("已开启调课：今日按照 星期${dayName(dayOfWeek)} 课表进行")
        }
        recalculateRealtimeStatus()
    }

    fun showHolidayDialog(show: Boolean) {
        _uiState.update { it.copy(showHolidayOverrideDialog = show) }
    }

    fun toggleWatchFaceMode() {
        _uiState.update { it.copy(isWatchFaceMode = !it.isWatchFaceMode) }
    }

    fun setPureBlackAmoled(pureBlack: Boolean) {
        _uiState.update { it.copy(isPureBlackAmoled = pureBlack) }
    }

    fun openEditCourseDialog(course: CourseEntity) {
        _uiState.update { it.copy(editingCourse = course, isCreatingNewCourse = false) }
    }

    fun openAddNewCourseDialog() {
        val nextPeriod = (_uiState.value.courses.maxOfOrNull { it.periodIndex } ?: 0) + 1
        val newCourse = CourseEntity(
            id = 0,
            dayOfWeek = _uiState.value.selectedDayOfWeek,
            periodIndex = nextPeriod,
            subject = "自习",
            startTime = "08:00",
            endTime = "08:40",
            note = "",
            colorHex = "#4F46E5"
        )
        _uiState.update { it.copy(editingCourse = newCourse, isCreatingNewCourse = true) }
    }

    fun dismissEditDialog() {
        _uiState.update { it.copy(editingCourse = null, isCreatingNewCourse = false) }
    }

    fun saveCourse(course: CourseEntity) {
        viewModelScope.launch {
            if (_uiState.value.isCreatingNewCourse) {
                repository.insertCourse(course)
                showSnackbar("已添加课程: ${course.subject}")
            } else {
                repository.updateCourse(course)
                showSnackbar("已更新课程: ${course.subject}")
            }
            dismissEditDialog()
        }
    }

    fun deleteCourse(course: CourseEntity) {
        viewModelScope.launch {
            repository.deleteCourse(course)
            showSnackbar("已删除 ${course.subject}")
            dismissEditDialog()
        }
    }

    fun showResetDialog(show: Boolean) {
        _uiState.update { it.copy(showResetConfirmDialog = show) }
    }

    fun resetToDefaultSchedule() {
        viewModelScope.launch {
            repository.resetToDefault()
            cachedAllCourses = DefaultScheduleData.getDefaultCourses()
            updateCoursesFromCache()
            recalculateRealtimeStatus()
            _uiState.update { it.copy(showResetConfirmDialog = false) }
            showSnackbar("已重置为默认课程表 (40分/节)")
        }
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(messageSnackbar = message) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(messageSnackbar = null) }
    }

    companion object {
        fun dayName(dayOfWeek: Int): String = when (dayOfWeek) {
            1 -> "一"
            2 -> "二"
            3 -> "三"
            4 -> "四"
            5 -> "五"
            6 -> "六"
            7 -> "日"
            else -> "一"
        }

        fun parseTimeToMinutes(timeStr: String): Int {
            return try {
                val parts = timeStr.trim().split(":", "：")
                if (parts.size >= 2) {
                    val h = parts[0].trim().toIntOrNull() ?: 0
                    val m = parts[1].trim().toIntOrNull() ?: 0
                    h * 60 + m
                } else 0
            } catch (e: Exception) {
                0
            }
        }
    }
}
