package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

enum class CognitiveLoad { DEEP_WORK, SHALLOW_WORK }
enum class PlanningHorizon { DAILY, WEEKLY }

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val cognitiveLoad: CognitiveLoad,
    val planningHorizon: PlanningHorizon,
    val isCompleted: Boolean = false
)

class Converters {
    @TypeConverter
    fun fromCognitiveLoad(value: CognitiveLoad): String = value.name

    @TypeConverter
    fun toCognitiveLoad(value: String): CognitiveLoad = enumValueOf(value)

    @TypeConverter
    fun fromPlanningHorizon(value: PlanningHorizon): String = value.name

    @TypeConverter
    fun toPlanningHorizon(value: String): PlanningHorizon = enumValueOf(value)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE planningHorizon = 'WEEKLY' AND isCompleted = 0 ORDER BY id DESC")
    fun getWeeklyTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE planningHorizon = 'DAILY' AND isCompleted = 0 ORDER BY cognitiveLoad ASC, id DESC")
    fun getDailyTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)
}

@Database(entities = [Task::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "dolphin_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}

class TaskRepository(private val taskDao: TaskDao) {
    val weeklyTasks: Flow<List<Task>> = taskDao.getWeeklyTasks()
    val dailyTasks: Flow<List<Task>> = taskDao.getDailyTasks()

    suspend fun insertTask(task: Task) = taskDao.insertTask(task)
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    suspend fun deleteTask(task: Task) = taskDao.deleteTaskById(task.id)
}
