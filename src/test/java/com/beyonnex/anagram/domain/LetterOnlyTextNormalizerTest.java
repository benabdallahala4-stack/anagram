package com.beyonnex.anagram.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LetterOnlyTextNormalizerTest {

    private final TextNormalizer normalizer = new LetterOnlyTextNormalizer();

    @Test
    @DisplayName("keeps letters, folds case, drops everything else")
    void reducesTextToItsLetters() {
        assertEquals("newyorktimes", normalizer.normalize("New York Times!").letters());
        assertEquals("nagaram", normalizer.normalize("Nag a ram").letters());
        assertEquals("abc", normalizer.normalize("a-b-c 123").letters());
    }

    @Test
    @DisplayName("the text as typed is preserved for display")
    void keepsTheRawText() {
        String raw = "  New York Times!  ";
        assertEquals(raw, normalizer.normalize(raw).raw());
    }

    @Test
    @DisplayName("the signature is the letters in sorted order")
    void signatureSortsTheLetters() {
        assertEquals(new AnagramSignature("eilnst"), normalizer.normalize("Listen!").signature());
        assertEquals(
                normalizer.normalize("silent").signature(),
                normalizer.normalize("listen").signature());
    }

    @Test
    @DisplayName("a text with no letters normalizes to nothing")
    void reportsTextWithoutLetters() {
        assertFalse(normalizer.normalize("").hasLetters());
        assertFalse(normalizer.normalize(" 42 !!! ").hasLetters());
        assertTrue(normalizer.normalize("a").hasLetters());
    }

    @Test
    void rejectsNull() {
        assertThrows(NullPointerException.class, () -> normalizer.normalize(null));
    }
}
