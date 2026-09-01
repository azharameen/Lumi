package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.account.UserProfileData
import com.example.domain.account.UserProfileManager
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
    private lateinit var userProfileManager: UserProfileManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Application>()
        userProfileManager = UserProfileManager.getInstance(context)
        userProfileManager.resetToDefaults()
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

    @Test
    fun `test user profile default values and persistence`() {
        val initialProfile = userProfileManager.userProfile.value
        assertEquals("Azhar Ameen", initialProfile.userName)
        assertEquals("azharameen52@gmail.com", initialProfile.userEmail)
        assertEquals(4.0f, initialProfile.dailyFocusTargetHours)
        assertEquals(8000, initialProfile.targetDailySteps)

        val updated = initialProfile.copy(
            userName = "Azhar A.",
            personaTone = com.example.domain.account.LumiPersonaTone.ZEN_MINIMALIST,
            temperature = 0.5f,
            dailyFocusTargetHours = 6.0f
        )
        userProfileManager.updateProfile(updated)

        val savedProfile = userProfileManager.userProfile.value
        assertEquals("Azhar A.", savedProfile.userName)
        assertEquals(com.example.domain.account.LumiPersonaTone.ZEN_MINIMALIST, savedProfile.personaTone)
        assertEquals(0.5f, savedProfile.temperature)
        assertEquals(6.0f, savedProfile.dailyFocusTargetHours)
    }

    @Test
    fun `test user facts add, toggle pin, and remove`() {
        val initialCount = userProfileManager.userFacts.value.size
        assertTrue(initialCount >= 1)

        userProfileManager.addUserFact(
            category = "Coding",
            factText = "Prefers clean Jetpack Compose architecture and Kotlin Coroutines",
            isPinned = true
        )

        val afterAdd = userProfileManager.userFacts.value
        assertEquals(initialCount + 1, afterAdd.size)
        val addedItem = afterAdd.firstOrNull { it.category == "Coding" }
        assertNotNull(addedItem)
        assertTrue(addedItem!!.isPinned)

        // Toggle pin
        userProfileManager.togglePinFact(addedItem.id)
        val afterToggle = userProfileManager.userFacts.value.first { it.id == addedItem.id }
        assertFalse(afterToggle.isPinned)

        // Remove fact
        userProfileManager.removeUserFact(addedItem.id)
        val afterRemove = userProfileManager.userFacts.value
        assertEquals(initialCount, afterRemove.size)
    }
}
