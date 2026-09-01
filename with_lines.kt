1 package com.example
2 
3 import android.Manifest
4 import android.content.Intent
5 import android.content.pm.PackageManager
6 import com.google.accompanist.permissions.ExperimentalPermissionsApi
7 import com.google.accompanist.permissions.isGranted
8 import com.google.accompanist.permissions.rememberPermissionState
9 import android.graphics.Bitmap
10 import android.graphics.ImageDecoder
11 import android.net.Uri
12 import android.os.Build
13 import android.os.Bundle
14 import android.provider.MediaStore
15 import androidx.activity.ComponentActivity
16 import androidx.activity.compose.BackHandler
17 import androidx.activity.compose.setContent
18 import androidx.activity.enableEdgeToEdge
19 import org.koin.androidx.viewmodel.ext.android.viewModel
20 import androidx.compose.animation.Crossfade
21 import androidx.compose.foundation.background
22 import androidx.compose.foundation.layout.Box
23 import androidx.compose.foundation.layout.fillMaxSize
24 import androidx.compose.foundation.layout.padding
25 import androidx.compose.material3.Scaffold
26 import androidx.compose.runtime.Composable
27 import androidx.lifecycle.compose.collectAsStateWithLifecycle
28 import androidx.paging.compose.collectAsLazyPagingItems
29 import androidx.compose.runtime.getValue
30 import androidx.compose.ui.Modifier
31 import androidx.compose.ui.platform.LocalContext
32 import androidx.compose.ui.Alignment
33 import androidx.compose.ui.unit.dp
34 import androidx.compose.material.icons.Icons
35 import androidx.compose.material.icons.filled.Home
36 import androidx.core.app.ActivityCompat
37 import androidx.core.content.ContextCompat
38 import com.example.framework.AppShortcutsManager
39 import com.example.framework.PetOverlayService
40 import com.example.presentation.components.BreathingExerciseModal
41 import com.example.presentation.components.CameraVisionDialog
42 import com.example.presentation.components.OverlayPermissionDialog
43 import com.example.core.navigation.NavDestination
44 import com.example.presentation.screens.ChatScreen
45 import com.example.presentation.home.HomeScreen
46 import com.example.presentation.screens.LifeHubScreen
47 import com.example.presentation.screens.UserAccountScreen
48 import com.example.presentation.screens.WellnessScreen
49 import com.example.core.theme.MyApplicationTheme
50 import com.example.core.theme.ObsidianDark
51 import com.example.presentation.viewmodel.*
52 
53 class MainActivity : ComponentActivity() {
54 
55     private val viewModel: LumiViewModel by viewModel()
56     private val aiSettingsViewModel: AiSettingsViewModel by viewModel()
57     private val chatViewModel: ChatViewModel by viewModel()
58     private val wellnessViewModel: WellnessViewModel by viewModel()
59     private val lifeHubViewModel: LifeHubViewModel by viewModel()
60     private val petViewModel: PetViewModel by viewModel()
61     private val authViewModel: AuthViewModel by viewModel()
62 
63     override fun onCreate(savedInstanceState: Bundle?) {
64         super.onCreate(savedInstanceState)
65         enableEdgeToEdge()
66 
67         // Initialize Dynamic App Shortcuts
68         AppShortcutsManager.initDynamicShortcuts(this)
69 
70         // Handle incoming intent (Shares, Shortcuts, Alarms, Widgets)
71         handleIntent(intent)
72 
73         setContent {
74             val petStatus by petViewModel.petStatus.collectAsStateWithLifecycle()
75             val petPrimary = androidx.compose.ui.graphics.Color(petStatus.bloubSkinColor.primaryHex)
76             val petSecondary = androidx.compose.ui.graphics.Color(petStatus.bloubSkinColor.endHex)
77             MyApplicationTheme(petColorPrimary = petPrimary, petColorSecondary = petSecondary) {
78                 LumiApp(
79                     viewModel = viewModel,
80                     aiSettingsViewModel = aiSettingsViewModel,
81                     chatViewModel = chatViewModel,
82                     wellnessViewModel = wellnessViewModel,
83                     lifeHubViewModel = lifeHubViewModel,
84                     petViewModel = petViewModel,
85                     authViewModel = authViewModel
86                 )
87             }
88         }
89     }
90 
91     override fun onNewIntent(intent: Intent) {
92         super.onNewIntent(intent)
93         setIntent(intent)
94         handleIntent(intent)
95     }
96 
97     private fun handleIntent(intent: Intent?) {
98         if (intent == null) return
99 
100         val action = intent.action
101         val type = intent.type
102 
103         // 1. Handle Android System Share Sheet (ACTION_SEND)
104         if (Intent.ACTION_SEND == action && type != null) {
105             if ("text/plain" == type) {
106                 intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText ->
107                     viewModel.handleIncomingSharedText(sharedText)
108                 }
109             } else if (type.startsWith("image/")) {
110                 val imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
111                     intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
112                 } else {
113                     @Suppress("DEPRECATION")
114                     intent.getParcelableExtra(Intent.EXTRA_STREAM)
115                 }
116                 imageUri?.let { uri ->
117                     val bitmap = loadBitmapFromUri(uri)
118                     if (bitmap != null) {
119                         viewModel.handleIncomingSharedImage(bitmap)
120                     }
121                 }
122             }
123         }
124 
125         // 2. Handle Launcher App Shortcut Actions
126         intent.getStringExtra("SHORTCUT_ACTION")?.let { shortcutAction ->
127             viewModel.handleShortcutAction(shortcutAction)
128         }
129 
130         // 3. Handle explicit tab navigation
131         if (intent.hasExtra("NAVIGATE_TAB")) {
132             val tab = intent.getIntExtra("NAVIGATE_TAB", 0)
133             viewModel.setSelectedTab(tab)
134         }
135 
136         // 4. Handle Daily Briefing Notification Deep Link
137         intent.getStringExtra("OPEN_BRIEFING")?.let { briefingStr ->
138             viewModel.setSelectedTab(0)
139             val type: com.example.domain.briefing.BriefingType? = when (briefingStr.uppercase()) {
140                 "MORNING" -> com.example.domain.briefing.BriefingType.MORNING
141                 "EVENING" -> com.example.domain.briefing.BriefingType.EVENING
142                 else -> null
143             }
144             lifeHubViewModel.refreshDailyBriefing(type, petViewModel.petStatus.value, petViewModel.petEvolution.value, wellnessViewModel.allWellnessLogs.value)
145         }
146     }
147 
148     private fun loadBitmapFromUri(uri: Uri): Bitmap? {
149         return try {
150             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
151                 val source = ImageDecoder.createSource(contentResolver, uri)
152                 ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
153                     decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
154                     decoder.isMutableRequired = true
155                 }
156             } else {
157                 @Suppress("DEPRECATION")
158                 MediaStore.Images.Media.getBitmap(contentResolver, uri)
159             }
160         } catch (e: Exception) {
161             null
162         }
163     }
164 
165 }
166 
167 @Composable
168 fun LumiApp(
169     viewModel: LumiViewModel,
170     aiSettingsViewModel: AiSettingsViewModel,
171     chatViewModel: ChatViewModel,
172     wellnessViewModel: WellnessViewModel,
173     lifeHubViewModel: LifeHubViewModel,
174     petViewModel: PetViewModel,
175     authViewModel: AuthViewModel
176 ) {
177     val uiState by viewModel.uiState.collectAsStateWithLifecycle()
178     val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
179     val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
180     val petStatus by petViewModel.petStatus.collectAsStateWithLifecycle()
181     val batteryStatus by viewModel.batteryStatus.collectAsStateWithLifecycle()
182     val networkStatus by viewModel.networkStatus.collectAsStateWithLifecycle()
183     val locationContext by viewModel.locationState.collectAsStateWithLifecycle()
184     val userFacts by viewModel.userFacts.collectAsStateWithLifecycle()
185     val benchmarkStatus by viewModel.benchmarkStatus.collectAsStateWithLifecycle()
186     val chatMessagesList by viewModel.chatMessages.collectAsStateWithLifecycle()
187     val aiRoutingMode by viewModel.aiRoutingMode.collectAsStateWithLifecycle()
188 
189     val calendarEvents by lifeHubViewModel.allCalendarEvents.collectAsStateWithLifecycle()
190     val tasks by lifeHubViewModel.allTasks.collectAsStateWithLifecycle()
191     val dailyBriefing by lifeHubViewModel.dailyBriefing.collectAsStateWithLifecycle()
192     val goalPlans by lifeHubViewModel.allGoalPlans.collectAsStateWithLifecycle()
193     val soundState by lifeHubViewModel.soundscapeState.collectAsStateWithLifecycle()
194 
195     val wellnessLogs by wellnessViewModel.allWellnessLogs.collectAsStateWithLifecycle()
196     val memories by wellnessViewModel.allMemories.collectAsStateWithLifecycle()
197 
198     val isListening by chatViewModel.voiceEngine.isListening.collectAsStateWithLifecycle()
199     val isSpeaking by chatViewModel.voiceEngine.isSpeaking.collectAsStateWithLifecycle()
200 
201     val modelDownloadStates by aiSettingsViewModel.modelDownloadStates.collectAsStateWithLifecycle()
202     val activeLocalModelId by aiSettingsViewModel.activeLocalModelId.collectAsStateWithLifecycle()
203     val selectedAccelerator by aiSettingsViewModel.selectedAccelerator.collectAsStateWithLifecycle()
204 
205     val context = LocalContext.current
206 
207     val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
208     val handleStartVoiceListening = {
209         if (audioPermissionState.status.isGranted) {
210             chatViewModel.startVoiceListening()
211         } else {
212             audioPermissionState.launchPermissionRequest()
213         }
214     }
215 
216     if (authUiState.user == null && !authUiState.isGuestMode) {
217         com.example.presentation.screens.auth.LoginScreen(
218             authViewModel = authViewModel,
219             petStatus = petStatus,
220             onLoginSuccess = {
221                 // User signed in successfully
222             },
223             onContinueAsGuest = {
224                 authViewModel.continueAsGuest()
225             }
226         )
227     } else if (!userProfile.hasCompletedOnboarding) {
228         com.example.presentation.screens.OnboardingScreen(
229             viewModel = viewModel,
230             onComplete = { /* State handles recomposition automatically */ }
231         )
232     } else {
233         BackHandler(
234             enabled = uiState.selectedTab != com.example.core.navigation.NavDestination.PetCompanion.tabIndex || 
235                       uiState.showWardrobeScreen || 
236                       uiState.showCameraDialog || 
237                       uiState.showBreathingDialog ||
238                       uiState.showOverlayPermissionDialog ||
239                       uiState.lifeHubSubTab != 0
240         ) {
241             when {
242                 uiState.showWardrobeScreen -> viewModel.setShowWardrobeScreen(false)
243                 uiState.showCameraDialog -> viewModel.setShowCamera(false)
244                 uiState.showBreathingDialog -> viewModel.setShowBreathing(false)
245                 uiState.showOverlayPermissionDialog -> viewModel.setShowOverlayPermission(false)
246                 uiState.selectedTab == com.example.core.navigation.NavDestination.LifeHub.tabIndex && uiState.lifeHubSubTab != 0 -> {
247                     viewModel.setLifeHubSubTab(0)
248                 }
249                 uiState.selectedTab != com.example.core.navigation.NavDestination.PetCompanion.tabIndex -> {
250                     viewModel.setSelectedTab(com.example.core.navigation.NavDestination.PetCompanion.tabIndex)
251                 }
252             }
253         }
254         val handleLifeHubAction: (com.example.presentation.viewmodel.LumiUiAction) -> Unit = { action ->
255     val handleLifeHubAction: (com.example.presentation.viewmodel.LumiUiAction) -> Unit = { action ->
256  
257         when (action) {
258             is com.example.presentation.viewmodel.LumiUiAction.NavigateToChat -> {
259                 viewModel.setSelectedTab(com.example.core.navigation.NavDestination.Assistant.tabIndex)
260                 action.prompt?.let { chatViewModel.sendMessage(it) }
261             }
262             is com.example.presentation.viewmodel.LumiUiAction.SetLifeHubSubTab -> viewModel.setLifeHubSubTab(action.tabIndex)
263             is com.example.presentation.viewmodel.LumiUiAction.AddCalendarEvent -> lifeHubViewModel.addCalendarEvent(action.event)
264             is com.example.presentation.viewmodel.LumiUiAction.DeleteCalendarEvent -> lifeHubViewModel.deleteCalendarEvent(action.id)
265             is com.example.presentation.viewmodel.LumiUiAction.SpeakBriefing -> {} // Handled via voice engine
266             is com.example.presentation.viewmodel.LumiUiAction.AddTask -> lifeHubViewModel.addTask(action.title, action.priority, action.category, action.estimatedMinutes, action.notes)
267             is com.example.presentation.viewmodel.LumiUiAction.ToggleTask -> lifeHubViewModel.toggleTask(action.id, action.isCompleted)
268             is com.example.presentation.viewmodel.LumiUiAction.DeleteTask -> lifeHubViewModel.deleteTask(action.task)
269             is com.example.presentation.viewmodel.LumiUiAction.DecomposeGoal -> lifeHubViewModel.decomposeGoal(action.title, action.description, action.category, action.deadline)
270             is com.example.presentation.viewmodel.LumiUiAction.DeleteGoal -> lifeHubViewModel.deleteGoal(action.id)
271             is com.example.presentation.viewmodel.LumiUiAction.ToggleMilestone -> lifeHubViewModel.toggleMilestone(action.milestoneId, action.goalId, action.isCompleted)
272             is com.example.presentation.viewmodel.LumiUiAction.ExecuteMilestone -> lifeHubViewModel.executeMilestone(action.milestoneId, action.goalId)
273             is com.example.presentation.viewmodel.LumiUiAction.StartSoundscape -> lifeHubViewModel.startSoundscape(action.type)
274             is com.example.presentation.viewmodel.LumiUiAction.StopSoundscape -> lifeHubViewModel.stopSoundscape()
275             is com.example.presentation.viewmodel.LumiUiAction.SetSoundscapeVolume -> lifeHubViewModel.setSoundscapeVolume(action.volume)
276             is com.example.presentation.viewmodel.LumiUiAction.StartFocusTimer -> lifeHubViewModel.startFocusTimerWithSoundscape(action.minutes)
277             is com.example.presentation.viewmodel.LumiUiAction.StopFocusTimer -> lifeHubViewModel.stopFocusTimerWithSoundscape()
278         }
279     }
280 
281         val petPrimary = androidx.compose.ui.graphics.Color(petStatus.bloubSkinColor.primaryHex)
282 
283         Scaffold(
284             modifier = Modifier.fillMaxSize()
285         ) { innerPadding ->
286             Box(
287                 modifier = Modifier
288                     .fillMaxSize()
289                     .padding(innerPadding)
290                     .background(ObsidianDark)
291             ) {
292                 Crossfade(
293                 targetState = uiState.selectedTab,
294                 label = "ScreenTransition"
295             ) { tab ->
296                 when (tab) {
297                     NavDestination.Assistant.tabIndex -> ChatScreen(
298                         uiState = uiState,
299                         petStatus = petStatus,
300                         chatMessages = chatViewModel.pagedChatMessages.collectAsLazyPagingItems(),
301                         isListening = isListening,
302                         isSpeaking = isSpeaking,
303                         onSendMessage = { text -> chatViewModel.sendMessage(text) },
304                         onSetInputText = { text -> viewModel.setInputText(text) },
305                         onShowCamera = { viewModel.setShowCamera(true) },
306                         onStartVoiceListening = { handleStartVoiceListening() },
307                         onStopVoiceListening = { chatViewModel.stopVoiceListening() },
308                         onToggleVoiceOutput = { viewModel.toggleVoiceOutput() },
309                         onNavigateBack = { viewModel.setSelectedTab(NavDestination.PetCompanion.tabIndex) }
310                     )
311                     NavDestination.LifeHub.tabIndex -> LifeHubScreen(
312                         uiState = uiState,
313                         tasks = tasks,
314                         events = calendarEvents,
315                         wellnessLogs = wellnessLogs,
316                         memories = memories,
317                         dailyBriefing = dailyBriefing,
318                         goalPlans = goalPlans,
319                         getMilestonesForGoal = { id -> lifeHubViewModel.getMilestonesForGoal(id) },
320                         soundState = soundState,
321                         onAction = handleLifeHubAction,
322                         onNavigateBack = { viewModel.setSelectedTab(NavDestination.PetCompanion.tabIndex) }
323                     )
324                     NavDestination.Wellness.tabIndex -> WellnessScreen(
325                         viewModel = wellnessViewModel,
326                         appViewModel = viewModel,
327                         onNavigateToChat = { viewModel.setSelectedTab(NavDestination.Assistant.tabIndex) },
328                         onNavigateBack = { viewModel.setSelectedTab(NavDestination.PetCompanion.tabIndex) }
329                     )
330                     NavDestination.Account.tabIndex -> UserAccountScreen(
331                         userProfile = userProfile,
332                         authUser = authUiState.user,
333                         onSignInWithGoogle = { authViewModel.signInWithGoogle(context) },
334                         onSignOut = { authViewModel.signOut() },
335                         userFacts = userFacts,
336                         petStatus = petStatus,
337                         benchmarkStatus = benchmarkStatus ?: "",
338                         tasks = tasks.map { it.toDomain() },
339                         events = calendarEvents.map { it.toDomain() },
340                         messages = chatMessagesList.map { it.toDomain() },
341                         aiRoutingMode = aiRoutingMode,
342                         onSetAiRoutingMode = { mode -> viewModel.setAiRoutingMode(mode) },
343                         localModelCatalog = aiSettingsViewModel.localModelCatalog,
344                         modelDownloadStates = modelDownloadStates,
345                         activeLocalModelId = activeLocalModelId,
346                         selectedAccelerator = selectedAccelerator,
347                         onUpdateProfile = { updated -> aiSettingsViewModel.updateUserProfile(updated) },
348                         onAddUserFact = { cat, txt, isPinned -> viewModel.addUserFact(cat, txt, isPinned) },
349                         onRemoveUserFact = { id -> viewModel.removeUserFact(id) },
350                         onTogglePinFact = { id -> viewModel.togglePinFact(id) },
351                         onClearAiAnalytics = { viewModel.clearAiAnalytics() },
352                         onDownloadLocalModel = { id -> aiSettingsViewModel.downloadLocalModel(id) },
353                         onCancelModelDownload = { id -> aiSettingsViewModel.cancelModelDownload(id) },
354                         onPauseModelDownload = { id -> aiSettingsViewModel.pauseModelDownload(id) },
355                         onDeleteLocalModel = { id -> aiSettingsViewModel.deleteLocalModel(id) },
356                         onSetActiveLocalModel = { id -> aiSettingsViewModel.setActiveLocalModel(id) },
357                         onSetHardwareAccelerator = { acc -> aiSettingsViewModel.setHardwareAccelerator(acc) },
358                         onRunGemmaBenchmark = { viewModel.runGemmaBenchmark() },
359                         isOverlayEnabled = uiState.isOverlayEnabled,
360                         onToggleOverlay = { if (it) viewModel.setShowOverlayPermission(true) else viewModel.setOverlayEnabled(false) },
361                         onNavigateToChat = { prompt ->
362                             viewModel.setSelectedTab(NavDestination.Assistant.tabIndex)
363                             prompt?.let { viewModel.sendMessage(it) }
364                         },
365                         onNavigateBack = { viewModel.setSelectedTab(NavDestination.PetCompanion.tabIndex) }
366                     )
367                     else -> HomeScreen(
368                         petStatus = petStatus,
369                         uiState = uiState,
370                         batteryStatus = batteryStatus,
371                         networkStatus = networkStatus,
372                         events = calendarEvents,
373                         tasks = tasks,
374                         isListening = isListening,
375                         isSpeaking = isSpeaking,
376                         authUser = authUiState.user,
377                         onPetPetted = { petViewModel.onPetPetted() },
378                         onPetTouched = { petViewModel.onPetTouched() },
379                         onTogglePetSleep = { petViewModel.togglePetSleep() },
380                         onStartVoiceListening = { handleStartVoiceListening() },
381                         onStopVoiceListening = { chatViewModel.stopVoiceListening() },
382                         onShowCamera = { viewModel.setShowCamera(true) },
383                         onShowWardrobe = { viewModel.setShowWardrobeScreen(true) },
384                         onNavigateToChat = { viewModel.setSelectedTab(NavDestination.Assistant.tabIndex) },
385                         onNavigateToLifeHub = { subTab -> viewModel.navigateToLifeHub(subTab) },
386                         onNavigateToAccount = { viewModel.setSelectedTab(NavDestination.Account.tabIndex) },
387                         onNavigateToWellness = { viewModel.setSelectedTab(NavDestination.Wellness.tabIndex) },
388                         locationContext = locationContext,
389                         userProfile = userProfile,
390                         onFeedPet = { petViewModel.feedPet() },
391                         onDancePet = { petViewModel.dancePet() },
392                         onPokePet = { petViewModel.pokePet() },
393                         onToggleTask = { id, isCompleted -> lifeHubViewModel.toggleTask(id, isCompleted) },
394                         onQuickAgentPrompt = { prompt ->
395                             viewModel.setSelectedTab(NavDestination.Assistant.tabIndex)
396                             chatViewModel.sendMessage(prompt)
397                         }
398                     )
399                 }
400             }
401             if (uiState.showWardrobeScreen) {
402                 com.example.presentation.screens.WardrobeScreen(petViewModel = petViewModel, wellnessViewModel = wellnessViewModel, onClose = { viewModel.setShowWardrobeScreen(false) })
403             }
404             // Camera / Vision Dialog Modal
405             if (uiState.showCameraDialog) {
406                 CameraVisionDialog(
407                     onDismiss = { viewModel.setShowCamera(false) },
408                     onImageCaptured = { bitmap, prompt ->
409                         viewModel.setShowCamera(false)
410                         viewModel.sendMessage(prompt, bitmap)
411                         viewModel.setSelectedTab(NavDestination.Assistant.tabIndex)
412                     }
413                 )
414             }
415 
416             // Breathing Exercise Dialog Modal
417             if (uiState.showBreathingDialog) {
418                 BreathingExerciseModal(
419                     onDismiss = { viewModel.setShowBreathing(false) },
420                     onComplete = {
421                         viewModel.setShowBreathing(false)
422                         viewModel.logWellness(
423                             moodScore = 9,
424                             moodLabel = "Centered & Relaxed",
425                             energyLevel = 8,
426                             hydrationCups = 0,
427                             gratitude = "Completed 4-7-8 Breathing Coherence with Lumi"
428                         )
429                     }
430                 )
431             }
432 
433             // Overlay Permission Dialog Modal
434             if (uiState.showOverlayPermissionDialog) {
435                 OverlayPermissionDialog(
436                     onDismiss = { viewModel.setShowOverlayPermission(false) },
437                     onGranted = {
438                         viewModel.setShowOverlayPermission(false)
439                         viewModel.setOverlayEnabled(true)
440                         val serviceIntent = Intent(context, PetOverlayService::class.java)
441                         ContextCompat.startForegroundService(context, serviceIntent)
442                     }
443                 )
444             }
445         }
446     }
447 }
448 }
449 
450 
