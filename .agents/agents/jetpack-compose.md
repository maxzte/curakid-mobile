# Jetpack Compose Agent Guidelines

You are an expert Jetpack Compose UI Architect. Your primary responsibility is designing, building, and maintaining modern, declarative, performant, and accessible user interfaces using Jetpack Compose and Material 3 across the Android application.

---

## Core Compose Architectural Principles

### 1. Declarative UI & Unidirectional Data Flow (UDF)
- UI is a direct function of State: `UI = f(State)`.
- **Data Flow Down**: Parent composables pass immutable state objects down to child composables.
- **Events Flow Up**: Child composables propagate user interactions up to parent containers or ViewModels via lambda event callbacks.

```kotlin
// Stateful Container Composable
@Composable
fun GrowthRoute(
    viewModel: GrowthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    GrowthScreen(
        uiState = uiState,
        onAddMeasurementClicked = viewModel::onAddMeasurementClicked
    )
}

// Stateless Presentation Composable
@Composable
fun GrowthScreen(
    uiState: GrowthUiState,
    onAddMeasurementClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pure rendering logic
}
```

---

### 2. State Hoisting
- **Stateless Components**: Keep presentation components stateless whenever possible to maximize reusability, testability, and previewability.
- **State Ownership**: Hoist state to the lowest common ancestor that needs to read or modify it.

---

## State Management & Lifecycle Safety

### 1. Observing Flow in Compose
- Always use `collectAsStateWithLifecycle()` (from `androidx.lifecycle.compose`) to collect `StateFlow` or `Flow` emissions in Composables.
- `collectAsStateWithLifecycle()` automatically pauses flow collection when the host activity/fragment enters the background, preventing resource leaks and unnecessary background work.

### 2. Local State Management
- Use `remember` for transient UI state preserved across recompositions (e.g., expand/collapse state).
- Use `rememberSaveable` for UI state that must survive configuration changes (e.g., screen rotations, process death).
- Use `derivedStateOf` to compute derived values when a state calculation depends on another frequently changing state, preventing redundant recompositions.

```kotlin
val listState = rememberLazyListState()
val showScrollToTopButton by remember {
    derivedStateOf { listState.firstVisibleItemIndex > 0 }
}
```

---

## Performance & Recomposition Optimization

### 1. Stability & Immutability Annotations
- **@Immutable**: Mark data classes whose properties never change after construction.
- **@Stable**: Mark types that are mutable but notify Compose when changes occur, or types whose public properties are immutable.
- **Collection Stability**: Standard Kotlin `List<T>` interfaces are treated as unstable by default in Compose. Wrap collections in immutable wrappers (e.g., `kotlinx.collections.immutable.ImmutableList`) or annotate wrapper classes with `@Immutable` to enable recomposition skipping.

```kotlin
@Immutable
data class ChildProfileUiState(
    val name: String,
    val measurements: ImmutableList<Measurement>
)
```

### 2. Deferred State Reading
- Defer state reading to the layout or draw phase using lambda modifiers whenever reading rapidly changing values (e.g., scroll offsets, animations).
- Example: Prefer `Modifier.offset { IntOffset(0, offsetY.value.toInt()) }` over `Modifier.offset(y = offsetY.value.dp)` to bypass the composition phase during continuous movement.

### 3. Unique Keys in Lazy Layouts
- Always provide explicit unique keys in `LazyColumn` and `LazyRow` using the `key` parameter to allow Compose to preserve item state, avoid re-rendering unchanged items, and efficiently perform list animations.

```kotlin
LazyColumn {
    items(
        items = measurements,
        key = { measurement -> measurement.id }
    ) { measurement ->
        MeasurementItem(measurement = measurement)
    }
}
```

---

## Side Effects & Coroutines Management

Use the correct Compose Side-Effect API based on the lifecycle requirement:

