package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.CourseEntity
import com.example.ui.components.CourseCard
import com.example.ui.components.DaySelector
import com.example.ui.components.EditCourseDialog
import com.example.ui.components.HolidayOverrideDialog
import com.example.ui.components.TimeStatusHeader
import com.example.ui.components.WatchFaceView
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchScheduleScreen(
    viewModel: WatchScheduleViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Handle snackbar messages
    LaunchedEffect(uiState.messageSnackbar) {
        uiState.messageSnackbar?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Auto-scroll to active course if today is selected
    LaunchedEffect(uiState.selectedDayOfWeek, uiState.realtimeStatus.currentCourse?.id) {
        if (uiState.selectedDayOfWeek == uiState.effectiveTodayDayOfWeek) {
            val activeId = uiState.realtimeStatus.currentCourse?.id
            val activeIndex = uiState.courses.indexOfFirst { it.id == activeId }
            if (activeIndex >= 0) {
                // +3 accounts for TimeStatusHeader, DaySelector, and TitleRow
                listState.animateScrollToItem((activeIndex + 3).coerceAtLeast(0))
            }
        }
    }

    val effectiveDayName = WatchScheduleViewModel.dayName(uiState.effectiveTodayDayOfWeek)
    val actualDayName = WatchScheduleViewModel.dayName(uiState.todayDayOfWeek)

    // Fullscreen Watch Face Mode
    if (uiState.isWatchFaceMode) {
        BackHandler {
            viewModel.toggleWatchFaceMode()
        }
        WatchFaceView(
            currentTime = uiState.currentTimeString,
            currentSeconds = uiState.currentSecondsString,
            currentDate = uiState.currentDateString,
            realtimeStatus = uiState.realtimeStatus,
            todayCourses = uiState.todayCourses,
            isHolidayOverridden = uiState.isHolidayOverridden,
            effectiveDayName = effectiveDayName,
            onCloseWatchMode = { viewModel.toggleWatchFaceMode() },
            onOpenHolidayDialog = { viewModel.showHolidayDialog(true) },
            onEditCourse = { viewModel.openEditCourseDialog(it) }
        )
        return
    }

    val isViewingEffectiveToday = uiState.selectedDayOfWeek == uiState.effectiveTodayDayOfWeek
    val currentCourseId = if (isViewingEffectiveToday) uiState.realtimeStatus.currentCourse?.id else null
    val nextCourseId = if (isViewingEffectiveToday) uiState.realtimeStatus.nextCourse?.id else null

    // Determine current time in minutes to detect past courses
    val cal = Calendar.getInstance()
    val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

    val dayNames = listOf("", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")
    val selectedDayName = dayNames.getOrElse(uiState.selectedDayOfWeek) { "星期一" }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030712)),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Watch,
                                    contentDescription = "Watch Schedule",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "手表课程表",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    // Quick Holiday Switch action button
                    IconButton(
                        onClick = { viewModel.showHolidayDialog(true) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("appbar_holiday_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EventRepeat,
                            contentDescription = "调课/换课",
                            tint = if (uiState.isHolidayOverridden) Color(0xFFF59E0B) else Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Watch face mode icon
                    IconButton(
                        onClick = { viewModel.toggleWatchFaceMode() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("appbar_watch_mode_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Watch,
                            contentDescription = "表盘模式",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Add new course button
                    IconButton(
                        onClick = { viewModel.openAddNewCourseDialog() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("appbar_add_course_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加课程",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // More Menu
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("more_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "更多选项",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(Color(0xFF0F172A))
                        ) {
                            DropdownMenuItem(
                                text = { Text("今日调课设置 (调休/补课)", color = Color(0xFF38BDF8)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.EventRepeat,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.showHolidayDialog(true)
                                },
                                modifier = Modifier.testTag("menu_holiday_override")
                            )

                            if (uiState.isHolidayOverridden) {
                                DropdownMenuItem(
                                    text = { Text("恢复正常周${actualDayName}课表", color = Color(0xFFF59E0B)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.RestartAlt,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B)
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        viewModel.setHolidayOverrideDay(null)
                                    }
                                )
                            }

                            DropdownMenuItem(
                                text = { Text("重置为默认课程表 (40分/节)", color = Color(0xFFE2E8F0)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.RestartAlt,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.showResetDialog(true)
                                },
                                modifier = Modifier.testTag("menu_reset_schedule")
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (isViewingEffectiveToday) "当前正在查看今日" else "返回今日课程",
                                        color = Color(0xFF38BDF8)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.selectToday()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF030712),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddNewCourseDialog() },
                containerColor = Color(0xFF38BDF8),
                contentColor = Color(0xFF0F172A),
                shape = CircleShape,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("fab_add_course")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加课程",
                    modifier = Modifier.size(22.dp)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF030712)
    ) { innerPadding ->
        // Single unified LazyColumn so the entire watch screen scrolls freely!
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 90.dp
            ),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Item 1: Realtime Watch Time & Class Status Hero Card
            item(key = "header_time_status") {
                TimeStatusHeader(
                    currentTime = uiState.currentTimeString,
                    currentSeconds = uiState.currentSecondsString,
                    currentDate = uiState.currentDateString,
                    realtimeStatus = uiState.realtimeStatus,
                    isTodaySelected = isViewingEffectiveToday,
                    isHolidayOverridden = uiState.isHolidayOverridden,
                    effectiveDayName = effectiveDayName,
                    onReturnTodayClick = { viewModel.selectToday() },
                    onWatchModeClick = { viewModel.toggleWatchFaceMode() },
                    onHolidayClick = { viewModel.showHolidayDialog(true) }
                )
            }

            // Item 2: Day Selector Tabs (Mon - Sun)
            item(key = "header_day_selector") {
                DaySelector(
                    selectedDay = uiState.selectedDayOfWeek,
                    todayDay = uiState.effectiveTodayDayOfWeek,
                    onDaySelected = { viewModel.selectDay(it) }
                )
            }

            // Item 3: Day Course Schedule Title & Quick Jump indicator
            item(key = "header_schedule_title") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$selectedDayName 课程",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.isHolidayOverridden && isViewingEffectiveToday) Color(0xFFF59E0B) else Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.isHolidayOverridden && isViewingEffectiveToday) {
                                "(调课·按周$effectiveDayName · 40分/节)"
                            } else {
                                "(${uiState.courses.size}节 · 40分/节)"
                            },
                            fontSize = 11.sp,
                            color = if (uiState.isHolidayOverridden && isViewingEffectiveToday) Color(0xFFF59E0B) else Color(0xFF94A3B8)
                        )
                    }

                    if (isViewingEffectiveToday && currentCourseId != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF22C55E).copy(alpha = 0.15f),
                            modifier = Modifier.clickable {
                                val activeIdx = uiState.courses.indexOfFirst { it.id == currentCourseId }
                                if (activeIdx >= 0) {
                                    scope.launch {
                                        listState.animateScrollToItem(activeIdx + 3)
                                    }
                                }
                            }
                        ) {
                            Text(
                                text = "定位当前",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF22C55E),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Empty state if no courses
            if (uiState.courses.isEmpty()) {
                item(key = "empty_state") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF1E293B),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "该日暂无安排课程",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE2E8F0)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "自由自习或休息，也可点击右下角+添加",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            } else {
                // List of Course Cards
                items(
                    items = uiState.courses,
                    key = { "course_${it.dayOfWeek}_${it.periodIndex}_${it.id}" }
                ) { course ->
                    val isCurrent = isViewingEffectiveToday && course.id == currentCourseId
                    val isNext = isViewingEffectiveToday && course.id == nextCourseId
                    val isPast = isViewingEffectiveToday && (WatchScheduleViewModel.parseTimeToMinutes(course.endTime) < currentMinutes)

                    Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                        CourseCard(
                            course = course,
                            isCurrent = isCurrent,
                            isNext = isNext,
                            isPast = isPast,
                            onEditClick = { viewModel.openEditCourseDialog(course) }
                        )
                    }
                }
            }
        }
    }

    // Holiday / Shift Schedule Override Dialog
    if (uiState.showHolidayOverrideDialog) {
        HolidayOverrideDialog(
            actualTodayDay = uiState.todayDayOfWeek,
            currentOverrideDay = uiState.holidayOverrideDayOfWeek,
            onSelectDay = { day -> viewModel.setHolidayOverrideDay(day) },
            onDismiss = { viewModel.showHolidayDialog(false) }
        )
    }

    // Edit/Add Course Dialog
    uiState.editingCourse?.let { course ->
        EditCourseDialog(
            course = course,
            isNew = uiState.isCreatingNewCourse,
            onSave = { updated -> viewModel.saveCourse(updated) },
            onDelete = { toDelete -> viewModel.deleteCourse(toDelete) },
            onDismiss = { viewModel.dismissEditDialog() }
        )
    }

    // Reset Confirmation Dialog
    if (uiState.showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showResetDialog(false) },
            title = {
                Text("恢复默认课程表？", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "这将把所有周一至周日的课程表重置回标准40分钟每节课设置（包括大课间、午休、周六周日特殊时间）。您所作的自定义修改将被覆盖。",
                    color = Color(0xFFCBD5E1)
                )
            },
            containerColor = Color(0xFF0F172A),
            confirmButton = {
                TextButton(
                    onClick = { viewModel.resetToDefaultSchedule() },
                    modifier = Modifier.testTag("confirm_reset_button")
                ) {
                    Text("确认恢复", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showResetDialog(false) }) {
                    Text("取消", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}
