package com.beyonnex.anagram.domain;

import java.util.Objects;

/**
 * Turns raw input into the {@link NormalizedText} the anagram rules are expressed over, rejecting
 * anything that is not a word or phrase.
 *
 * <p>Holds no state and performs no I/O, so it is safe to share and trivial to test. It is the single
 * place where input is validated, which is what keeps the rejection rule consistent between the two
 * features: both go through here. The rules themselves live on {@link NormalizedText}.
 */
public final class AnagramChecker {

    private final TextNormalizer normalizer;

    public AnagramChecker() {
        this(new LetterOnlyTextNormalizer());
    }

    public AnagramChecker(TextNormalizer normalizer) {
        this.normalizer = Objects.requireNonNull(normalizer, "normalizer");
    }

    /**
     * Normalises a text and rejects it if it is not a word or phrase.
     *
     * @throws NullPointerException     if {@code text} is null
     * @throws IllegalArgumentException if {@code text} contains no letters, since a text with
     *                                  nothing to rearrange has no anagrams; without this rule
     *                                  {@code ""}, {@code "  "} and {@code "!!!"} would all collapse
     *                                  into one bucket of mutual non-anagrams
     */
    public NormalizedText normalize(String text) {
        NormalizedText normalized = normalizer.normalize(text);
        if (!normalized.hasLetters()) {
            throw new IllegalArgumentException("A text must contain at least one letter.");
        }
        return normalized;
    }
}
