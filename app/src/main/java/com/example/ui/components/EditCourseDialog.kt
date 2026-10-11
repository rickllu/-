package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.DefaultScheduleData
import com.example.data.local.CourseEntity
import com.example.ui.WatchScheduleViewModel
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditCourseDialog(
    course: CourseEntity,
    isNew: Boolean,
    onSave: (CourseEntity) -> Unit,
    onDelete: (CourseEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var subject by remember { mutableStateOf(course.subject) }
    var startTime by remember { mutableStateOf(course.startTime) }
    var endTime by remember { mutableStateOf(course.endTime) }
    var note by remember { mutableStateOf(course.note) }
    var periodIndexStr by remember { mutableStateOf(course.periodIndex.toString()) }
    var classroom by remember { mutableStateOf(course.classroom) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val quickSubjects = listOf(
        "语文", "数学", "英语", "物理", "化学", "生物",
        "自习", "体育", "班会", "定时训练", "历史", "地理", "政治"
    )

    fun calculateEndTime(start: String, durationMins: Int): String {
        val startMins = WatchScheduleViewModel.parseTimeToMinutes(start)
        val endMins = startMins + durationMins
        val h = (endMins / 60) % 24
        val m = endMins % 60
        return String.format(Locale.getDefault(), "%02d:%02d", h, m)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("edit_course_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Dialog Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isNew) "添加课程" else "编辑课程",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (!isNew) {
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.testTag("delete_course_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "删除该课程",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Subject Name Field
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("科目名称", color = Color(0xFF94A3B8)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        cursorColor = Color(0xFF38BDF8)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Subject Choice Chips
                Text(
                    text = "快捷科目选择:",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    quickSubjects.forEach { subj ->
                        val isSelected = subject == subj
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E293B),
                            modifier = Modifier.clickable { subject = subj }
                        ) {
                            Text(
                                text = subj,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF0F172A) else Color(0xFFE2E8F0),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time Setting Rows: Start Time & End Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("开始时间", color = Color(0xFF94A3B8)) },
                        placeholder = { Text("07:40") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            cursorColor = Color(0xFF38BDF8)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("start_time_input")
                    )

                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("结束时间", color = Color(0xFF94A3B8)) },
                        placeholder = { Text("08:20") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            cursorColor = Color(0xFF38BDF8)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("end_time_input")
                    )
                }

                // Quick Duration Fillers
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "快速时长:", fontSize = 11.sp, color = Color(0xFF64748B))
                    listOf(40, 30, 45).forEach { mins ->
                        val isDefault40 = mins == 40
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isDefault40) Color(0xFF0284C7).copy(alpha = 0.35f) else Color(0xFF1E293B),
                            modifier = Modifier.clickable {
                                endTime = calculateEndTime(startTime, mins)
                            }
                        ) {
                            Text(
                                text = if (isDefault40) "+40分(单节课)" else "+${mins}分",
                                fontSize = 11.sp,
                                fontWeight = if (isDefault40) FontWeight.Bold else FontWeight.Normal,
                                color = if (isDefault40) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Period Index & Note
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = periodIndexStr,
                        onValueChange = { periodIndexStr = it },
                        label = { Text("第几节", color = Color(0xFF94A3B8)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            cursorColor = Color(0xFF38BDF8)
                        ),
                        modifier = Modifier
                            .weight(0.8f)
                            .testTag("period_index_input")
                    )

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("备注/大课间", color = Color(0xFF94A3B8)) },
                        placeholder = { Text("如: 之后是大课间") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            cursorColor = Color(0xFF38BDF8)
                        ),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("note_input")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Cancel & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_edit_button")
                    ) {
                        Text("取消", color = Color(0xFF94A3B8))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val parsedPeriod = periodIndexStr.toIntOrNull() ?: course.periodIndex
                            val color = when {
                                subject.contains("语文") -> "#E11D48"
                                subject.contains("数学") -> "#2563EB"
                                subject.contains("英语") -> "#D97706"
                                subject.contains("物理") -> "#7C3AED"
                                subject.contains("化学") -> "#0D9488"
                                subject.contains("生物") -> "#059669"
                                subject.contains("体育") -> "#DB2777"
                                subject.contains("自习") -> "#4F46E5"
                                subject.contains("定时") -> "#0284C7"
                                else -> "#475569"
                            }
                            val updated = course.copy(
                                subject = subject.ifBlank { "自习" },
                                startTime = startTime.ifBlank { "08:00" },
                                endTime = endTime.ifBlank { "08:45" },
                                note = note,
                                periodIndex = parsedPeriod,
                                classroom = classroom,
                                colorHex = color
                            )
                            onSave(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                        modifier = Modifier.testTag("save_course_button")
                    ) {
                        Text("保存", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除该节课？", color = Color.White) },
            text = { Text("将删除 ${course.subject} (${course.startTime} - ${course.endTime})", color = Color(0xFFCBD5E1)) },
            containerColor = Color(0xFF0F172A),
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(course)
                    }
                ) {
                    Text("删除", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
private fun IconButton(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    androidx.compose.material3.IconButton(onClick = onClick, modifier = modifier) {
        content()
    }
}
