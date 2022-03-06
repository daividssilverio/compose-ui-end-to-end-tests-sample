package com.example.testzeuitests

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.IdlingRegistry
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import dagger.hilt.EntryPoint
import dagger.hilt.android.EarlyEntryPoints
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
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
    val currentRoute = mutableStateOf<String?>(null)

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

        composeTestRule.activity.currentRouteCallback = {
            synchronized(currentRoute) {
                currentRoute.value = it?.route
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

        composeTestRule.registerIdlingResource(
            object : IdlingResource {
                override val isIdleNow: Boolean
                    get() {
                        synchronized(currentRoute) {
                            return currentRoute.value != null
                        }
                    }
            }
        )
    }

    @Test
    fun runsTheStuffAndItWorks() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.mainClock.advanceTimeByFrame()

        composeTestRule
            .onNodeWithText("Login mate", ignoreCase = true, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.mainClock.advanceTimeByFrame()

        composeTestRule
            .onNodeWithTag("sync")
            .assertExists()

        composeTestRule.mainClock.advanceTimeByFrame()

        composeTestRule.waitForIdle()

        assertFalse(isSyncing.value)

        composeTestRule.onRoot().printToLog("xxaa")

        composeTestRule.mainClock.advanceTimeByFrame()

        composeTestRule
            .onNodeWithTag("the list", useUnmergedTree = true)
            .assertIsDisplayed()
    }
}