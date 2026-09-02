package com.beyonnex.anagram.domain;

import java.util.Objects;

/**
 * The canonical fingerprint of a text's letters: every letter it contains, sorted.
 *
 * <p>Two texts are built from the same letters if and only if their signatures are equal, which
 * turns "find the anagrams of X" from a scan over every known text into a single hash lookup.
 *
 * <p>Sorting is done over Unicode <em>code points</em> rather than {@code char}s so that letters
 * outside the Basic Multilingual Plane (which Java stores as surrogate pairs) are treated as
 * single letters instead of being split in half.
 */
public record AnagramSignature(String sortedLetters) {

    public AnagramSignature {
        Objects.requireNonNull(sortedLetters, "sortedLetters");
    }

    /**
     * Builds the signature of an already-normalised letter sequence.
     *
     * @param letters output of a {@link TextNormalizer}; must contain letters only
     * @return the sorted-letter signature, in O(n log n) on the number of letters
     */
    public static AnagramSignature of(String letters) {
        Objects.requireNonNull(letters, "letters");
        int[] codePoints = letters.codePoints().sorted().toArray();
        return new AnagramSignature(new String(codePoints, 0, codePoints.length));
    }
}
