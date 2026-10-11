package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CourseEntity
import com.example.ui.RealtimeClassStatus
import com.example.ui.ScheduleStatusType

@Composable
fun WatchFaceView(
    currentTime: String,
    currentSeconds: String,
    currentDate: String,
    realtimeStatus: RealtimeClassStatus,
    todayCourses: List<CourseEntity>,
    isHolidayOverridden: Boolean = false,
    effectiveDayName: String = "",
    onCloseWatchMode: () -> Unit,
    onOpenHolidayDialog: () -> Unit = {},
    onEditCourse: (CourseEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activeCourseIndex = remember(realtimeStatus.currentCourse, todayCourses) {
        val found = todayCourses.indexOfFirst { it.id == realtimeStatus.currentCourse?.id }
        if (found >= 0) found else 0
    }

    var viewingIndex by remember(activeCourseIndex) { mutableIntStateOf(activeCourseIndex) }

    val displayCourse: CourseEntity? = todayCourses.getOrNull(viewingIndex)
    val isRealtimeCurrent = displayCourse != null && displayCourse.id == realtimeStatus.currentCourse?.id

    val progressAnim by animateFloatAsState(
        targetValue = if (isRealtimeCurrent) realtimeStatus.progress else 0f,
        animationSpec = tween(500),
        label = "watchProgress"
    )

    val accentColor = when (realtimeStatus.statusType) {
        ScheduleStatusType.IN_CLASS -> Color(0xFF22C55E)
        ScheduleStatusType.IN_RECESS -> Color(0xFF38BDF8)
        ScheduleStatusType.BEFORE_SCHOOL -> Color(0xFFF59E0B)
        ScheduleStatusType.AFTER_SCHOOL -> Color(0xFFA855F7)
        ScheduleStatusType.NO_CLASS_TODAY -> Color(0xFF94A3B8)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("watch_face_view")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close Action Top Bar with Holiday shift button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Holiday status / toggle
                    if (isHolidayOverridden) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                            modifier = Modifier
                                .clickable { onOpenHolidayDialog() }
                                .testTag("watchface_holiday_badge")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.EventRepeat,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "按周$effectiveDayName",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF59E0B)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "表盘模式",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                    }

                    IconButton(
                        onClick = onCloseWatchMode,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .testTag("close_watch_face_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "返回普通列表",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Circular Watch Dial Container
            item {
                BoxWithConstraints(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                        .size(280.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF090D16))
                ) {
                    val strokeWidthPx = 12f

                    // Circular Progress Arc
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        // Background Track
                        drawArc(
                            color = Color(0xFF1E293B),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                        )

                        // Active Progress Arc
                        if (progressAnim > 0f) {
                            drawArc(
                                color = accentColor,
                                startAngle = -90f,
                                sweepAngle = 360f * progressAnim,
                                useCenter = false,
                                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // Dial Content
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {
                        // Digital Time
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = currentTime,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                            Text(
                                text = ":$currentSeconds",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                            )
                        }

                        Text(
                            text = if (isHolidayOverridden) "$currentDate (按周$effectiveDayName)" else currentDate,
                            fontSize = 11.sp,
                            color = if (isHolidayOverridden) Color(0xFFF59E0B) else Color(0xFF64748B),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        // Subject Name in Large Glanceable Typography
                        if (displayCourse != null) {
                            Text(
                                text = displayCourse.subject,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isRealtimeCurrent) Color.White else Color(0xFFE2E8F0)
                            )

                            // Period & Time tag
                            Text(
                                text = "第${displayCourse.periodIndex}节 · ${displayCourse.startTime}-${displayCourse.endTime}",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.SemiBold
                            )

                            if (displayCourse.note.isNotEmpty()) {
                                Text(
                                    text = displayCourse.note,
                                    fontSize = 11.sp,
                                    color = Color(0xFFF59E0B),
                                    modifier = Modifier.padding(top = 1.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "无课程",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        // Realtime Status or Remaining Time
                        if (isRealtimeCurrent && realtimeStatus.timeRemainingText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = accentColor.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = realtimeStatus.timeRemainingText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Navigation controls between periods on the watch face
                        if (todayCourses.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                IconButton(
                                    onClick = { if (viewingIndex > 0) viewingIndex-- },
                                    enabled = viewingIndex > 0,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "上一节",
                                        tint = if (viewingIndex > 0) Color.White else Color(0xFF334155),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                Text(
                                    text = "${viewingIndex + 1}/${todayCourses.size}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )

                                IconButton(
                                    onClick = { if (viewingIndex < todayCourses.size - 1) viewingIndex++ },
                                    enabled = viewingIndex < todayCourses.size - 1,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "下一节",
                                        tint = if (viewingIndex < todayCourses.size - 1) Color.White else Color(0xFF334155),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Downward indicator hint
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "下滑查看今日全部课程",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "下滑查看今日课表 (${todayCourses.size}节 · 40分/节)",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // Today's full course list accessible directly under the watch face!
            items(
                items = todayCourses,
                key = { "watchface_${it.dayOfWeek}_${it.periodIndex}_${it.id}" }
            ) { course ->
                val isCurrent = course.id == realtimeStatus.currentCourse?.id
                val isNext = course.id == realtimeStatus.nextCourse?.id

                CourseCard(
                    course = course,
                    isCurrent = isCurrent,
                    isNext = isNext,
                    isPast = false,
                    onEditClick = { onEditCourse(course) },
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }

            // Safe bottom padding for watch bezel
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
