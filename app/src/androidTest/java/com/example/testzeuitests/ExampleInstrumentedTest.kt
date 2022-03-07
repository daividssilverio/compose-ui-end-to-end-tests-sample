package com.example.testzeuitests

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@HiltAndroidTest
class ExampleInstrumentedTest {

    @get:Rule(order = 1)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var dao: DummyDao

    val isSyncing = mutableStateOf(false)

    @Before
    fun setup() {
        runBlocking {
            hiltRule.inject()
            dao.deleteAllData()
            dao.deleteUser()
        }

        composeTestRule.activity.isSyncingCallback = {
            synchronized(isSyncing) {
                isSyncing.value = it
            }
        }

        composeTestRule.registerIdlingResource(
            object : IdlingResource {
                override val isIdleNow: Boolean
                    get() {
                        synchronized(isSyncing) {
                            return !isSyncing.value
                        }
                    }
            }
        )
    }

    @Test
    fun runsTheStuffAndItWorks() {
        composeTestRule
            .onNodeWithText("login", ignoreCase = true, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule
            .onNodeWithTag("sync")
            .assertExists()

        composeTestRule.waitForIdle()

        assertFalse(isSyncing.value)

        composeTestRule.onRoot().printToLog("not in the list")

        composeTestRule
            .onNodeWithTag("the list", useUnmergedTree = true)
            .assertIsDisplayed()
    }
}