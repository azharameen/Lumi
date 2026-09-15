# AI Agent System Directives: Android Kotlin Target Architecture

**CRITICAL DIRECTIVE:** This document represents the **North Star / Target Architecture** for this repository. 
While parts of the existing codebase may contain technical debt or anti-patterns, **AI Agents MUST write all NEW code according to these strict architectural standards.** 
When an Agent modifies existing code that violates these rules, the Agent MUST apply the "Boy Scout Rule" and refactor the modified component to comply with this standard.

## 1. Core Technical Stack
- **Build System:** Gradle with Kotlin DSL (`build.gradle.kts`) and Version Catalogs (`libs.versions.toml`).
- **Dependency Injection:** Koin (`core/di/AppModule.kt`).
- **UI Framework:** Jetpack Compose Material 3 with Navigation Compose.
- **Async & Reactive Data:** Kotlin Coroutines and Flows (`StateFlow`, `SharedFlow`). 
- **Local Storage:** Room Database with `Paging3` integration.
- **Network & Cloud:** Firebase, Retrofit, OkHttp, Moshi.
- **Testing Frameworks:** JUnit4, Robolectric, and Roborazzi.

## 2. Strict Clean Architecture Boundaries
Dependencies point INWARD toward the Domain layer.

- **Presentation Layer (UI & ViewModels):**
  - **Rule:** ViewModels MUST NOT inject Repositories directly.
  - **Rule:** ViewModels MUST ONLY interact with the Domain layer via **UseCases** (Interactors).
  
- **Domain Layer (UseCases & Models):**
  - **Rule:** The Domain layer MUST be pure Kotlin. It MUST NOT contain any Android framework imports (`android.*` or `androidx.*`).
  - **Rule:** UseCases must be single-purpose (e.g., `GetPetStatusUseCase`) and expose a single `operator fun invoke()`.
  - **Rule:** Define Repository Interfaces here using `Flow<T>` for continuous data streams and `suspend fun` for one-shot operations.

- **Data Layer (Repositories, DAOs, Networking):**
  - **Rule:** Repository implementations (`*Impl.kt`) belong here.
  - **Rule:** Data models (Room Entities, Network DTOs) MUST be mapped to Domain Models (e.g., `.toDomain()`) *before* crossing the boundary back to Domain. DAOs and Entities MUST NEVER leak to the UI.

## 3. State Management, UI Events & Error Handling

- **Error Propagation (Result Wrapper):**
  - **Rule:** Do NOT throw raw exceptions across architectural boundaries. 
  - **Rule:** One-shot UseCases and Repositories MUST wrap their return types in `Result<T>` or a custom sealed `Resource<T>` class (`Success`, `Error`, `Loading`).

- **Unidirectional Data Flow (UDF) & State Hoisting:**
  - UI State MUST be represented by a single Immutable Data Class (e.g., `data class LumiUiState(val isLoading: Boolean = false)`).
  - ViewModels back state with `private val _uiState = MutableStateFlow(LumiUiState())` and expose it via `asStateFlow()`.
  - **Rule:** UI Events (clicks, inputs) MUST flow UP as lambdas to the ViewModel. The UI must NEVER mutate state directly.

## 4. Jetpack Compose & Design System (STRICT)

The Lumi design system is a dark cyberpunk/ethereal aesthetic. ALL visual values
MUST come from the centralized theme in `core/theme/`. Hardcoded values are **PROHIBITED**.

### 4.1 The Modifier Rule (CRITICAL)
- **Rule:** EVERY public Composable function MUST accept a `modifier: Modifier = Modifier` as its first optional parameter (or right after required data parameters).
- **Rule:** This passed `modifier` MUST be applied to the root layout element of that Composable.

### 4.2 Typography (CRITICAL)
- **Rule:** ALL `Text()` composables MUST use `style = MaterialTheme.typography.XXX`. Raw `fontSize = X.sp` combined with `fontWeight = ...` is **PROHIBITED**.
- **Rule:** The minimum readable font size is `labelSmall` (10sp). Text below 10sp is **ILLEGAL**.
- **Rule:** Fractional font sizes (e.g., `11.5.sp`) are **PROHIBITED**. Use integer values only.
- **Rule:** When a `Text()` needs a non-default color, pass `color = ...` alongside `style = ...`. Never bypass the typography scale for color changes.
- **Available styles:** `displayLarge(32sp/Black)`, `displayMedium(28sp/ExtraBold)`, `displaySmall(24sp/Bold)`, `headlineLarge(22sp/Bold)`, `headlineMedium(19sp/Bold)`, `headlineSmall(17sp/SemiBold)`, `titleLarge(16sp/Bold)`, `titleMedium(14sp/SemiBold)`, `titleSmall(13sp/Medium)`, `bodyLarge(15sp/Normal)`, `bodyMedium(13sp/Normal)`, `bodySmall(12sp/Normal)`, `labelLarge(13sp/Bold)`, `labelMedium(11sp/SemiBold)`, `labelSmall(10sp/Medium)`.

