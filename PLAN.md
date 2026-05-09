🛠 План реалізації проекту CuraKid (Android)

🔐 Автентифікація (/api/auth)
POST /api/auth/register — Реєстрація нового користувача.
POST /api/auth/login — Вхід у систему (отримання Access та Refresh токенів).
POST /api/auth/refresh — Оновлення токена доступу за допомогою Refresh токена.
POST /api/auth/logout — Вихід із системи (анулювання токенів).
POST /api/auth/oauth/google — Вхід/реєстрація через Google OAuth.

👶 Керування профілями дітей (/api/children)
GET /api/children — Отримати список усіх дітей, прив'язаних до поточного користувача.
GET /api/children/{childId} — Отримати детальну інформацію про конкретну дитину.
POST /api/children — Створити новий профіль дитини.
PUT /api/children/{childId} — Оновити дані дитини (ім'я, дата народження, стать).
DELETE /api/children/{childId} — Видалити профіль дитини.

🧠 Оцінка навичок (Milestones) (/api/children/{childId}/assessments)
GET /api/children/{childId}/assessments — Отримати перелік віхових показників (майлстоунів) для дитини, згрупованих за доменами.
POST /api/children/{childId}/assessments — Зберегти статус досягнення віхових показників.

⚖️ Антропометрія (/api/children/{childId}/measurements)
GET /api/children/{childId}/measurements — Отримати історію вимірювань дитини (вага, зріст).
POST /api/children/{childId}/measurements — Додати нове вимірювання.

🧠 Оцінка навичок (/api/children/{childId}/assessments)
GET /api/children/{childId}/assessments — Отримати результати оцінювання (можна фільтрувати за доменом: моторика, мовлення тощо).
POST /api/children/{childId}/assessments — Зберегти результати нового квізу/оцінювання навичок.
NOTE: Реалізовано через вкладку "Milestones" у деталях дитини.

📊 Звіти та Аналітика
GET /api/children/{childId}/reports — Список усіх сформованих звітів для дитини.
GET /api/reports/{reportId} — Отримати детальний контент конкретного звіту (включаючи аналіз та рекомендації LLM).
POST /api/reports/{summaryId}/regenerate — Перезапустити генерацію звіту для закритого періоду.

👤 Профіль користувача (Модуль Profile)
POST /profile/register — Реєстрація (внутрішній/тестовий ендпоінт модуля).
GET /profile/{id}/{email} — Отримання даних профілю за ID та email.
GET /profile/all — Отримання списку всіх профілів (тестовий).

🤖 Intelligence Service (Внутрішні ендпоінти Python FastAPI)
Ці ендпоінти використовуються основним сервісом і захищені Shared Secret:

GET /health — Перевірка працездатності FIS-модуля.
POST /analyze — Виконання аналізу нечіткої логіки (FIS) для визначення критичності стану.
POST /embed — Генерація векторних представлень (embeddings) для медичних текстів.
NOTE

Всі ендпоінти /api/** (крім реєстрації та логіну) потребують JWT-токен у заголовку Authorization. Ендпоінти /api/admin/** у цей список не включено згідно з вашим запитом.

Фаза 1: Фундамент та Інфраструктура (Setup)

    1.1. Налаштування проекту: Створення проекту в Android Studio, конфігурація build.gradle.kts (Version Catalog), підключення Compose, Coroutines та Hilt.

    1.2. Базова архітектура: Створення структури пакетів за шарами: data, domain, presentation, di.

    1.3. Навігація: Реалізація навігаційного графа за допомогою Navigation 3. Створення базових маршрутів (Auth, Dashboard, Profile).

    1.4. Dependency Injection: Конфігурація HiltAndroidApp та створення модулів для Network, Database та Repositories.

Фаза 2: Рівень даних (Data Layer & Networking)

    2.1. Мережевий шар: Опис DTO (Data Transfer Objects) та створення інтерфейсу Retrofit для взаємодії зі Spring Boot.

    2.2. Безпека (Auth): Реалізація AuthInterceptor для додавання JWT та Authenticator для автоматичного оновлення токенів (Refresh Token logic).

    2.3. Локальне сховище: Налаштування Room: створення сутностей (ChildEntity, MeasurementEntity) та DAO.

    2.4. Репозиторії: Реалізація класів репозиторіїв, що поєднують дані з мережі та локального кешу (Offline-first підхід).

Фаза 3: Бізнес-логіка (Domain Layer)

    3.1. Моделі домену: Створення "чистих" Kotlin-моделей даних, вільних від анотацій серіалізації.

    3.2. Use Cases (Auth): Реалізація сценаріїв LoginUseCase, RegisterUseCase.

    3.3. Use Cases (Main): Створення сценаріїв AddMeasurementUseCase, GetChildDevelopmentHistoryUseCase.

    3.4. Use Cases (AI): Реалізація RequestAIAnalysisUseCase для виклику RAG/FIS функціоналу бекенду.

Фаза 4: Рівень представлення (Presentation Layer)

    4.1. Стан екранів: Опис UI State (Lce - Loading/Content/Error) для кожного екрана.

    4.2. ViewModels: Розробка бізнес-орієнтованих ViewModels, які взаємодіють з Use Cases та оновлюють StateFlow.

    4.3. UI Компоненти: Створення бібліотеки спільних компонентів (кнопки, картки, текстові поля) на Jetpack Compose.

    4.4. Екрани (Screens): Верстка основних екранів:

        Dashboard з графіками (Vico/Compose-Charts).

        Екран зростання з HorizontalPager для етапів розвитку та переліком питань.

        Екран деталей дитини з вкладками Growth та Milestones.

Фаза 5: ШІ-інтеграція та UX (Advanced Features)

    5.1. Візуалізація даних: Налаштування графіків розвитку (Z-score) на основі отриманих від бекенду даних.

    5.2. AI Feedback UI: Реалізація інтерактивного відображення згенерованих порад (Markdown підтримка для тексту звіту).

    5.3. Обробка помилок: Реалізація глобального механізму обробки помилок мережі та відображення Snackbar або Dialog.

Фаза 6: Тестування та Полірування

    6.1. Unit Testing: Тестування Use Cases та ViewModels за допомогою Mockk/Turbine.

    6.2. UI Testing: Базові тести на Compose (проходження сценарію авторизації).

    6.3. Оптимізація: Перевірка витоків пам'яті та налаштування R8 (ProGuard).

📈 Ключові етапи (Milestones)

    M1: Skeleton App — Працює навігація між пустими екранами, Hilt налаштований.

    M2: API Sync — Застосунок успішно логіниться та отримує список дітей із Supabase через Spring Boot.

    M3: Core Logic — Можливість додати замір та побачити його на графіку (Room + Retrofit).

    M4: AI Integration — Отримання та відображення повноцінного RAG-звіту з ШІ-модуля.