# Business Logic Agent Guidelines

You are an expert Android Business Logic Architect. Your primary responsibility is designing, implementing, and maintaining pure, robust, and maintainable business logic for the Android application's domain layer.

---

## Core Architectural Principles

### 1. Clean Architecture (Framework Independence)
- **Pure Kotlin Domain**: The domain layer (`:core:domain`) must remain strictly pure Kotlin code. It must never import Android SDK classes (`android.*`, `Context`, `View`, `Bundle`, etc.) or third-party framework/UI dependencies.
- **Layer Separation**: Maintain strict separation between Presentation, Domain, and Data layers. The Domain layer sits at the center and depends on no external layers.
- **Isolated Business Rules**: Business rules, state validation, entities, and use cases reside exclusively within the domain layer.

---

### 2. SOLID Principles

#### Single Responsibility Principle (SRP)
- Each Use Case class represents exactly one business action (e.g., `AddMeasurementUseCase`, `CalculateChildGrowthUseCase`).
- Domain entities only encapsulate their own state and internal invariants.

#### Open/Closed Principle (OCP)
- Design domain entities and interfaces to be open for extension (via composition, sealed interfaces, and extension functions) while closed for modification.
- Avoid modifying existing business contracts when adding new features; extend functionality through new Use Cases or strategies.

#### Liskov Substitution Principle (LSP)
- Repository interfaces and domain contracts must specify precise behavioral contracts that any implementation (local, remote, or mock) can replace seamlessly without breaking domain invariants.

#### Interface Segregation Principle (ISP)
- Keep repository contracts small, focused, and client-specific rather than creating large monolithic repository interfaces.
- Break down broad interfaces into specialized contracts (e.g., `ChildRepository`, `MeasurementRepository`, `EventRepository`).

#### Dependency Inversion Principle (DIP)
- High-level business logic (Use Cases) depends strictly on domain abstractions (repository interfaces), never on low-level data implementations or frameworks (Room, Retrofit, SharedPreferences).

---

### 3. KISS (Keep It Simple, Stupid)
- **Avoid Over-Engineering**: Do not create speculative abstractions or unnecessary wrappers for trivial operations.
- **Direct & Readable Code**: Favor straightforward code structures. If a Use Case simply forwards a request to a repository without business rules or transformations, keep its execution direct and lightweight.
- **Clear Naming**: Use unambiguous, domain-oriented names for classes, functions, and variables (e.g., `GetChildAssessmentsUseCase`, `PeriodReadiness`).

---

### 4. DRY (Don't Repeat Yourself)
- **Single Source of Truth**: Business rules, domain validation logic, and calculations (e.g., age calculation, percentile evaluation) must exist in one place within the domain layer.
- **Domain Utility Helpers**: Consolidate repetitive domain logic into pure Kotlin extension functions or domain calculators (e.g., `AgeCalculator`, `GrowthSummaryCalculator`).
- **Clock Abstraction & Time Handling**: Never call `System.currentTimeMillis()` directly in domain calculators or Use Cases. Inject or pass a deterministic `Clock` or reference timestamp to ensure reproducible unit tests.

---

## Offline-First Contracts & Synchronization

- Expose synchronization status via sealed domain models (e.g., `SyncState`: `Idle`, `Syncing`, `Success`, `Failed`).
- Domain Use Cases should stream cached data immediately and orchestrate synchronization requests without blocking immediate UI rendering.

---

## Streaming Operations (AI Chat & Live Summaries)

- Model streaming responses from LLMs or assistants as cold `Flow<ChatStreamEvent>` (e.g., chunks of text, completion tokens, or error signals).
- Ensure Use Cases consuming stream data handle cancellations cleanly and maintain local conversational context.

---

## Domain Layer Structure & Component Design

### 1. Domain Entities & Value Objects
- **Immutable State**: Use Kotlin `data class` with read-only (`val`) properties for entities.
- **Invariants**: Enforce entity integrity at construction or through explicit domain validation methods.
- **Pure Data Structures**: Entities must contain no framework annotations or serialization tags.

```kotlin
data class Child(
    val id: UUID,
    val name: String,
    val birthDate: Long,
    val gender: Gender,
    val isPremature: Boolean = false,
    val gestationalWeeks: Int? = null,
)
```

---

### 2. Use Cases (Interactors)
- **Naming Convention**: Name classes using verb-noun business actions ending with `UseCase` (e.g., `AddChildUseCase`, `GetMeasurementNormsUseCase`).
- **Single Entry Point**: Expose a single `suspend operator fun invoke(...)` function in each Use Case.
- **Explicit Inputs & Outputs**: Use explicit parameters instead of passing raw UI bundles or generic maps. Return structured `Result<T>` or custom domain state.
- **Business Rule Enforcement**: Perform validation and domain rule enforcement inside the Use Case before delegating to repositories.

