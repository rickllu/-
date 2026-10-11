package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CourseEntity

@Composable
fun CourseCard(
    course: CourseEntity,
    isCurrent: Boolean,
    isNext: Boolean,
    isPast: Boolean,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subjectColor = try {
        if (course.colorHex.isNotEmpty()) Color(android.graphics.Color.parseColor(course.colorHex))
        else Color(0xFF38BDF8)
    } catch (e: Exception) {
        Color(0xFF38BDF8)
    }

    val cardBg = when {
        isCurrent -> Color(0xFF132338) // Glowing highlight for active class
        isNext -> Color(0xFF162032) // Slightly elevated for next class
        isPast -> Color(0xFF090E17) // Subtle dimmed for past classes
        else -> Color(0xFF0F172A)
    }

    val cardBorder = when {
        isCurrent -> Color(0xFF38BDF8)
        isNext -> Color(0xFF334155)
        else -> Color(0xFF1E293B)
    }

    val periodLabel = when {
        course.note.contains("早自习") || (course.periodIndex == 1 && course.startTime == "07:00") -> "早自习"
        course.periodIndex in 1..6 -> "${course.periodIndex}"
        course.periodIndex in 7..10 -> "下${course.periodIndex - 6}"
        course.periodIndex in 11..13 -> "晚${course.periodIndex - 10}"
        else -> "${course.periodIndex}"
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = cardBg,
        border = androidx.compose.foundation.BorderStroke(if (isCurrent) 1.5.dp else 1.dp, cardBorder),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEditClick() }
            .testTag("course_card_${course.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Period number chip with subject color
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(subjectColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = periodLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = subjectColor
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Main Info: Subject, Note/Status
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = course.subject,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isCurrent -> Color.White
                            isPast -> Color(0xFF64748B)
                            else -> Color(0xFFF1F5F9)
                        }
                    )

                    // Active badge
                    if (isCurrent) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF22C55E).copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "上课中",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF22C55E),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    } else if (isNext) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF38BDF8).copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "下一节",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                // Note/remark if any (e.g. 大课间, 之后是午休)
                if (course.note.isNotEmpty() && !course.note.contains("早自习")) {
                    Text(
                        text = course.note,
                        fontSize = 10.sp,
                        color = if (course.note.contains("大课间") || course.note.contains("午休")) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                        maxLines = 1
                    )
                }
            }

            // Time interval & quick edit icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${course.startTime}-${course.endTime}",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        isCurrent -> Color(0xFF38BDF8)
                        isPast -> Color(0xFF475569)
                        else -> Color(0xFF94A3B8)
                    }
                )

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("edit_course_btn_${course.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "修改",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
