package com.beyonnex.anagram.domain;

import java.util.Objects;

/**
 * A text paired with the two derived forms the anagram rules are expressed in.
 *
 * @param raw       the text exactly as the user typed it; this is what we echo back to them
 * @param letters   the normalised letter sequence (case-folded, non-letters removed), which decides
 *                  whether two texts are the <em>same</em> word or phrase
 * @param signature the sorted letters, which decides whether two texts are built from the
 *                  <em>same letters</em>
 */
public record NormalizedText(String raw, String letters, AnagramSignature signature) {

    public NormalizedText {
        Objects.requireNonNull(raw, "raw");
        Objects.requireNonNull(letters, "letters");
        Objects.requireNonNull(signature, "signature");
    }

    /** Whether this text contains at least one letter, i.e. whether it is a word or phrase at all. */
    public boolean hasLetters() {
        return !letters.isEmpty();
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
     *   <li><b>Different word or phrase.</b> Unequal letter sequences, so no text is an anagram of
     *       itself. Because the comparison is on the <em>normalised</em> sequence, mere differences
     *       in case or punctuation do not make a new word: {@code "Listen"} and {@code "listen"} are
     *       the same word, as are {@code "dog"} and {@code "d o g"}.</li>
     * </ol>
     */
    public boolean isAnagramOf(NormalizedText other) {
        Objects.requireNonNull(other, "other");
        return signature.equals(other.signature) && !letters.equals(other.letters);
    }
}