```kotlin
class AddMeasurementUseCase @Inject constructor(
    private val repository: MeasurementRepository,
) {
    suspend operator fun invoke(
        childId: UUID,
        weight: Double?,
        height: Double?,
        headCircumference: Double?,
    ): Result<Unit> {
        val hasValidMeasurement = listOf(weight, height, headCircumference)
            .any { it != null && it > 0.0 }

        if (!hasValidMeasurement) {
            return Result.failure(
                IllegalArgumentException("At least one measurement must be greater than zero.")
            )
        }

        return repository.addMeasurement(childId, weight, height, headCircumference)
    }
}
```

#### Use Case Orchestration & Inter-Use-Case Injection
Інжектити один Use Case в інший **дозволено**, але лише за умови **суворої односпрямованої оркестрації і не глибше одного рівня вкладеності** (`Max depth = 1`: `HighLevelUseCase -> LowLevelUseCase`).

- **Коли це виправдано (Use Case Orchestration):**
  - **Високорівневий бізнес-сценарій:** Високорівневий Use Case виступає координатором, делегуючи атомарні кроки іншим готовим Use Cases.
    *Приклад:* `SubmitOrderUseCase` викликає `ValidateCartUseCase`, `ReserveInventoryUseCase` та `ProcessPaymentUseCase`.
  - **DRY для критичної валідації:** Складна валідація або перевірка прав доступу вже інкапсульована в окремому Use Case і має запускатися перед операцією.

- **Ризики та чому цим не варто зловживати:**
  - **Циклічні залежності:** Зростання системи легко призводить до ситуацій, де `AUseCase` потребує `BUseCase`, а згодом `BUseCase` починає вимагати `AUseCase`.
  - **Складність тестування:** Мокування Use Case, який всередині викликає інші Use Cases (кожен зі своїми репозиторіями та диспатчерами), роздуває підготовку тестів (`given/when`) і робить юніт-тести крихкими.
  - **Порушення меж транзакцій:** Якщо атомарність операції має контролюватись у рамках однієї транзакції БД, оркестрація декількох Use Cases через репозиторії може призвести до часткових змін при збоях.
  - **Ланцюжки викликів:** Глибина викликів понад 2 рівні (`UC1 -> UC2 -> UC3`) перетворює Use Cases на розподілений спагеті-код.

- **Як краще організувати альтернативи:**
  | Ситуація | Рекомендований підхід |
  | :--- | :--- |
  | **Спільні доменні розрахунки чи правила** | Виносити у **Domain Service** або методи самої **Domain Model / Value Object**, а не створювати штучний Use Case. |
  | **Поєднання кількох потоків даних для UI** | Об'єднувати їх безпосередньо у **ViewModel** через оператори реактивних потоків (`combine`, `zip`), замість створення "прохідного" об'єднуючого Use Case. |
  | **Транзакційні операції над кількома сутностями** | Виносити логіку на рівень **Repository** або спеціалізованого **Domain Service**, який управляє межами транзакції. |

---

### 3. Repository Contracts
- Define repository interfaces inside the domain layer (`:core:domain`).
- Use domain models exclusively in method signatures (no DTOs, entity database models, or response objects).
- Return explicit `Result<T>` for asynchronous one-shot operations or `Flow<T>` for reactive streams.

```kotlin
interface ChildRepository {
    fun getChildren(): Flow<List<Child>>
    suspend fun getChildById(id: UUID): Result<Child>
    suspend fun addChild(child: Child): Result<UUID>
    suspend fun setSelectedChildId(id: UUID)
    fun getSelectedChildId(): Flow<UUID?>
}
```

---

### 4. Error & Result Handling
- Represent expected domain failure states using Kotlin `Result<T>` or sealed domain error hierarchies (e.g., `DomainError`).
- Map low-level exceptions (network errors, SQL exceptions) to meaningful domain errors before reaching Use Cases or Presentation components.

```kotlin
sealed interface DomainError {
    data object NotFound : DomainError
    data object NetworkFailure : DomainError
    data class ValidationError(val reason: String) : DomainError
}
```

---

## Reactive Streams & Coroutines Guidelines

- **Coroutines & Suspended Execution**: All asynchronous business logic functions must be `suspend` functions.
- **Reactive Data Streams**: Use `Flow<T>` for continuous reactive data flows (e.g., database streams, real-time updates).
- **Thread Safety**: Use Cases should be main-safe. Repositories and domain utilities must switch dispatchers appropriately when handling blocking work.

---

## Unidirectional Data Flow (UDF) & Presentation Integration

- **ViewModel Consumption**: ViewModels consume Use Cases to execute actions and observe `Flow<T>` streams from Use Cases/Repositories.
- **State Propagation**: Convert domain streams into UI State flows (`StateFlow<T>`) within Presentation ViewModels.
- **Immutability**: Ensure domain state emitted to presentation components remains immutable.
