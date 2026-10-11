package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY dayOfWeek ASC, periodIndex ASC, startTime ASC")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE dayOfWeek = :dayOfWeek ORDER BY periodIndex ASC, startTime ASC")
    fun getCoursesForDay(dayOfWeek: Int): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE dayOfWeek = :dayOfWeek ORDER BY periodIndex ASC, startTime ASC")
    suspend fun getCoursesForDaySync(dayOfWeek: Int): List<CourseEntity>

    @Query("SELECT COUNT(*) FROM courses")
    suspend fun getCourseCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Delete
    suspend fun deleteCourse(course: CourseEntity)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteCourseById(id: Long)

    @Query("DELETE FROM courses WHERE dayOfWeek = :dayOfWeek")
    suspend fun deleteCoursesForDay(dayOfWeek: Int)

    @Query("DELETE FROM courses")
    suspend fun clearAll()
}
