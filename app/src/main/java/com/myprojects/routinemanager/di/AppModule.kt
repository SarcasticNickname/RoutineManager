package com.myprojects.routinemanager.di

import android.app.Application
import androidx.room.Room
import com.myprojects.routinemanager.data.repository.DayTemplateRepository
import com.myprojects.routinemanager.data.repository.TaskRepository
import com.myprojects.routinemanager.data.room.DayTemplateDao
import com.myprojects.routinemanager.data.room.RoutineManagerDatabase
import com.myprojects.routinemanager.data.room.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): RoutineManagerDatabase {
        val db = Room.databaseBuilder(
            app,
            RoutineManagerDatabase::class.java,
            "routine_manager_db"
        )
            .addCallback(RoutineManagerDatabase.createCallback())
            .fallbackToDestructiveMigration() // безопасно в разработке
            .build()

        // передаём ссылку в companion object, чтобы onCreate мог использовать DAO
        RoutineManagerDatabase.databaseRef = db

        return db
    }

    @Provides
    fun provideTaskDao(db: RoutineManagerDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideDayTemplateDao(db: RoutineManagerDatabase): DayTemplateDao = db.dayTemplateDao()

    @Provides
    fun provideTaskRepository(taskDao: TaskDao): TaskRepository = TaskRepository(taskDao)

    @Provides
    fun provideDayTemplateRepository(
        taskDao: TaskDao,
        dayTemplateDao: DayTemplateDao
    ): DayTemplateRepository = DayTemplateRepository(taskDao, dayTemplateDao)
}
