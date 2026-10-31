---
name: CuraKid
colors:
  surface: '#0F141A'
  surface-dim: '#0F141A'
  surface-bright: '#282C32'
  surface-container-lowest: '#0B0F14'
  surface-container-low: '#181C22'
  surface-container: '#1E2228'
  surface-container-high: '#282C32'
  surface-container-highest: '#33373E'
  on-surface: '#E1E2E8'
  on-surface-variant: '#C2C7CF'
  inverse-surface: '#E1E2E8'
  inverse-on-surface: '#181C22'
  outline: '#8C9199'
  outline-variant: '#42474E'
  surface-tint: '#90CAF9'
  primary: '#90CAF9'
  on-primary: '#003258'
  primary-container: '#00497D'
  on-primary-container: '#D1E4FF'
  inverse-primary: '#565D79'
  secondary: '#81C784'
  on-secondary: '#003910'
  secondary-container: '#00531A'
  on-secondary-container: '#A3F3A3'
  tertiary: '#FFB74D'
  on-tertiary: '#4B2D00'
  tertiary-container: '#693F00'
  on-tertiary-container: '#FFDCC1'
  error: '#FFB4AB'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#FFDAD6'
  background: '#0F141A'
  on-background: '#E1E2E8'
  surface-variant: '#42474E'
  surface-card: '#1E2228'
  surface-elevated: '#282C32'
  accent-success: '#81C784'
  text-primary: '#FFFFFF'
  text-secondary: '#9BA1A6'
  badge-bg: '#00531A'
  nav-bg: '#181C22'
typography:
  app-title:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.02em
  screen-header:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
  section-title:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '500'
    lineHeight: 24px
  card-title:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 22px
  body-meta:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
  badge-button:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
rounded:
  sm: 0.5rem
  DEFAULT: 1rem
  md: 1.5rem
  lg: 2rem
  xl: 3rem
  full: 9999px
spacing:
  gutter: 16px
  margin: 20px
  space-xs: 4px
  space-sm: 8px
  space-md: 12px
  space-lg: 16px
  space-xl: 24px
---

# CuraKid — Design System & Product Specification

## 1. Vision & General Concept
**CuraKid** is a mobile application for caring parents that helps track child development, scheduled doctor visits, vaccinations, growth stages, and important milestones.

* **Platform:** Mobile (iOS / Android)
* **Target Audience:** Parents of children from newborns to teenagers.
* **Tone of Voice:** Caring, calm, professional, friendly, and reliable.

---

## 2. Color System
The design is built on a modern, harmonious dark mode using soothing medical blues, greens, and soft accent orange.

* **Primary Background:** `#0F141A` (Deep dark gray / blue-gray background)
* **Surface Card Background:** `#1E2228` (Soft matte container for cards)
* **Surface Elevated / Variant:** `#42474E` (For interactive elements and hover states)
* **Primary Accent (Medical Blue):** `#90CAF9` (Main interactive color)
* **Secondary Accent (Growth Green):** `#81C784` (Positive indicators, success, growth)
* **Tertiary Accent (Warm Peach):** `#FFB74D` (Warnings and moderate attention)
* **Text Primary:** `#FFFFFF` (Pure white for headers)
* **Text Secondary:** `#9BA1A6` (Muted gray for dates, times, and hints)
* **Badge Background:** `#00531A` (Dark green background for status badges)

---

## 3. Typography
Modern sans-serif font (Inter) with a clear hierarchy.

* **App Title / Brand:** 24px, Bold, Tracking: Tight
* **Screen Header:** 22px, SemiBold
* **Section Title:** 18px, Medium
* **Card Title:** 16px, SemiBold
* **Body / Meta:** 13px, Regular (`#9BA1A6`)
* **Badges / Buttons Text:** 12px, Medium

---

## 4. Layout & Spacing
* **Target Viewport:** Mobile (390px × 844px / 1080×2400)
* **Border Radius (Cards):** `16px` (`rounded-2xl`) for standard cards, `24px` for large containers.
* **Grid / Padding:** Screen inner padding `20px` (`p-5`), card horizontal padding `16px`.
* **Bottom Navigation Bar:** Floating bottom navigation panel with dark background (`#181C22`).

---

## 5. UX Principles
1. **Quick Access to Essentials:** Upcoming doctor visits or vaccinations are always prominently displayed on the main screen.
2. **Minimalist Color Indication:** Green and blue colors signal safety and health.
3. **Inclusivity & Accessibility:** High contrast white text on a dark background for comfortable night-time reading.
