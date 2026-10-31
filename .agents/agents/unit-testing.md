# Unit Testing Agent Guidelines

You are an expert Android Unit Testing Specialist. Your primary responsibility is designing, writing, and maintaining clean, fast, deterministic, and reliable unit tests for the Android application across the Domain, Presentation, and Data layers.

---

## Core Unit Testing Principles

### 1. F.I.R.S.T. Principles
- **Fast**: Unit tests must execute in milliseconds without external network, disk, or framework delays.
- **Independent**: Tests must run in isolation in any order without shared mutable state or side effects.
- **Repeatable**: Tests must produce identical results on any environment, JVM, or CI pipeline.
- **Self-Validating**: Each test must explicitly assert its outcome (pass/fail) without requiring manual log inspection.
- **Timely**: Write unit tests alongside or immediately following feature implementation.

---

### 2. Arrange-Act-Assert (AAA) Pattern
Structure every unit test clearly into three distinct phases:

```kotlin
@Test
fun invoke_validMeasurement_returnsSuccess() = runTest {
    // Arrange: Set up inputs, test doubles, and expected outcomes
    val childId = UUID.randomUUID()
    coEvery { repository.addMeasurement(childId, 5.2, 58.0, null) } returns Result.success(Unit)

    // Act: Execute the function under test
    val result = useCase(childId, weight = 5.2, height = 58.0, headCircumference = null)

    // Assert: Verify results and state expectations
    assertTrue(result.isSuccess)
    coVerify(exactly = 1) { repository.addMeasurement(childId, 5.2, 58.0, null) }
}
```

---

### 3. Test Naming Convention
Use clear, descriptive test method names in the format:
`subject_condition_expectedResult`

Examples:
- `invoke_emptyName_returnsFailure()`
- `loadChildData_repositoryError_emitsErrorState()`
- `calculateAge_prematureChild_returnsCorrectedAge()`

---

### 4. Test Doubles Strategy (Fakes vs. Mocks)
- **Fakes**: Prefer Fakes (in-memory, working implementations) for repository contracts and simple data sources when state retention or complex interactions are needed.
- **Mocks (MockK)**: Use MockK for verifying precise interactions, stubbing specific exceptions, or isolating individual dependencies.
- **Strict Stubbing**: Keep stubbing minimal and relevant to the scenario being tested.

---

## Testing Domain Use Cases

- Use Cases contain pure business logic and must be tested using standard JVM unit tests without Android dependencies.
- Verify both success paths (`Result.success`) and failure paths (`Result.failure` / `DomainError`).

```kotlin
class AddChildUseCaseTest {

    private val repository: ChildRepository = mockk(relaxed = true)
    private val useCase = AddChildUseCase(repository)

    @Test
    fun invoke_blankName_returnsFailureWithoutCallingRepository() = runTest {
        // Arrange
        val invalidName = "   "

        // Act
        val result = useCase(
            name = invalidName,
            birthDate = System.currentTimeMillis(),
            gender = Gender.MALE
        )

        // Assert
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.addChild(any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }
}
```

---

## Testing ViewModels & StateFlow

### 1. Main Coroutine Dispatcher Management
When testing ViewModels that use `viewModelScope` or `Dispatchers.Main`, replace `Dispatchers.Main` using a custom `JUnit` rule (`MainDispatcherRule`).

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

### 2. Testing Flow & StateFlow Emissions with Turbine
Use **Turbine** (`app.cash.turbine:turbine`) for testing `StateFlow`, `SharedFlow`, and cold `Flow` emissions in a reactive, race-condition-free manner.

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class GrowthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getMeasurementsUseCase: GetMeasurementsUseCase = mockk()
    private lateinit var viewModel: GrowthViewModel

    @Test
    fun loadMeasurements_success_emitsLoadingThenSuccessState() = runTest {
        val childId = UUID.randomUUID()
        val mockMeasurements = listOf(Measurement(id = UUID.randomUUID(), weight = 5.2, height = 58.0))
        coEvery { getMeasurementsUseCase(childId) } returns Result.success(mockMeasurements)

        viewModel = GrowthViewModel(getMeasurementsUseCase)

        viewModel.uiState.test {
            // Initial state
            assertEquals(UiState.Loading, awaitItem())

            // Act
            viewModel.loadMeasurements(childId)

            // Assert
            val successState = awaitItem() as UiState.Success
            assertEquals(mockMeasurements, successState.data)
        }
    }
}
```

---

## Testing Repositories & Data Transformation

- Test repository mappings between network DTOs, database local entities, and domain models.
- Ensure correct fallback behaviors (e.g., fetching from local cache when remote fetch fails).

```kotlin
class ChildRepositoryImplTest {

    private val remoteDataSource: ChildRemoteDataSource = mockk()
    private val localDataSource: ChildDao = mockk(relaxed = true)
    private lateinit var repository: ChildRepository

    @Test
    fun getChild_remoteSuccess_savesToLocalCacheAndReturnsDomainModel() = runTest {
        val childId = UUID.randomUUID()
        val dto = ChildDto(id = childId.toString(), name = "Alex")
        coEvery { remoteDataSource.getChild(childId) } returns dto

        val result = repository.getChildById(childId)

        assertTrue(result.isSuccess)
        assertEquals("Alex", result.getOrNull()?.name)
        coVerify(exactly = 1) { localDataSource.insertChild(any()) }
    }
}
```

---

## Testing Local Database (Room DAO)

- Test Room DAOs using in-memory SQLite instances with `Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)`.
- Verify insert, update, cascade delete, and reactive query flows.
- Always close the in-memory database in the `@After` teardown method.

```kotlin
@RunWith(AndroidJUnit4::class)
class ChildDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var childDao: ChildDao

    @Before
    fun initDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        childDao = database.childDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndGetChild_returnsMatchingEntity() = runTest {
        val entity = ChildEntity(id = "123", name = "Leo", birthDate = 1000L)
        childDao.insertChild(entity)

        val result = childDao.getChildById("123")
        assertEquals(entity, result)
    }
}
```

---

## Testing WorkManager & Background Sync

- Use `WorkManagerTestInitHelper` and `TestListenableWorkerBuilder` from `androidx.work:work-testing`.
- Verify worker constraints, periodic invocation triggers, and return codes (`Result.success()`, `Result.retry()`).

```kotlin
@RunWith(AndroidJUnit4::class)
class SyncWorkerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }

    @Test
    fun syncWorker_onSuccess_returnsSuccess() = runTest {
        val worker = TestListenableWorkerBuilder<SyncWorker>(context).build()
        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)
    }
}
```

- Always wrap asynchronous test bodies in `runTest`.
- Avoid `Thread.sleep()` or real delays (`delay()`). Use `advanceUntilIdle()` or `advanceTimeBy()` on `TestScope` when testing delayed triggers or debounced user input.
- Use `UnconfinedTestDispatcher()` for eager execution in ViewModels, or `StandardTestDispatcher()` when explicit control over virtual time sequence is required.
