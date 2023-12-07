package com.example.dn

import android.location.Location
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.dn", appContext.packageName)
    }

    @Test
    fun checkFileSavedLocationNotExist() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // Check if file exist for save location exist
        assertEquals(null, MainActivity.readLocationFile(appContext, "loc.txt"))
    }

    @Test
    fun createFile() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val loc = Location("Test")
        loc.latitude = 4.0
        loc.longitude = 4.4

        MainActivity.writeLocationFile(appContext, "loc.txt", loc)

        assertNotEquals(null, MainActivity.readLocationFile(appContext, "loc.txt"))
    }

    @Test
    fun checkValuesSavedInFile() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val loc = Location("Test")
        loc.latitude = 4.0
        loc.longitude = 4.4

        MainActivity.writeLocationFile(appContext, "loc.txt", loc)

        val locSaved = MainActivity.readLocationFile(appContext, "loc.txt")

        if (locSaved != null) {
            assertEquals(4.0, locSaved.latitude, 0.0001)
            assertEquals(4.4, locSaved.longitude, 0.0001)
        }
    }
}