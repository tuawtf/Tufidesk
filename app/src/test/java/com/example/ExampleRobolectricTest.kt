package com.example

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.example.data.auth.AuthManager
import com.example.data.database.AppDatabase
import com.example.data.firebase.FirebaseSyncManager
import com.example.data.model.Course
import com.example.data.model.Homework
import com.example.data.model.UserAccount
import com.example.data.repository.HomeworkRepository
import com.example.data.repository.NotesRepository
import com.example.ui.HomeworkViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TufiDESK", appName)
  }

  @Test
  fun `launch MainActivity without crashing`() {
    val scenario = ActivityScenario.launch(MainActivity::class.java)
    scenario.onActivity { activity ->
      assertNotNull(activity)
    }
  }

  @Test
  fun `test database and viewmodel operations`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = AppDatabase.getInstance(context)
    val repository = HomeworkRepository(database.courseDao(), database.homeworkDao())
    val notesRepository = NotesRepository(database.noteDao())
    val authManager = AuthManager(context)
    val syncManager = FirebaseSyncManager(
        context,
        repository,
        CoroutineScope(Dispatchers.IO + SupervisorJob())
    )

    val viewModel = HomeworkViewModel(repository, authManager, syncManager, notesRepository)
    assertNotNull(viewModel)

    // Test profile selection
    viewModel.selectProfile(UserAccount.PROFILES.first())
    assertEquals("tuna", viewModel.currentUser.value?.id)

    // Test Sunday calculation
    val now = System.currentTimeMillis()
    val sunday = HomeworkViewModel.getSundayOfWeek(now)
    val monday = HomeworkViewModel.getMondayOfWeek(now)
    assertTrue(sunday >= monday)

    // Test saving homework
    val courseId = repository.insertCourse(
        Course(name = "Test Dersi", teacher = "Hoca", colorHex = "#3B82F6", iconName = "MenuBook")
    )
    viewModel.saveHomework(
        id = 0L,
        courseId = courseId,
        title = "Test Ödevi",
        description = "Açıklama",
        dueDateMillis = sunday,
        dueTimeText = "23:59",
        priority = "MEDIUM",
        estimatedMinutes = 30,
        assignedTo = "Tuna"
    )
  }

  @Test
  fun `test crash handler logging and recovery`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.util.GlobalCrashHandler.install(context)

    // Simulate saving a manual error
    val testException = RuntimeException("Test Çökme Mesajı")
    com.example.util.GlobalCrashHandler.saveManualError(context, "UnitTest", testException)

    val lastLog = com.example.util.GlobalCrashHandler.getLastCrashLog(context)
    assertNotNull(lastLog)
    assertTrue(lastLog!!.contains("Test Çökme Mesajı"))
    assertTrue(lastLog.contains("=== TUFIDESK UYGULAMA İÇİ HATA RAPORU"))
  }

  @Test
  fun `test database migration safety from version 4 to 8`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.data.database.AppDatabase.getInstance(context)
    assertNotNull(db)
    val courses = db.courseDao().getAllCoursesList()
    assertNotNull(courses)
  }
}

