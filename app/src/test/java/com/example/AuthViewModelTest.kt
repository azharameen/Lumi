package com.example

import com.example.domain.account.UserProfileData
import com.example.domain.account.UserProfileRepository
import com.example.domain.account.UserFactItem
import com.example.domain.model.AuthUser
import com.example.domain.repository.AuthRepository
import com.example.domain.service.AnalyticsService
import com.example.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FakeAuthRepository : AuthRepository {
    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    override suspend fun signInWithGoogle(context: Any): Result<AuthUser> {
        val user = AuthUser(uid = "test-uid", email = "test@example.com", displayName = "Test User", photoUrl = null, isNewUser = false)
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun signOut(): Result<Unit> {
        _currentUser.value = null
        return Result.success(Unit)
    }

    override suspend fun getCurrentUser(): AuthUser? = _currentUser.value

    override suspend fun syncUserWithFirestore(user: AuthUser): Result<Boolean> = Result.success(false)

    override fun getSha1Fingerprint(): String = "SHA1-MOCK"
    override fun getSha256Fingerprint(): String = "SHA256-MOCK"
    override fun getWebClientId(): String = "WEBCLIENT-MOCK"
}

class FakeUserProfileRepository : UserProfileRepository {
    private val _profile = MutableStateFlow(UserProfileData())
    override val userProfile: StateFlow<UserProfileData> = _profile.asStateFlow()

    private val _facts = MutableStateFlow<List<UserFactItem>>(emptyList())
    override val userFacts: StateFlow<List<UserFactItem>> = _facts.asStateFlow()

    override fun updateProfile(profile: UserProfileData) { _profile.value = profile }
    override fun updateField(block: (UserProfileData) -> UserProfileData) { _profile.value = block(_profile.value) }
    override fun addUserFact(category: String, factText: String, isPinned: Boolean) {}
    override fun removeUserFact(id: String) {}
    override fun togglePinFact(id: String) {}
    override fun resetToDefaults() {}
}

class FakeAnalyticsService : AnalyticsService {
    val loggedAuthEvents = mutableListOf<Pair<String, Boolean>>()

    override fun logPetInteraction(action: String, petMood: String, happinessLevel: Int) {}
    override fun logPetLevelUp(oldLevel: Int, newLevel: Int, formName: String) {}
    override fun logSoundscapeSession(soundscapeTitle: String, isPlaying: Boolean) {}
    override fun logVaultAction(action: String, isSuccess: Boolean) {}
    override fun logRemoteConfigSync(status: String) {}
    override fun logGoalMilestone(goalTitle: String, category: String, isCompleted: Boolean) {}
    override fun logWellnessSession(exerciseType: String, durationSeconds: Int) {}
    override fun logAiChatMessage(mode: String, messageLength: Int, modelUsed: String) {}
    override fun logScreenView(screenName: String, screenClass: String) {}
    override fun logAuthEvent(method: String, isNewUser: Boolean) {
        loggedAuthEvents.add(method to isNewUser)
    }
    override fun setUserProperty(name: String, value: String) {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var userProfileRepository: FakeUserProfileRepository
    private lateinit var fakeAnalytics: FakeAnalyticsService
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        userProfileRepository = FakeUserProfileRepository()
        fakeAnalytics = FakeAnalyticsService()
        viewModel = AuthViewModel(
            authRepository = authRepository,
            userProfileManager = userProfileRepository,
            crashlytics = null,
            analytics = fakeAnalytics
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun guestLogin_updatesGuestStateAndLogsAnalytics() = runTest {
        viewModel.continueAsGuest()

        assertTrue(viewModel.uiState.value.isGuestMode)
        assertEquals(1, fakeAnalytics.loggedAuthEvents.size)
        assertEquals("guest", fakeAnalytics.loggedAuthEvents[0].first)
        assertFalse(fakeAnalytics.loggedAuthEvents[0].second)
    }

    @Test
    fun mockLogin_updatesUserStateAndLogsAnalytics() = runTest {
        viewModel.signInAsMockUser()

        assertNotNull(viewModel.uiState.value.user)
        assertEquals("mock-debug-uid-123", viewModel.uiState.value.user?.uid)
        assertEquals(1, fakeAnalytics.loggedAuthEvents.size)
        assertEquals("mock", fakeAnalytics.loggedAuthEvents[0].first)
    }

    @Test
    fun signOut_clearsUserStateAndLogsSignOutEvent() = runTest {
        viewModel.signInAsMockUser()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.signOut()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.user)
        assertTrue(fakeAnalytics.loggedAuthEvents.any { it.first == "sign_out" })
    }
}
