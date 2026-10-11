package com.example.data.repository

import com.example.data.DefaultScheduleData
import com.example.data.local.CourseDao
import com.example.data.local.CourseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CourseRepository(private val courseDao: CourseDao) {

    val allCourses: Flow<List<CourseEntity>> = courseDao.getAllCourses()

    fun getCoursesForDay(dayOfWeek: Int): Flow<List<CourseEntity>> =
        courseDao.getCoursesForDay(dayOfWeek)

    suspend fun ensureDefaultCourses() = withContext(Dispatchers.IO) {
        val count = courseDao.getCourseCount()
        if (count == 0) {
            courseDao.insertCourses(DefaultScheduleData.getDefaultCourses())
        }
    }

    suspend fun resetToDefault() = withContext(Dispatchers.IO) {
        courseDao.clearAll()
        courseDao.insertCourses(DefaultScheduleData.getDefaultCourses())
    }

    suspend fun insertCourse(course: CourseEntity) = withContext(Dispatchers.IO) {
        courseDao.insertCourse(course)
    }

    suspend fun updateCourse(course: CourseEntity) = withContext(Dispatchers.IO) {
        courseDao.updateCourse(course)
    }

    suspend fun deleteCourse(course: CourseEntity) = withContext(Dispatchers.IO) {
        courseDao.deleteCourse(course)
    }

    suspend fun deleteCourseById(id: Long) = withContext(Dispatchers.IO) {
        courseDao.deleteCourseById(id)
    }
}
