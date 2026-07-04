# Qalam Android — Design System

The visual contract for the app: color tokens, typography, spacing, and interaction patterns.
Material 3 is the underlying engine; every M3 role is mapped to a Qalam token so built-in components
(`TextField`, `NavigationBar`, `TopAppBar`, chips, buttons) inherit the correct identity automatically.
Tokens live in `ui/theme/` (`Color.kt`, `Type.kt`, `Theme.kt`).

> **Rule:** always use Qalam tokens — `Typography.*` and the named color constants (`QalamPrimary`,
> `QalamTerra`, `QalamInk`, `QalamBg`, …). Never use `MaterialTheme.typography.*` or
> `MaterialTheme.colorScheme.*` directly; the M3 defaults bypass the custom fonts and palette.
> **Light mode only** — no dark variants.

## Color tokens & Material 3 roles

| M3 Role | Qalam token | Hex | Usage |
|---|---|---|---|
| `background` | `--bg` | `#F4EEE2` | App background (scaffold) |
| `surface` | `--surface` | `#FFFEFB` | Cards, input fields |
| `surfaceVariant` | `--surface-2` | `#F1E9DA` | Chip backgrounds, section fills |
| `primary` | `--primary` | `#1F6F5C` | Primary action, active nav |
| `primaryContainer` | `--primary-c` | `#C3E9DA` | Mastered pill, summary card |
| `onPrimaryContainer` | `--on-primary-c` | `#053C2E` | Text on primary container |
| `outline` | `--outline` | `#E0D6C2` | Card borders, dividers |
| `error` | `--terra` | `#B85537` | Error, "again" state |
| `errorContainer` | `--terra-c` | `#F6D9CE` | Error container, "again" card |
| (custom) | `--paper` | `#FBF7EF` | Bottom sheets, nav bar background |
| (custom) | `--gold` | `#B07D26` | Root accent, FAB background |

### All tokens

| Token | Hex | Role / mapping |
|---|---|---|
| `--bg` | `#F4EEE2` | `background` |
| `--paper` | `#FBF7EF` | secondary surface |
| `--surface` | `#FFFEFB` | `surface` |
| `--surface-2` | `#F1E9DA` | `surfaceVariant` |
| `--surface-3` | `#E7DDC8` | skeleton / track |
| `--ink` | `#231F17` | `onBackground`, `onSurface` |
| `--ink-2` | `#6E6555` | `onSurfaceVariant` |
| `--ink-3` | `#9A917D` | placeholders |
| `--outline` | `#E0D6C2` | `outline` |
| `--outline-2` | `#D2C6AE` | stronger outline |
| `--primary` | `#1F6F5C` | `primary` |
| `--primary-d` | `#155244` | primary pressed |
| `--on-primary` | `#FFFFFF` | `onPrimary` |
| `--primary-c` | `#C3E9DA` | `primaryContainer` |
| `--on-primary-c` | `#053C2E` | `onPrimaryContainer` |
| `--gold` | `#B07D26` | root accent / FAB |
| `--gold-c` | `#F2E2BD` | gold container |
| `--terra` | `#B85537` | `error` |
| `--terra-c` | `#F6D9CE` | `errorContainer` |
| `--lapis` | `#2F6E9E` | texts accent |
| `--lapis-c` | `#D2E4F2` | lapis container |

### Mastery colors

| Level | Label | Token | Hex | Backend enum |
|---|---|---|---|---|
| 0 | Unseen | `--m0` | `#A99F8B` | `NEW` |
| 1 | Learning | `--m1` / `--gold` | `#B07D26` | `LEARNING` |
| 2 | Reviewing | `--m2` / `--lapis` | `#2F6E9E` | `KNOWN` |
| 3 | Mastered | `--m3` / `--primary` | `#1F6F5C` | `MASTERED` |

## Typography

| Role | Family | Weights | Usage |
|---|---|---|---|
| UI / body | **Hanken Grotesk** | 400, 500, 600, 700 | All UI labels, stats, navigation |
| Serif / italic | **Newsreader** | 400, 500 (+ italic) | Transliterations, prose notes, session subtitles |
| Arabic | **Amiri** | 400, 700 (+ bold) | All Arabic text, always RTL |
| Icons | **Material Symbols Rounded** | variable | `FILL=1` active, `FILL=0` inactive |

Fonts are bundled in `res/font/` (not runtime-downloaded) for offline use.

### Arabic text rules

- Always `layoutDirection = LayoutDirection.Rtl` (or `textDirection = TextDirection.Rtl`).
- Minimum 24sp for reading; 30–74sp for hero display.
- Line height 1.3–1.7× for body, ~1.1× for large display.
- Never clip Arabic with `maxLines` untested — diacritics extend beyond ascenders/descenders.

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

Standard horizontal page padding: **20dp** on list screens, **22dp** on detail screens.

## Navigation & screen inventory

Bottom tabs, in order: **Home · Words · Roots · Texts**. Active = filled icon + `primaryContainer`
pill; inactive = outlined icon, no pill. Icons (Material Symbols Rounded): `home`, `translate`,
`account_tree`, `menu_book`.

| Screen | Nav bar | FAB | Back |
|---|---|---|---|
| Home | ✓ | ✓ (gold, "Train") | — |
| Words list | ✓ | ✓ (gold, "Train") | — |
| Word detail | ✓ | — | ← arrow_back |
| Roots list / detail | ✓ | — | — / ← |
| Texts list / reader | ✓ | — | — / ← |
| Training | — | — | ✕ (home) |
| Summary | — | — | — |

## Key interaction patterns

- **Training swipe** — drag → `translateX` + `rotate(dx·0.04°)`. Past 90dp right → "KNEW IT ✓"
  (grade correct); past 90dp left → "AGAIN ↻" (grade incorrect); released under 90dp → spring back via
  `animateFloatAsState`.
- **Token tap (reader)** — bottom sheet slides up with large Arabic, transliteration, gloss. Linked
  token → "View full entry" navigates to Word detail; unlinked → italic "Not yet in your vocabulary".
- **Mastery bar (Word detail)** — 4 segments filled to current level with the mastery color; unfilled
  segments use `--surface-3`.
- **AI sections (Word detail)** — AI examples and AI insight render on demand; on `503` the section
  shows an unavailable fallback rather than an error.

## Animations

| Name | Trigger | Spec |
|---|---|---|
| `fadeUp` | List-screen enter | opacity 0→1, translateY 10dp→0, 350–400ms ease |
| `slideIn` | Detail-screen enter | opacity 0→1, translateX 16dp→0, 300ms ease |
| `popIn` | FAB appear | opacity 0→1, scale 0.96→1, 300ms ease |
| `pulse` | Connection dot (online) | opacity 1→0.35→1, 2.4s infinite |
| `sheetUp` | Bottom sheet open | translateY 100%→0, 260ms ease |

In Compose: `AnimatedVisibility`, `animateFloatAsState`, `Animatable`.
