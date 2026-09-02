package com.beyonnex.anagram.domain;

/**
 * Reduces a raw text to the letter sequence the anagram rules compare.
 *
 * <p>Which characters survive normalisation <em>is</em> the definition of an anagram in practice,
 * so it is an extension point: swap in a different implementation to preserve digits, fold
 * diacritics, or apply language-specific rules without touching the rest of the program.
 */
@FunctionalInterface
public interface TextNormalizer {

    /**
     * @param raw the text as entered
     * @return the raw text plus its normalised and sorted forms
     * @throws NullPointerException if {@code raw} is null
     */
    NormalizedText normalize(String raw);
}
