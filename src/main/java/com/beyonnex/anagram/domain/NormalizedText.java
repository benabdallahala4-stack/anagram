package com.beyonnex.anagram.domain;

import java.util.Objects;

/**
 * A text as typed, plus its normalized letters. Comparisons use {@code letters};
 * {@code raw} is only for showing the text back to the user.
 */
public record NormalizedText(String raw, String letters) {

    public NormalizedText {
        Objects.requireNonNull(raw, "raw");
        Objects.requireNonNull(letters, "letters");
    }

    public boolean hasLetters() {
        return !letters.isEmpty();
    }

    public AnagramSignature signature() {
        return AnagramSignature.of(letters);
    }

    /** Same word ignoring case and punctuation: "Listen" and "listen", "dog" and "d o g". */
    public boolean isSameWordAs(NormalizedText other) {
        return letters.equals(other.letters);
    }

    /**
     * Wikipedia: an anagram rearranges the letters of a <i>different</i> word or phrase.
     * So: same letters, and not the same word. A text is not an anagram of itself.
     */
    public boolean isAnagramOf(NormalizedText other) {
        return !isSameWordAs(other) && signature().equals(other.signature());
    }
}
