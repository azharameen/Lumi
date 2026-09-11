package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.navigation.NavDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Application>()
    }

    @Test
    fun `read string from context`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Lumi AI Friend", appName)
    }

    @Test
    fun `verify 5 primary navigation destinations`() {
        val allDestinations = NavDestination.allDestinations
        assertEquals(5, allDestinations.size)
        assertEquals("Companion", allDestinations[0].title)
        assertEquals("Assistant", allDestinations[1].title)
        assertEquals("Productivity", allDestinations[2].title)
        assertEquals("Wellness", allDestinations[3].title)
        assertEquals("Account", allDestinations[4].title)
    }
}
