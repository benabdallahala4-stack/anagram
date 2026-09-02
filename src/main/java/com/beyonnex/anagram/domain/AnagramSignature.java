package com.beyonnex.anagram.domain;

import java.util.Objects;

/**
 * A text's letters, sorted. Two texts are made of the same letters exactly when their signatures
 * are equal, so this is also the lookup key in the history.
 */
public record AnagramSignature(String sortedLetters) {

    public AnagramSignature {
        Objects.requireNonNull(sortedLetters, "sortedLetters");
    }

    public static AnagramSignature of(String letters) {
        // Sort code points, not chars: letters outside the BMP are stored as surrogate pairs.
        int[] codePoints = letters.codePoints().sorted().toArray();
        return new AnagramSignature(new String(codePoints, 0, codePoints.length));
    }
}
