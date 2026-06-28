package com.tonihacks.qalam.ui.theme

import androidx.compose.ui.graphics.Color

// -- Background tiers

val QalamBg = Color(0xFFF4EEE2) // scaffold background
val QalamPaper    = Color(0xFFFBF7EF)   // bottom sheets, nav bar
val QalamSurface  = Color(0xFFFFFEFB)   // cards, input fields
val QalamSurface2 = Color(0xFFF1E9DA)   // chips, section fills
val QalamSurface3 = Color(0xFFE7DDC8)   // progress track, skeleton

// ── Ink (text / icons) ───────────────────────────────────────────────────────
val QalamInk  = Color(0xFF231F17)
val QalamInk2 = Color(0xFF6E6555)
val QalamInk3 = Color(0xFF9A917D)

// ── Borders ───────────────────────────────────────────────────────────────────
val QalamOutline  = Color(0xFFE0D6C2)
val QalamOutline2 = Color(0xFFD2C6AE)

// ── Primary (mastered green) ──────────────────────────────────────────────────
val QalamPrimary      = Color(0xFF1F6F5C)
val QalamPrimaryD     = Color(0xFF155244)   // pressed state
val QalamOnPrimary    = Color(0xFFFFFFFF)
val QalamPrimaryC     = Color(0xFFC3E9DA)   // container: mastered pill, summary card
val QalamOnPrimaryC   = Color(0xFF053C2E)

// ── Gold (roots) ──────────────────────────────────────────────────────────────
val QalamGold  = Color(0xFFB07D26)
val QalamGoldC = Color(0xFFF2E2BD)

// ── Terra (errors / "again") ──────────────────────────────────────────────────
val QalamTerra  = Color(0xFFB85537)
val QalamTerraC = Color(0xFFF6D9CE)

// ── Lapis (texts / reviewing) ────────────────────────────────────────────────
val QalamLapis  = Color(0xFF2F6E9E)
val QalamLapisC = Color(0xFFD2E4F2)

// ── Mastery levels ────────────────────────────────────────────────────────────
// Backend enum: NEW / LEARNING / KNOWN / MASTERED
val MasteryUnseen    = Color(0xFFA99F8B)
val MasteryLearning  = QalamGold       // alias for clarity at call sites
val MasteryReviewing = QalamLapis
val MasteryMastered  = QalamPrimary