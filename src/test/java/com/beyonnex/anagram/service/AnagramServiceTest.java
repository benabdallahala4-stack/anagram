package com.beyonnex.anagram.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AnagramServiceTest {

    private AnagramService service;

    @BeforeEach
    void setUp() {
        service = new AnagramService();
    }

    /**
     * The example from the task statement, transcribed as-is: A, B and D are anagrams, C is not,
     * and feature 1 is invoked with f1(A,B), f1(A,C), f1(A,D).
     */
    @Nested
    @DisplayName("the worked example from the specification")
    class SpecificationExample {

        private static final String A = "listen";
        private static final String B = "silent";
        private static final String C = "hello";
        private static final String D = "enlist";

        @BeforeEach
        void invokeFeatureOne() {
            assertTrue(service.areAnagrams(A, B), "A and B are anagrams");
            assertFalse(service.areAnagrams(A, C), "C is not an anagram of A");
            assertTrue(service.areAnagrams(A, D), "A and D are anagrams");
        }

        @Test
        @DisplayName("f2(A) returns [B, D]")
        void f2OfA() {
            assertEquals(List.of(B, D), service.findAnagramsOf(A));
        }

        @Test
        @DisplayName("f2(B) returns [A, D]")
        void f2OfB() {
            assertEquals(List.of(A, D), service.findAnagramsOf(B));
        }

        @Test
        @DisplayName("f2(C) returns [] since C has no anagrams among the other inputs")
        void f2OfC() {
            assertEquals(List.of(), service.findAnagramsOf(C));
        }
    }

    @Test
    @DisplayName("both texts are recorded even when they are not anagrams")
    void recordsBothArgumentsRegardlessOfOutcome() {
        service.areAnagrams("listen", "hello");

        assertEquals(List.of("listen", "hello"), service.recordedTexts());
    }

    @Test
    @DisplayName("find does not add to the history")
    void queryingDoesNotRecord() {
        service.areAnagrams("listen", "silent");

        assertEquals(List.of("listen", "silent"), service.findAnagramsOf("enlist"));
        assertEquals(List.of("listen", "silent"), service.recordedTexts(), "the query was not recorded");

        // Had the query been recorded, this second identical query would now find itself too.
        assertEquals(List.of("listen", "silent"), service.findAnagramsOf("enlist"));
    }

    @Test
    @DisplayName("a text entered many times is reported once")
    void deduplicatesRepeatedEntries() {
        service.areAnagrams("listen", "silent");
        service.areAnagrams("listen", "enlist");
        service.areAnagrams("silent", "listen");

        assertEquals(List.of("silent", "enlist"), service.findAnagramsOf("listen"));
    }

    @Test
    @DisplayName("a text never entered can still be queried")
    void queryingAnUnknownTextStillSearchesHistory() {
        service.areAnagrams("listen", "silent");

        assertEquals(List.of("listen", "silent"), service.findAnagramsOf("enlist"));
    }

    @Test
    @DisplayName("results keep the spelling first entered, not the spelling queried")
    void resultsUseTheSpellingFirstEntered() {
        service.areAnagrams("Listen", "SILENT");

        assertEquals(List.of("Listen", "SILENT"), service.findAnagramsOf("enlist"));
    }

    @Test
    @DisplayName("a text with no letters is rejected rather than recorded")
    void rejectsTextWithoutLetters() {
        assertThrows(IllegalArgumentException.class, () -> service.areAnagrams("listen", "!!!"));
        assertThrows(IllegalArgumentException.class, () -> service.findAnagramsOf("   "));

        // The rejected call must not have recorded its valid first argument either.
        assertEquals(List.of(), service.recordedTexts());
    }
}
