package com.example.testzeuitests

import android.content.Context
import androidx.room.*
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Singleton

@Entity
data class SomeData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val value: String = UUID.randomUUID().toString(),
)

@Entity
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String = UUID.randomUUID().toString()
)

@Database(
    entities = [
        SomeData::class,
        User::class
    ],
    version = 1
)
abstract class DummyDatabase : RoomDatabase(), AppDao

interface AppDao {
    fun dummyDao(): DummyDao
}

@Dao
interface DummyDao {
    @Insert
    suspend fun addData(someData: SomeData): Long

    @Insert
    suspend fun addData(someData: Collection<SomeData>)

    @Query("select * from SomeData")
    fun getAllDataFlow(): Flow<List<SomeData>>

    @Insert
    suspend fun addUser(user: User): Long

    @Query("select * from User limit 1")
    fun getUserFlow(): Flow<User?>

    @Query("select * from User limit 1")
    suspend fun getUser(): User

    @Query("delete from SomeData")
    suspend fun deleteAllData()

    @Query("delete from User")
    suspend fun deleteUser()
}

fun interface WorkManagerProvider {
    fun getWorkManager(): WorkManager
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun database(
        @ApplicationContext context: Context
    ): DummyDatabase = Room
        .databaseBuilder(context, DummyDatabase::class.java, "dummy")
        .build()

    @Provides
    @Singleton
    fun dao(dummyDatabase: DummyDatabase): DummyDao = dummyDatabase.dummyDao()

    @Provides
    fun provideWorkManagerProvider(
        @ApplicationContext context: Context
    ): WorkManagerProvider = WorkManagerProvider { WorkManager.getInstance(context) }
}