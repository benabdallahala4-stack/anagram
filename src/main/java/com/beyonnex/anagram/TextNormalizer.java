package com.beyonnex.anagram;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

/**
 * Reduces a text to the letters that count: letters only, case ignored. That is what Wikipedia's
 * own examples need ("anagram" / "nag a ram", "New York Times" / "monkeys write").
 *
 * <p>Accented letters stay distinct ("café" is not an anagram of "face").
 */
public final class TextNormalizer {

    private TextNormalizer() {
    }

    /** @throws IllegalArgumentException if the text has no letters, i.e. nothing to rearrange */
    public static NormalizedText normalize(String raw) {
        Objects.requireNonNull(raw, "raw");

        // NFC first, so "e" + combining accent becomes "é" before non-letters are dropped.
        String composed = Normalizer.normalize(raw, Normalizer.Form.NFC);
        // Locale.ROOT: under a Turkish default locale "I" lowercases to dotless "ı".
        // Lowercasing can decompose again ("İ" -> "i" + dot), so NFC once more.
        String folded =
                Normalizer.normalize(composed.toLowerCase(Locale.ROOT), Normalizer.Form.NFC);

        StringBuilder letters = new StringBuilder(folded.length());
        folded.codePoints().filter(Character::isLetter).forEach(letters::appendCodePoint);

        if (letters.length() == 0) {
            throw new IllegalArgumentException("A text must contain at least one letter.");
        }
        return new NormalizedText(raw, letters.toString());
    }
}
