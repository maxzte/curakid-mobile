---
name: design-system
description: Guide and token mappings for the CuraKid Design System based on DESIGN.md.
  Covers dark-mode surface palette, custom card/elevation tones, Inter typography,
  rounded shape radii, and spacing scales for Jetpack Compose components.
license: Complete terms in LICENSE.txt
metadata:
  author: CuraKid Team
  keywords:
  - design system
  - material3
  - compose
  - tokens
  - colors
  - typography
  - shapes
---

# CuraKid Design System Skill

This skill provides comprehensive tokens and UI guidelines derived directly from `front/DESIGN.md` for consistent UI development with Jetpack Compose.

---

## 🎨 Color Tokens & Palette

CuraKid features an elegant dark-theme design palette. Whenever constructing or reviewing Jetpack Compose UI, reference these specific color definitions or theme extension tokens:

| Token Name | Hex Code | Purpose / Usage |
| :--- | :--- | :--- |
| `surface` | `#0F141A` | Default dark screen background |
| `surface-container-low` | `#181C22` | Slightly elevated sections, group containers |
| `surface-container` | `#1E2228` | Standard container surface |
| `surface-card` | `#1E2228` | Medical and developmental cards, primary content containers |
| `surface-elevated` | `#282C32` | Elevated active cards, modals, bottom sheets |
| `primary` | `#90CAF9` | Main interactive brand color (buttons, active indicators) |
| `primary-container` | `#00497D` | Deep brand background for badges and highlighted blocks |
| `secondary` | `#81C784` | Secondary accent, positive indicators |
| `accent-success` | `#81C784` | Success badges, completed milestones, verified status |
| `badge-bg` | `#00531A` | Background for success/completed milestone chips |
| `tertiary` | `#FFB74D` | Warm alert/accent, moderate urgency indicator |
| `error` | `#FFB4AB` | Error text, critical alerts, high urgency indicator |
| `text-primary` | `#FFFFFF` | Primary high-emphasis headers and body text |
| `text-secondary` | `#9BA1A6` | Subtitles, timestamps, metadata labels |
| `nav-bg` | `#181C22` | Bottom navigation bar background |

---

## ✍️ Typography Guidelines

CuraKid typography is centered around the **Inter** font family:

- **App Title**: 24sp, Bold (700), line height 32sp, letter spacing -0.02em.
- **Screen Header**: 22sp, SemiBold (600), line height 28sp.
- **Section Title**: 18sp, Medium (500), line height 24sp.
- **Card Title**: 16sp, SemiBold (600), line height 22sp.
- **Body Meta**: 13sp, Normal (400), line height 18sp.
- **Badge / Button**: 12sp, Medium (500), line height 16sp.

---

## 📐 Shape & Corner Radii

Always align `RoundedCornerShape` values with the design specification:

- `sm`: `8.dp` (Small chips, secondary tag badges, input fields)
- `DEFAULT`: `16.dp` (Standard cards, dialogs, item cards)
- `md`: `24.dp` (Large bottom sheets, highlighted banners)
- `lg`: `32.dp` (Top navigation curved sheets, onboarding hero containers)
- `full`: `50` or `CircleShape` (Avatars, floating action buttons, pill buttons)

---

## 📏 Spacing Scale

- `space-xs`: `4.dp`
- `space-sm`: `8.dp`
- `space-md` / `gutter`: `16.dp`
- `margin`: `20.dp`
- `space-lg`: `24.dp`
- `space-xl`: `32.dp`

---

## 🧩 Standard Component Recipes

### 1. Milestone / Assessment Card
```kotlin
Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2228)),
    modifier = modifier.fillMaxWidth()
) {
    // Content with text-primary & text-secondary
}
```

### 2. Status Badge / Chip
```kotlin
Surface(
    shape = RoundedCornerShape(8.dp),
    color = Color(0xFF00531A),
    contentColor = Color(0xFF81C784)
) {
    Text(
        text = statusText,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
```
