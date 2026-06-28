# Qalam Android — Design System

Source of truth extracted from `Qalam.dc.html`. All colors, fonts, and spacing decisions come from
the interactive prototype. Do not substitute Material 3 defaults without explicit approval.

---

## Color tokens → Compose equivalents

Map every CSS variable to a named Kotlin `Color` in `ui/theme/Color.kt`.

| Token | Hex | Usage |
|---|---|---|
| `--bg` | `#F4EEE2` | App background (scaffold background) |
| `--paper` | `#FBF7EF` | Bottom sheets, nav bar background |
| `--surface` | `#FFFEFB` | Cards, input fields |
| `--surface-2` | `#F1E9DA` | Chip backgrounds, section fills |
| `--surface-3` | `#E7DDC8` | Progress track, skeleton fills |
| `--ink` | `#231F17` | Primary text |
| `--ink-2` | `#6E6555` | Secondary text, labels |
| `--ink-3` | `#9A917D` | Placeholder, hints, icons |
| `--outline` | `#E0D6C2` | Card borders, dividers |
| `--outline-2` | `#D2C6AE` | Stronger dividers |
| `--primary` | `#1F6F5C` | Primary action, active nav, FAB border |
| `--primary-d` | `#155244` | Primary pressed state |
| `--on-primary` | `#FFFFFF` | Text on primary |
| `--primary-c` | `#C3E9DA` | Primary container (mastered pill, summary card) |
| `--on-primary-c` | `#053C2E` | Text on primary container |
| `--gold` | `#B07D26` | Root accent, FAB background |
| `--gold-c` | `#F2E2BD` | Root card background, gold container |
| `--terra` | `#B85537` | Error, "again" state |
| `--terra-c` | `#F6D9CE` | Error container, offline banner, "again" card |
| `--lapis` | `#2F6E9E` | Texts accent, "reviewing" mastery |
| `--lapis-c` | `#D2E4F2` | Lapis container, linked-token highlight |

### Mastery colors

| Level | Label | Color token | Hex |
|---|---|---|---|
| 0 | Unseen | `--m0` | `#A99F8B` |
| 1 | Learning | `--m1` / `--gold` | `#B07D26` |
| 2 | Reviewing | `--m2` / `--lapis` | `#2F6E9E` |
| 3 | Mastered | `--m3` / `--primary` | `#1F6F5C` |

These four values map directly to the backend enum `NEW / LEARNING / KNOWN / MASTERED`.

---

## Typography

Three font families. Add all as downloadable fonts or include as assets.

| Role | Family | Weights | Usage |
|---|---|---|---|
| UI / body | **Hanken Grotesk** | 400, 500, 600, 700 | All UI labels, stats, navigation |
| Serif / italic | **Newsreader** | 400, 500 (regular + italic) | Transliterations, prose notes, session subtitles |
| Arabic | **Amiri** | 400, 700 (regular + bold) | All Arabic text, always RTL |
| Icons | **Material Symbols Rounded** | variable | Use `FILL=1` for active state, `FILL=0` for inactive |

Font sources: Google Fonts. Bundle as `res/font/` assets (not runtime download) for offline use.

### Arabic text rules

- Always `layoutDirection = LayoutDirection.Rtl` or `textDirection = TextDirection.Rtl`
- Minimum 24sp for reading; 30–74sp for hero display
- Line height: 1.3–1.7× for body, 1.1× for large display
- Never clip Arabic with `maxLines` without testing — diacritics extend beyond ascenders/descenders

---

## Spacing & shape

| Element | Corner radius | Padding |
|---|---|---|
| Large cards | 20–26dp | 16–18dp |
| Small chips | 14–20dp (pill) | 5dp v / 11–14dp h |
| Bottom sheet | 26dp top corners | 22dp h / 28dp bottom |
| Buttons (primary) | 18dp | 16dp v / full width |
| FAB | 20dp | 22dp h, 60dp height |
| List rows | 18dp | 14–16dp |
| Training card | 28dp | 26–30dp |

Standard horizontal page padding: **20dp** on all list screens. Detail screens: **22dp**.

---

## Screen inventory

| Screen | Nav bar | FAB | Back button |
|---|---|---|---|
| Home | ✓ | ✓ (gold, "Train") | — |
| Words list | ✓ | ✓ (gold, "Train") | — |
| Word detail | ✓ | — | ← arrow_back |
| Roots list | ✓ | — | — |
| Root detail | ✓ | — | ← arrow_back |
| Texts list | ✓ | — | — |
| Text reader | ✓ | — | ← arrow_back |
| Training | — | — | ✕ close (goes home) |
| Summary | — | — | — |

### Bottom navigation tabs

Order: Home · Words · Roots · Texts
Active state: filled icon + primary-container pill background.
Inactive: outlined icon + no pill.

Icons (Material Symbols Rounded):
- Home: `home`
- Words: `translate`
- Roots: `account_tree`
- Texts: `menu_book`

---

## Key interaction patterns

### Training card swipe
- Pointer/touch drag → `translateX` + `rotate(dx * 0.04deg)`
- Swipe > 90dp right → "KNEW IT ✓" badge fades in (left side), grade correct
- Swipe > 90dp left → "AGAIN ↻" badge fades in (right side), grade incorrect
- Release < 90dp → spring back with `animateFloatAsState`

### Token tap (Text reader)
- Tap a token → bottom sheet slides up (sheetUp animation)
- Sheet shows large Arabic, transliteration, gloss
- If token linked to a vocabulary word: "View full entry" button → navigate to Word detail
- If not linked: italic note "Not yet in your vocabulary"
- Tap scrim to dismiss

### Offline banner
- Shown at top (below status bar) when backend unreachable
- Background: `--terra-c`, icon: `cloud_off`, text: "Backend unreachable — showing cached data"
- "Retry" link triggers connection recheck

### Mastery progress bar (Word detail)
- 4 segments, filled up to current mastery level with mastery color
- Unfilled segments: `--surface-3`

---

## Animations

| Name | Trigger | Spec |
|---|---|---|
| `fadeUp` | Screen enter (list screens) | opacity 0→1, translateY 10dp→0, 350–400ms ease |
| `slideIn` | Detail screen enter | opacity 0→1, translateX 16dp→0, 300ms ease |
| `popIn` | FAB appear | opacity 0→1, scale 0.96→1, 300ms ease |
| `pulse` | Connection dot (online) | opacity 1→0.35→1, 2.4s infinite |
| `sheetUp` | Bottom sheet open | translateY 100%→0, 260ms ease |

In Compose: use `AnimatedVisibility`, `animateFloatAsState`, `Animatable`.
