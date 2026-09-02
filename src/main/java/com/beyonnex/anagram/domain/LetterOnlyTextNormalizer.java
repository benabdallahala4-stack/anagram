package com.beyonnex.anagram.domain;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

/**
 * Keeps letters only and ignores case. That is what Wikipedia's own examples need:
 * "anagram" / "nag a ram" and "New York Times" / "monkeys write".
 *
 * <p>Accented letters stay distinct ("café" is not an anagram of "face").
 */
public final class LetterOnlyTextNormalizer implements TextNormalizer {

    @Override
    public NormalizedText normalize(String raw) {
        Objects.requireNonNull(raw, "raw");

        // NFC first, so "e" + combining accent becomes "é" before we drop non-letters.
        String composed = Normalizer.normalize(raw, Normalizer.Form.NFC);
        // Locale.ROOT: under a Turkish default locale "I" lowercases to dotless "ı".
        // Lowercasing can decompose again ("İ" -> "i" + dot), so NFC once more.
        String folded = Normalizer.normalize(composed.toLowerCase(Locale.ROOT), Normalizer.Form.NFC);

        StringBuilder letters = new StringBuilder(folded.length());
        folded.codePoints().filter(Character::isLetter).forEach(letters::appendCodePoint);

        return new NormalizedText(raw, letters.toString());
    }
}
