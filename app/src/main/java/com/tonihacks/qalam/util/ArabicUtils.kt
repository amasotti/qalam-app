package com.tonihacks.qalam.util

/**
 * Strip Arabic tashkeel (harakat/vowel diacritics) from a string, leaving only the consonantal skeleton.
 *
 * Unicode ranges removed:
 *  U+064B–U+065F  — standard tashkeel block (fathatan through wavy hamza below)
 *  U+0670         — superscript alef (daggered alef, used as a diacritic)
 *  U+0640         — tatweel (kashida/elongation mark, omitted for canonical comparison)
 *
 * Callers typically use the stripped form for duplicate detection only; the original
 * (vocalized) text should still be stored/displayed as-is.
 */
fun stripDiacritics(arabic: String): String =
    arabic.replace(Regex("[\u064B-\u065F\u0670\u0640]"), "")
