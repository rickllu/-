package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a scheduled course period.
 * dayOfWeek: 1 = Monday, 2 = Tuesday, ..., 7 = Sunday
 */
@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayOfWeek: Int, // 1..7 (1 = Monday, 7 = Sunday)
    val periodIndex: Int, // 1, 2, 3...
    val subject: String, // e.g. "语文", "数学", "英语"
    val startTime: String, // "07:00" (HH:mm)
    val endTime: String, // "07:30" (HH:mm)
    val note: String = "", // e.g. "早自习30min", "之后是大课间"
    val classroom: String = "", // optional e.g. "高三(1)班" or "实验楼"
    val colorHex: String = "" // color theme for subject
)
