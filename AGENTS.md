# CuraKid Android Application Architecture

## 1. Layered Distribution (Clean Architecture)

The application is divided into three logical layers to separate business logic from the interface and network implementation details.

### Presentation Layer (UI)
- **Jetpack Compose:** Modern declarative framework for building the user interface.
- **ViewModels:** Maintain the screen state (State) and handle user events. Use `StateFlow` for reactive UI updates.
- **HorizontalPager:** Used for switching between developmental stages (months) on the Growth screen.
- **Navigation 3:** Used for handling navigation between screens.

### Domain Layer (Business Logic)
- **Use Cases:** Individual classes for each action (e.g., `GetChildReportUseCase`, `AddMeasurementUseCase`, `GetMilestonesUseCase`, `SaveAssessmentUseCase`). This is the "brain" of the application, independent of libraries.
- **Models:** Pure Kotlin data classes describing entities (e.g., `Child`, `Measurement`, `Milestone`).

### Data Layer (Infrastructure)
- **Repositories:** Implement interfaces from the Domain layer. They decide where to fetch data from: cache or server.
- **Remote Data Source (Retrofit):** Interaction with the Spring Boot backend via REST API (including AI-powered reports).
- **Local Data Source (Room):** Local database for offline access to basic child data.

---

## 🛠 Technology Stack

| Component | Technology | Role |
| :--- | :--- | :--- |
| **Language** | Kotlin | Primary development language. |
| **UI Framework** | Jetpack Compose | Building reactive interfaces. |
| **Navigation** | Navigation 3 | Modern navigation management. |
| **DI Framework** | Hilt | Dependency Injection for modularity. |
| **Networking** | Retrofit 2 + OkHttp | Requests to Spring Boot and JWT handling. |
| **Async / Stream** | Coroutines & Flow | Asynchronous work and data streams. |
| **Local DB** | Room | Data caching for offline mode. |
| **Charts** | Compose-Charts / Vico | Child development chart visualization. |

---

## 🔄 Data Flow (Unidirectional Data Flow - UDF)

1. **User Action:** User clicks the "Get AI Analysis" button.
2. **ViewModel:** Calls the corresponding `UseCase`.
3. **UseCase:** Requests data from the `Repository`.
4. **Repository:** Makes a network request via Retrofit.
5. **Backend (Spring AI):** Processes data via FIS and RAG and returns JSON.
6. **Repository:** Receives the response, saves it to Room if necessary, and returns it to the `UseCase`.
7. **ViewModel:** Updates `StateFlow`, the UI automatically recomposes to show the report.

---

## 🔐 Security & UX Features

- **AuthInterceptor:** Automatically adds the JWT token (received during login) to every request in the `Authorization` header.
- **Token Refresh:** If the server returns 401, the app automatically uses a Refresh Token to get a new pair of tokens without logging out the user.
- **Shimmer Effect:** Shows a skeleton animation while the AI report is loading to improve perceived performance (LLMs take time to generate).
