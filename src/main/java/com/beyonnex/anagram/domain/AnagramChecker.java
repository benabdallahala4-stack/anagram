package com.beyonnex.anagram.domain;

import java.util.Objects;

/** Normalizes input and rejects texts with no letters. Both features go through here. */
public final class AnagramChecker {

    private final TextNormalizer normalizer;

    public AnagramChecker() {
        this(new LetterOnlyTextNormalizer());
    }

    public AnagramChecker(TextNormalizer normalizer) {
        this.normalizer = Objects.requireNonNull(normalizer, "normalizer");
    }

    /** @throws IllegalArgumentException if the text has no letters, i.e. nothing to rearrange */
    public NormalizedText normalize(String text) {
        NormalizedText normalized = normalizer.normalize(text);
        if (!normalized.hasLetters()) {
            throw new IllegalArgumentException("A text must contain at least one letter.");
        }
        return normalized;
    }
}