### 4.3 Colors (CRITICAL)
- **Rule:** ALL colors MUST come from `core/theme/Color.kt` or `MaterialTheme.colorScheme`. Raw `Color(0xFF...)` literals in composables are **PROHIBITED**.
- **Rule:** `Color.White` and `Color.Black` are **PROHIBITED** in composables. Use `TextPrimary` / `ObsidianDark` / `SurfaceHighlight` etc. instead.
- **Rule:** NO duplicate color definitions. If a color exists in `Color.kt`, import it. Defining `private val SomeColor = Color(0xFF...)` in a composable file is **PROHIBITED**.
- **Rule:** All new brand colors MUST be added to `Color.kt` with a descriptive `Lumi*` prefix and a corresponding glow variant (`XxxGlow = Color(0x33...)`).
- **Available tokens:** See `Color.kt` for the full palette — brand accents (LumiCyan, LumiViolet, LumiPink, LumiGold, LumiMint, LumiCoral), surfaces (ObsidianDark, SpaceDark, SlateDark, SurfaceDark, SurfaceDarkVariant, SurfaceHighlight, SurfaceGlass), text (TextPrimary, TextSecondary, TextTertiary, TextMuted), and gradient brushes (LumiPrimaryGradient, LumiGlassCardBorder, etc.).

### 4.4 Corner Radius (STRICT)
- **Rule:** ALL `RoundedCornerShape` values MUST use the defined tiers from `MaterialTheme.spacing`: `cornerMicro(6dp)`, `cornerSmall(10dp)`, `cornerMedium(14dp)`, `cornerLarge(20dp)`, `cornerExtraLarge(24dp)`. Arbitrary values (e.g., `RoundedCornerShape(13.dp)`) are **PROHIBITED**.
- **Tier usage guide:**
  - `cornerMicro` — tiny badges, pills, tags
  - `cornerSmall` — chips, small surfaces, input fields
  - `cornerMedium` — standard cards, list items
  - `cornerLarge` — message bubbles, large cards, dialogs
  - `cornerExtraLarge` — bottom sheets, hero cards, full-screen overlays

### 4.5 Spacing (STRICT)
- **Rule:** ALL padding, margin, and spacer values MUST come from the spacing scale in `MaterialTheme.spacing`: `extraSmall(4dp)`, `small(8dp)`, `medium(16dp)`, `large(24dp)`, `extraLarge(32dp)`. Off-scale values (e.g., `6.dp`, `10.dp`, `12.dp`, `14.dp`, `22.dp`) are **PROHIBITED**.
- **Rule:** Border widths MUST be `1.dp` or `2.dp`. Fractional border widths (e.g., `1.2.dp`, `1.5.dp`) are **PROHIBITED**.

### 4.6 Icon Sizes (STRICT)
- **Rule:** ALL icon sizes MUST use the defined scale from `MaterialTheme.spacing`: `iconXS(12dp)`, `iconSM(16dp)`, `iconMD(20dp)`, `iconLG(24dp)`, `iconXL(32dp)`, `iconHero(48dp)`. Arbitrary icon sizes (e.g., `15.dp`, `22.dp`, `36.dp`) are **PROHIBITED**.

### 4.7 Card & Surface Standardization (CRITICAL)
- **Rule:** ALL card-like surfaces MUST use `LumiCard` (the glassmorphic card in `LumiPrimitives.kt`). Raw `Box` + `Modifier.background(...)` + `Modifier.clip(...)` patterns for cards are **PROHIBITED**.
- **Rule:** `LumiCard` MUST have `shadowElevation >= 1.dp` to differentiate from the background.
- **Rule:** Interactive cards MUST use `Surface(onClick = ...)` or `LumiCard` with `onClick` to ensure proper ripple indication. Bare `Modifier.clickable` on card-like elements is **PROHIBITED**.

### 4.8 Compose Stability
- Keep Composables stateless where possible (pass data in, pass events out).
- Use `remember` for local UI state or expensive derived computations.
- ALWAYS use the `key` parameter in `LazyColumn`/`LazyRow` items to prevent unnecessary recompositions.

### 4.9 Hardcoded Strings (CRITICAL)
- **Rule:** AI Agents MUST NOT hardcode strings in Composables. ALL user-facing text MUST be extracted to `res/values/strings.xml` and referenced via `stringResource(id = R.string.xxx)`.
- **Rule:** This includes: button labels, error messages, status text, content descriptions, accessibility labels, notification text, and empty-state copy.
- **Rule:** `contentDescription` on icon-only clickable elements MUST use `stringResource(R.string.xxx)`. `contentDescription = null` on actionable icons is **PROHIBITED**.

