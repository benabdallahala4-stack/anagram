package com.beyonnex.anagram;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AnagramHistoryTest {

    private final AnagramHistory history = new AnagramHistory();

    private void add(String text) {
        history.add(TextNormalizer.normalize(text));
    }

    private List<String> findAnagramsOf(String text) {
        return history.findAnagramsOf(TextNormalizer.normalize(text)).stream()
                .map(NormalizedText::raw)
                .toList();
    }

    @Test
    @DisplayName("returns the anagrams of a text, excluding the text itself")
    void returnsTheAnagramFamilyWithoutTheQuery() {
        add("listen");
        add("silent");
        add("enlist");

        assertEquals(List.of("silent", "enlist"), findAnagramsOf("listen"));
    }

    @Test
    @DisplayName("results follow the order texts were first recorded")
    void preservesInsertionOrder() {
        add("enlist");
        add("silent");
        add("tinsel");

        assertEquals(List.of("enlist", "silent", "tinsel"), findAnagramsOf("listen"));
    }

    @Test
    @DisplayName("re-recording a text, in any casing, does not duplicate it")
    void deduplicatesOnTheNormalizedForm() {
        add("listen");
        add("LISTEN");
        add("  listen!  ");
        add("silent");

        assertEquals(List.of("listen"), findAnagramsOf("silent"), "the first spelling wins");
        assertEquals(2, history.all().size());
    }

    @Test
    @DisplayName("an unknown set of letters yields nothing")
    void returnsEmptyForAnUnseenSignature() {
        add("listen");

        assertEquals(List.of(), findAnagramsOf("hello"));
    }

    @Test
    @DisplayName("a text whose only stored match is itself yields nothing")
    void excludesTheQueryEvenAsTheSoleEntry() {
        add("listen");

        assertEquals(List.of(), findAnagramsOf("listen"));
    }

    @Test
    @DisplayName("all() lists every distinct text in arrival order")
    void listsEverythingInOrder() {
        add("listen");
        add("hello");
        add("silent");
        add("hello");

        assertEquals(
                List.of("listen", "hello", "silent"),
                history.all().stream().map(NormalizedText::raw).toList());
    }
}
