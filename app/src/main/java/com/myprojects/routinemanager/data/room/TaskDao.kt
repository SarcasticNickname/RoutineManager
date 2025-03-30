package com.myprojects.routinemanager.data.room

import androidx.room.*
import androidx.room.Dao
import androidx.room.Query
import com.myprojects.routinemanager.data.model.Task
import kotlinx.coroutines.flow.Flow

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
}