### 4.10 Accessibility (STRICT)
- **Rule:** Every icon-only clickable element MUST have a non-null `contentDescription`.
- **Rule:** `contentDescription` values MUST use `stringResource(R.string.xxx)`, never hardcoded English strings.
- **Rule:** Touch targets MUST be at least `48dp x 48dp` (Material Design minimum).

## 5. Navigation

- **Type-Safe Navigation:**
  - **Rule:** Navigation Compose routes MUST use `kotlinx.serialization` (e.g., `@Serializable data class ProfileScreen(val userId: String)`).
  - Do not use string-based route matching (`"profile/{userId}"`) for new navigation graphs.

## 6. Concurrency & Main-Safety

- **Rule:** The Data and Domain layers MUST be **Main-Safe**.
- **Rule:** ViewModels should not specify `Dispatchers.IO`. The Repository/UseCase is responsible for wrapping heavy disk/network operations in `withContext(Dispatchers.IO) { ... }`.
- **Rule:** Use structured concurrency (`viewModelScope.launch { }`). Never use `GlobalScope`.

## 7. Dependency Injection (Koin)

- **Rule:** NO manual instantiation of dependencies (e.g., `val repo = PetRepositoryImpl()`).
- **Rule:** Provide implementations as their interface types in Koin modules: `single<PetRepository> { PetRepositoryImpl(get()) }`.
- **Rule:** Use constructor injection in ViewModels and UseCases. Do not use field injection (`by inject()`) inside classes unless absolutely necessary for Android components (like Services).

## 8. Testing Standards

- **Fakes Over Mocks:**
  - **Rule:** When writing tests for ViewModels or UseCases, prefer creating **Fake** implementations of Repository interfaces (e.g., `class FakePetRepository : PetRepository`) over using Mocking libraries (MockK/Mockito). Fakes are faster and more reliable.
- **UI Testing:**
  - **Rule:** Apply `testTag` or `semantics` to key UI components so they can be easily targeted in Compose UI tests.
  - **Rule:** Use Roborazzi snapshot tests (`*ScreenshotTest.kt`) using `RobolectricDeviceQualifiers` for visual components.

## 9. Workspace & Tooling Constraints (CRITICAL)

- **STRICT PROHIBITION ON SCRIPT-BASED EDITS:**
  - **Rule:** NEVER write `.sh`, `.py`, `.pl`, or any shell scripts to manipulate or patch codebase files.
  - **Rule:** NEVER use shell commands like `sed`, `awk`, `echo`, or shell redirection (`>`) to modify code.
  - **Reason:** Shell scripts bypass IDE memory buffers, cause sync issues, and risk destroying unsaved work. Agents MUST exclusively use native IDE tool calls (e.g., `replace_file_content`, `multi_replace_file_content`, `write_file`) for file manipulation.

## 10. Strict Definition of Done (DoD)
Before exiting ANY task, an agent MUST self-verify:
- [ ] No direct Repository injection in ViewModels (UseCases are used instead).
- [ ] Return types cross boundaries via `Result<T>` or sealed state classes for error handling.
- [ ] EVERY new Composable accepts and applies a `modifier: Modifier = Modifier`.
- [ ] No hardcoded strings; everything is extracted to `strings.xml`.
- [ ] Data layer implementations are Main-Safe (`withContext(Dispatchers.IO)`).
- [ ] Entities/DTOs are mapped to Domain models before leaving the Data layer.
- [ ] Refactoring (Boy Scout Rule) was applied to the immediate context of the modified code.
- [ ] NO scratch scripts (`.sh`, `.py`) were left behind in the workspace.
- [ ] **No raw `fontSize = X.sp` in any `Text()` — all use `MaterialTheme.typography.XXX`.**
- [ ] **No `Color(0xFF...)`, `Color.White`, or `Color.Black` in composables — all from `Color.kt` or `MaterialTheme.colorScheme`.**
- [ ] **No `RoundedCornerShape(X.dp)` with off-tier values — all use spacing corner tiers (6/10/14/20/24dp).**
- [ ] **No off-scale spacing (6dp, 10dp, 12dp, 14dp, 22dp, etc.) — all use 4/8/16/24/32dp scale.**
- [ ] **No off-scale icon sizes — all use 12/16/20/24/32/48dp scale.**
- [ ] **All card-like surfaces use `LumiCard` — no raw Box+background+clip patterns.**
- [ ] **All icon-only clickables have non-null `contentDescription` via `stringResource`.**
- [ ] **No text below 10sp. No fractional font sizes.**