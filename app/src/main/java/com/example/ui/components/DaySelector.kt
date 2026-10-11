package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DaySelector(
    selectedDay: Int, // 1..7
    todayDay: Int, // 1..7
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = listOf(
        1 to "一",
        2 to "二",
        3 to "三",
        4 to "四",
        5 to "五",
        6 to "六",
        7 to "日"
    )

    Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .testTag("day_selector")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            days.forEach { (dayNum, dayLabel) ->
                val isSelected = dayNum == selectedDay
                val isToday = dayNum == todayDay

                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF38BDF8) else Color.Transparent,
                    animationSpec = tween(150),
                    label = "dayBgColor"
                )

                val textColor = when {
                    isSelected -> Color(0xFF0F172A)
                    isToday -> Color(0xFF38BDF8)
                    else -> Color(0xFF94A3B8)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 1.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgColor)
                        .clickable { onDaySelected(dayNum) }
                        .padding(vertical = 5.dp)
                        .testTag("day_chip_$dayNum"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dayLabel,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )

                        // Small dot for Today
                        if (isToday) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 1.dp)
                                    .size(3.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFF0F172A) else Color(0xFF38BDF8))
                            )
                        }
                    }
                }
            }
        }
    }
}