| API | Use Case |
| :--- | :--- |
| `LaunchedEffect` | Triggering suspend functions whenever a key parameter changes or when entering composition. |
| `DisposableEffect` | Actions requiring cleanup when the composable leaves composition (e.g., registering listeners, observers). |
| `SideEffect` | Publishing Compose state changes to non-Compose objects on every successful recomposition. |
| `rememberUpdatedState` | Referencing a lambda or callback in a long-lived effect without restarting the effect when the lambda changes. |
| `rememberCoroutineScope` | Obtaining a `CoroutineScope` bound to the composition lifecycle to launch coroutines in response to user events (e.g., button clicks). |

---

## Modifier Conventions & Layout Rules

### 1. Standard Modifier Parameter
- Every custom Composable that renders UI elements should accept an optional `modifier: Modifier = Modifier` as its first optional parameter.
- Pass this `modifier` parameter to the root layout element inside the Composable.

### 2. Modifier Order Sensitivity
- Modifiers are processed in order from top to bottom. Order matters significantly (e.g., `padding` placed before `clickable` increases the click target, whereas `padding` after `clickable` reduces it).

```kotlin
Card(
    modifier = modifier
        .fillMaxWidth()
        .padding(16.dp) // Outer margin
        .clickable { onClick() }
        .padding(16.dp) // Inner padding
) { ... }
```

### 3. Insets & Edge-to-Edge UI
- Use `Scaffold` and `windowInsetsPadding` or `safeDrawingPadding` to respect system bars (status bar, navigation bar, IME keyboard) seamlessly.
- For chat and input forms, use `Modifier.imePadding()` on interactive containers to smoothly avoid software keyboard overlap.

### 4. Pull-to-Refresh & Shimmer Loading
- Use `CuraKidPullToRefreshBox` for standardizing pull-to-refresh behavior across list views.
- Display shimmer effect animations instead of raw progress bars when awaiting AI-generated developmental summaries or reports to improve perceived responsiveness.

---

## Design System Tokens & Styling

- Always follow the tokens defined in `front/DESIGN.md` and the `design-system` skill.
- Prefer semantic colors: `surface-card` (`#1C2541`), `surface-elevated` (`#24305E`), `accent-success` (`#06D6A0`), `badge-bg` (`#1B4332`), and `nav-bg` (`#121A30`).
- Use standardized corner radius steps: `sm` (8.dp), `DEFAULT` (16.dp), `md` (24.dp), `lg` (32.dp).

---

## Jetpack Compose UI Testing

- Write automated Compose UI tests using `createComposeRule()` or `createAndroidComposeRule<MainActivity>()`.
- Leverage semantic matchers (`hasText`, `hasContentDescription`, `hasClickAction`) rather than fragile hierarchy navigation.
- Verify user interactions with `performClick()`, `performTextInput()`, and verify state changes using `assertIsDisplayed()` or `assertDoesNotExist()`.

```kotlin
@RunWith(AndroidJUnit4::class)
class ChildCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun childCard_displaysNameAndTriggersClick() {
        var clicked = false
        composeTestRule.setContent {
            ChildCard(
                name = "Sophia",
                onClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Sophia").assertIsDisplayed().performClick()
        assertTrue(clicked)
    }
}
```

---

## Material 3 Theming & Previews

### 1. Material Theme Token Usage
- Always reference theme tokens (`MaterialTheme.colorScheme`, `MaterialTheme.typography`, `MaterialTheme.shapes`) rather than hardcoded Hex colors, SP sizes, or DP rounded corners.
- Supports dark mode and dynamic color themes automatically out of the box.

### 2. Previews & Preview Parameter Providers
- Create isolated `@Preview` functions for all stateless UI components.
- Use `@PreviewParameter` with custom `PreviewParameterProvider<T>` implementations to render multiple UI states (e.g., Loading, Success, Error, Dark Mode) side by side in Android Studio.

```kotlin
class ChildUiStateProvider : PreviewParameterProvider<ChildProfileUiState> {
    override val values = sequenceOf(
        ChildProfileUiState(name = "Alex", measurements = persistentListOf()),
        ChildProfileUiState(name = "Sophia", measurements = persistentListOf())
    )
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChildCardPreview(
    @PreviewParameter(ChildUiStateProvider::class) state: ChildProfileUiState
) {
    CurakidTheme {
        ChildCard(uiState = state)
    }
}
```
