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

## 4. Jetpack Compose Best Practices

- **The Modifier Rule (CRITICAL):**
  - **Rule:** EVERY public Composable function MUST accept a `modifier: Modifier = Modifier` as its first optional parameter (or right after required data parameters).
  - **Rule:** This passed `modifier` MUST be applied to the root layout element of that Composable.
- **Compose Stability:** 
  - Keep Composables stateless where possible (pass data in, pass events out).
  - Use `remember` for local UI state or expensive derived computations.
  - ALWAYS use the `key` parameter in `LazyColumn`/`LazyRow` items to prevent unnecessary recompositions.
- **Hardcoded Strings:**
  - **Rule:** AI Agents MUST NOT hardcode strings in Composables. All user-facing text MUST be extracted to `res/values/strings.xml` and referenced via `stringResource(id = R.string.xxx)`.

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

## 9. Strict Definition of Done (DoD)
Before exiting ANY task, an agent MUST self-verify:
- [ ] No direct Repository injection in ViewModels (UseCases are used instead).
- [ ] Return types cross boundaries via `Result<T>` or sealed state classes for error handling.
- [ ] EVERY new Composable accepts and applies a `modifier: Modifier = Modifier`.
- [ ] No hardcoded strings; everything is extracted to `strings.xml`.
- [ ] Data layer implementations are Main-Safe (`withContext(Dispatchers.IO)`).
- [ ] Entities/DTOs are mapped to Domain models before leaving the Data layer.
- [ ] Refactoring (Boy Scout Rule) was applied to the immediate context of the modified code.