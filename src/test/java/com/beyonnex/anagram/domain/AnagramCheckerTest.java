package com.beyonnex.anagram.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AnagramCheckerTest {

    /** "café" with e-acute as a single code point, U+00E9. */
    private static final String CAFE_COMPOSED = "caf\u00e9";

    /** The same word with e-acute spelled as "e" plus a combining acute accent, U+0301. */
    private static final String CAFE_DECOMPOSED = "cafe\u0301";

    private final AnagramChecker checker = new AnagramChecker();

    /**
     * Exactly what {@code AnagramService} does for feature #1 minus the recording: normalise both
     * texts, then apply the rule. Asserting through the same path production takes means these cases
     * cannot pass against a shortcut that only the tests use.
     */
    private boolean areAnagrams(String left, String right) {
        return checker.normalize(left).isAnagramOf(checker.normalize(right));
    }

    @ParameterizedTest(name = "\"{0}\" and \"{1}\" are anagrams")
    @CsvSource({
        "listen, silent",
        "anagram, nag a ram",              // Wikipedia's example for the word itself
        "New York Times, monkeys write",   // only works if case AND spaces are ignored
        "Madam Curie, Radium came",
        "William Shakespeare, I am a weakish speller",
        "Dormitory, Dirty room",
    })
    void recognisesAnagrams(String left, String right) {
        assertTrue(areAnagrams(left, right));
        assertTrue(areAnagrams(right, left), "the relation is symmetric");
    }

    @ParameterizedTest(name = "\"{0}\" and \"{1}\" are not anagrams")
    @CsvSource({
        "listen, hello",     // different letters entirely
        "aab, abb",          // same letters, different counts: not "each exactly once"
        "listen, listens",   // a superset is not a rearrangement
        "silent, silen",     // nor is a subset
    })
    void rejectsNonAnagrams(String left, String right) {
        assertFalse(areAnagrams(left, right));
        assertFalse(areAnagrams(right, left), "the relation is symmetric");
    }

    @Test
    @DisplayName("a text is never an anagram of itself: Wikipedia requires a DIFFERENT word")
    void aTextIsNotAnAnagramOfItself() {
        assertFalse(areAnagrams("listen", "listen"));
    }

    @Test
    @DisplayName("restyling a text does not make a different word")
    void differencesInCaseOrPunctuationDoNotMakeANewWord() {
        assertFalse(areAnagrams("Listen", "listen"), "case alone");
        assertFalse(areAnagrams("dog", "d o g"), "spacing alone");
        assertFalse(areAnagrams("dog!", "...dog"), "punctuation alone");
        assertFalse(areAnagrams("New York Times", "newyorktimes"), "both at once");
    }

    @Test
    @DisplayName("digits are not letters, so they are ignored")
    void digitsAreIgnored() {
        assertFalse(areAnagrams("abc1", "abc2"), "both reduce to the same word, abc");
        assertTrue(areAnagrams("abc1", "cba2"));
    }

    @Test
    @DisplayName("diacritics are significant letters")
    void diacriticsAreSignificant() {
        assertFalse(areAnagrams(CAFE_COMPOSED, "face"), "e-acute is not e");
        assertTrue(areAnagrams(CAFE_COMPOSED, "fac\u00e9"));
    }

    @Test
    @DisplayName("the same word spelled composed or decomposed reduces to the same letters")
    void unicodeCompositionDoesNotChangeTheAnswer() {
        assertEquals(
                checker.normalize(CAFE_COMPOSED).signature(),
                checker.normalize(CAFE_DECOMPOSED).signature(),
                "both spellings must reduce to the same letters");
        assertFalse(areAnagrams(CAFE_COMPOSED, CAFE_DECOMPOSED), "they are the same word");
        assertTrue(areAnagrams(CAFE_DECOMPOSED, "fac\u00e9"));
    }

    @Test
    @DisplayName("letters outside the Basic Multilingual Plane count as one letter, not two halves")
    void handlesSupplementaryPlaneLetters() {
        // Deseret capital long I and long E: each is one letter that Java stores as a surrogate pair.
        String first = new String(new int[] {0x10400, 0x10401}, 0, 2);
        String second = new String(new int[] {0x10401, 0x10400}, 0, 2);

        assertTrue(areAnagrams(first, second));

        String letters = checker.normalize(first).letters();
        assertEquals(2, letters.codePointCount(0, letters.length()), "two letters, not four chars");
    }

    @Test
    @DisplayName("the answer does not depend on the machine's default locale")
    void isIndependentOfDefaultLocale() {
        Locale original = Locale.getDefault();
        try {
            // Under a Turkish locale, "I".toLowerCase() is a dotless "ı"; a normaliser that used the
            // default locale would silently answer differently here than everywhere else.
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertTrue(areAnagrams("LISTEN", "silent"));
            assertFalse(areAnagrams("LISTEN", "listen"));
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    @DisplayName("texts with no letters are rejected")
    void rejectsTextsWithoutLetters() {
        assertThrows(IllegalArgumentException.class, () -> checker.normalize(""));
        assertThrows(IllegalArgumentException.class, () -> checker.normalize("   "));
        assertThrows(IllegalArgumentException.class, () -> checker.normalize("123 !!!"));
        assertThrows(NullPointerException.class, () -> checker.normalize(null));
    }
}
