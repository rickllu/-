package com.example.data

import com.example.data.local.CourseEntity

object DefaultScheduleData {

    fun getDefaultCourses(): List<CourseEntity> {
        val list = mutableListOf<CourseEntity>()
        var courseIdCounter = 1L

        // Helper to get matching subject color
        fun getSubjectColor(subject: String): String = when {
            subject.contains("语文") -> "#E11D48" // Rose / Crimson
            subject.contains("数学") -> "#2563EB" // Royal Blue
            subject.contains("英语") -> "#D97706" // Warm Amber
            subject.contains("物理") -> "#7C3AED" // Violet
            subject.contains("化学") -> "#0D9488" // Teal
            subject.contains("生物") -> "#059669" // Emerald Green
            subject.contains("体育") -> "#DB2777" // Pink
            subject.contains("班会") -> "#EA580C" // Orange
            subject.contains("定时") || subject.contains("训练") -> "#0284C7" // Sky Blue
            subject.contains("自习") -> "#4F46E5" // Indigo
            else -> "#475569" // Slate
        }

        // Standard Weekday Timing template (Monday - Friday)
        data class Slot(
            val periodIndex: Int,
            val startTime: String,
            val endTime: String,
            val note: String = ""
        )

        val weekdaySlots = listOf(
            Slot(1, "07:00", "07:30", "早自习30min"),
            Slot(2, "07:40", "08:20", ""),
            Slot(3, "08:30", "09:10", "之后是大课间"),
            Slot(4, "09:40", "10:20", ""),
            Slot(5, "10:30", "11:10", ""),
            Slot(6, "11:20", "12:00", "之后是午休"),
            Slot(7, "14:30", "15:10", ""),
            Slot(8, "15:20", "16:00", ""),
            Slot(9, "16:10", "16:50", ""),
            Slot(10, "17:00", "17:40", "之后是晚餐时间"),
            Slot(11, "18:40", "19:20", ""),
            Slot(12, "19:30", "20:10", ""),
            Slot(13, "20:20", "21:00", "")
        )

        // 星期一
        val mondaySubjects = listOf(
            "英语", "语文", "英语", "数学", "物理", "生物",
            "化学", "生物", "物理", "数学", "定时训练", "语文", "英语"
        )
        mondaySubjects.forEachIndexed { index, subj ->
            val slot = weekdaySlots[index]
            list.add(
                CourseEntity(
                    id = courseIdCounter++,
                    dayOfWeek = 1,
                    periodIndex = slot.periodIndex,
                    subject = subj,
                    startTime = slot.startTime,
                    endTime = slot.endTime,
                    note = slot.note,
                    colorHex = getSubjectColor(subj)
                )
            )
        }

        // 星期二: 语文 英语 语文 物理 自习 化学 数学 体育 生物，语文 班会 数学 数学
        val tuesdaySubjects = listOf(
            "语文", "英语", "语文", "物理", "自习", "化学",
            "数学", "体育", "生物", "语文", "班会", "数学", "数学"
        )
        tuesdaySubjects.forEachIndexed { index, subj ->
            val slot = weekdaySlots[index]
            list.add(
                CourseEntity(
                    id = courseIdCounter++,
                    dayOfWeek = 2,
                    periodIndex = slot.periodIndex,
                    subject = subj,
                    startTime = slot.startTime,
                    endTime = slot.endTime,
                    note = slot.note,
                    colorHex = getSubjectColor(subj)
                )
            )
        }

        // 星期三: 英语 语文 英语 自习 物理 数学 化学 语文 生物 物理 数学 生物 化学
        val wednesdaySubjects = listOf(
            "英语", "语文", "英语", "自习", "物理", "数学",
            "化学", "语文", "生物", "物理", "数学", "生物", "化学"
        )
        wednesdaySubjects.forEachIndexed { index, subj ->
            val slot = weekdaySlots[index]
            list.add(
                CourseEntity(
                    id = courseIdCounter++,
                    dayOfWeek = 3,
                    periodIndex = slot.periodIndex,
                    subject = subj,
                    startTime = slot.startTime,
                    endTime = slot.endTime,
                    note = slot.note,
                    colorHex = getSubjectColor(subj)
                )
            )
        }

        // 星期四: 语文 语文 英语 数学 化学 生物 数学 英语 化学 物理 定时训练 数学 语文
        val thursdaySubjects = listOf(
            "语文", "语文", "英语", "数学", "化学", "生物",
            "数学", "英语", "化学", "物理", "定时训练", "数学", "语文"
        )
        thursdaySubjects.forEachIndexed { index, subj ->
            val slot = weekdaySlots[index]
            list.add(
                CourseEntity(
                    id = courseIdCounter++,
                    dayOfWeek = 4,
                    periodIndex = slot.periodIndex,
                    subject = subj,
                    startTime = slot.startTime,
                    endTime = slot.endTime,
                    note = slot.note,
                    colorHex = getSubjectColor(subj)
                )
            )
        }

        // 星期五: 英语 数学 英语 化学 自习 语文 生物 物理 英语 体育 物理 化学 生物
        val fridaySubjects = listOf(
            "英语", "数学", "英语", "化学", "自习", "语文",
            "生物", "物理", "英语", "体育", "物理", "化学", "生物"
        )
        fridaySubjects.forEachIndexed { index, subj ->
            val slot = weekdaySlots[index]
            list.add(
                CourseEntity(
                    id = courseIdCounter++,
                    dayOfWeek = 5,
                    periodIndex = slot.periodIndex,
                    subject = subj,
                    startTime = slot.startTime,
                    endTime = slot.endTime,
                    note = slot.note,
                    colorHex = getSubjectColor(subj)
                )
            )
        }

        // 星期六:
        // 数学 英语 英语（由于周六的大课间只有20min,之后的时间都要改，下午的时间一致） 自习 物理 物理 化学 化学 生物 生物（不上晚自习）
        val saturdaySlots = listOf(
            Slot(1, "07:00", "07:30", "早自习30min"),
            Slot(2, "07:40", "08:20", ""),
            Slot(3, "08:30", "09:10", "大课间20min"),
            Slot(4, "09:30", "10:10", "大课间后提早开始"),
            Slot(5, "10:20", "11:00", ""),
            Slot(6, "11:10", "11:50", "之后是午休"),
            Slot(7, "14:30", "15:10", ""),
            Slot(8, "15:20", "16:00", ""),
            Slot(9, "16:10", "16:50", ""),
            Slot(10, "17:00", "17:40", "不上晚自习，今日结束")
        )
        val saturdaySubjects = listOf(
            "数学", "英语", "英语", "自习", "物理", "物理",
            "化学", "化学", "生物", "生物"
        )
        saturdaySubjects.forEachIndexed { index, subj ->
            val slot = saturdaySlots[index]
            list.add(
                CourseEntity(
                    id = courseIdCounter++,
                    dayOfWeek = 6,
                    periodIndex = slot.periodIndex,
                    subject = subj,
                    startTime = slot.startTime,
                    endTime = slot.endTime,
                    note = slot.note,
                    colorHex = getSubjectColor(subj)
                )
            )
        }

        // 星期天:
        // （只上下午）语文 语文 数学 数学
        val sundaySlots = listOf(
            Slot(1, "14:30", "15:10", "下午第1节"),
            Slot(2, "15:20", "16:00", "下午第2节"),
            Slot(3, "16:10", "16:50", "下午第3节"),
            Slot(4, "17:00", "17:40", "下午第4节，今日结束")
        )
        val sundaySubjects = listOf("语文", "语文", "数学", "数学")
        sundaySubjects.forEachIndexed { index, subj ->
            val slot = sundaySlots[index]
            list.add(
                CourseEntity(
                    id = courseIdCounter++,
                    dayOfWeek = 7,
                    periodIndex = slot.periodIndex,
                    subject = subj,
                    startTime = slot.startTime,
                    endTime = slot.endTime,
                    note = slot.note,
                    colorHex = getSubjectColor(subj)
                )
            )
        }

        return list
    }
}
