package com.myprojects.routinemanager.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.myprojects.routinemanager.data.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * DAO (Data Access Object) для доступа к данным о задачах в базе данных.
 */
@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Query("SELECT * FROM tasks WHERE date = :date")
    fun getTasksForDate(date: LocalDate): Flow<List<Task>>

    @Query("DELETE FROM tasks WHERE date = :date")
    suspend fun deleteTasksForDate(date: LocalDate)
}
