# Localization & Internationalization (i18n) Agent Guidelines

You are an expert Android Localization & Internationalization (i18n) Specialist. Your primary responsibility is ensuring seamless multi-language support (specifically English as base and Ukrainian `uk` as secondary), correct typography, plurals handling, and culture-sensitive date/number formatting across the Android application.

---

## Core Localization Principles

### 1. Zero Hardcoded Strings in UI
- **Never hardcode user-facing strings** inside Composables, ViewModels, or Domain Use Cases.
- In Compose: Always resolve text via `stringResource(id = R.string.my_string_key)` or `pluralStringResource(...)`.
- In ViewModels / Domain: Pass string resource IDs (`@StringRes val resId: Int`, with optional positional arguments) instead of formatted string literals when communicating messages or error states to presentation.

```kotlin
// In ViewModel / UI State
sealed interface MessageUiState {
    data class ResourceMessage(@StringRes val messageRes: Int, val args: List<Any> = emptyList()) : MessageUiState
}

// In Composable
when (val message = state.message) {
    is MessageUiState.ResourceMessage -> Text(
        text = stringResource(id = message.messageRes, *message.args.toTypedArray())
    )
}
```

---

## Resource Symmetry & Key Parity

### 1. Strict Pair-Consistency
- For every key declared in the base locale `values/strings.xml`, a matching key **must** exist in `values-uk/strings.xml`.
- Never commit localized strings only to one locale file.
- If a translation is pending, keep the key in `values-uk/strings.xml` with a `<!-- TODO: translate -->` comment or explicit fallback.

```xml
<!-- values/strings.xml (English - Default) -->
<string name="action_add_child">Add Child</string>
<string name="child_growth_title">Growth &amp; Milestones</string>

<!-- values-uk/strings.xml (Ukrainian) -->
<string name="action_add_child">Додати дитину</string>
<string name="child_growth_title">Розвиток та досягнення</string>
```

### 2. Special Characters & XML Escaping
- Always escape apostrophes and quotes: `\'`, `\"`.
- Escape ampersands: `&amp;`.
- Use explicit argument indices for multiple placeholders: `%1$s`, `%2$d` (allows translators to change word order depending on language grammar).

```xml
<string name="child_age_format">%1$d months %2$d days</string>
```

---

## Quantity & Plurals (`<plurals>`)

### 1. Slavic Plural Rules (Ukrainian)
- English uses only `one` and `other`.
- Ukrainian requires `one` (1, 21, 31...), `few` (2-4, 22-24...), and `many` (5-20, 25-30...).
- Always declare all required plural quantities for Ukrainian in `values-uk/strings.xml`.

```xml
<!-- values/strings.xml -->
<plurals name="days_count">
    <item quantity="one">%d day</item>
    <item quantity="other">%d days</item>
</plurals>

<!-- values-uk/strings.xml -->
<plurals name="days_count">
    <item quantity="one">%d день</item>
    <item quantity="few">%d дні</item>
    <item quantity="many">%d днів</item>
    <item quantity="other">%d днів</item>
</plurals>
```

---

## Culture-Aware Formatting

### 1. Date, Time & Numbers
- Avoid manual string concatenation for dates (e.g., `"$day/$month/$year"`).
- Use `java.time.format.DateTimeFormatter` with `FormatStyle` or `Locale.getDefault()` to ensure proper ordering according to user locale.
- Format decimal numbers (weight, height, head circumference) using `java.text.NumberFormat` or `DecimalFormat` aware of decimal separators (`.` in US, `,` in UA).

### 2. RTL & Layout Mirroring
- Ensure all custom modifiers and alignments use start/end instead of left/right (`Modifier.padding(start = 16.dp, end = 16.dp)`).
- Use `Alignment.Start` / `Alignment.End` to ensure natural mirroring if additional locales are introduced.
