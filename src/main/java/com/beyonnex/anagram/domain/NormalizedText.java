package com.beyonnex.anagram.domain;

import java.util.Objects;

/**
 * A text paired with the letter sequence the anagram rules compare it by.
 *
 * <p>Only these two values are state; the signature is derived from {@code letters} on demand rather
 * than stored, so an instance cannot exist whose signature disagrees with its letters. Callers that
 * need the signature repeatedly — the store, which uses it as a map key — ask for it once and hold
 * on to it.
 *
 * @param raw     the text exactly as the user typed it; this is what we echo back to them
 * @param letters the normalised letter sequence (case-folded, non-letters removed)
 */
public record NormalizedText(String raw, String letters) {

    public NormalizedText {
        Objects.requireNonNull(raw, "raw");
        Objects.requireNonNull(letters, "letters");
    }

    /** Whether this text contains at least one letter, i.e. whether it is a word or phrase at all. */
    public boolean hasLetters() {
        return !letters.isEmpty();
    }

    /** @return the sorted letters, which decide whether two texts are built from the same letters */
    public AnagramSignature signature() {
        return AnagramSignature.of(letters);
    }

    /**
     * Whether two texts are the same word or phrase, judged on the normalised letters rather than the
     * raw input: {@code "Listen"} and {@code "listen"} are one word typed twice, as are {@code "dog"}
     * and {@code "d o g"}.
     */
    public boolean isSameWordAs(NormalizedText other) {
        Objects.requireNonNull(other, "other");
        return letters.equals(other.letters);
    }

    /**
     * The anagram rule, in one place.
     *
     * <p>Wikipedia defines an anagram as a word or phrase formed by rearranging the letters of
     * <em>a different</em> word or phrase, using all the original letters exactly once. That yields
     * exactly two conditions, and the second one is the easy one to miss:
     *
     * <ol>
     *   <li><b>Same letters.</b> Equal signatures &mdash; every letter reused exactly as often.</li>
     *   <li><b>Different word or phrase.</b> So no text is an anagram of itself, and restyling a text
     *       does not produce one.</li>
     * </ol>
     */
    public boolean isAnagramOf(NormalizedText other) {
        return !isSameWordAs(other) && signature().equals(other.signature());
    }
}
