package com.beyonnex.anagram.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.beyonnex.anagram.domain.LetterOnlyTextNormalizer;
import com.beyonnex.anagram.domain.NormalizedText;
import com.beyonnex.anagram.domain.TextNormalizer;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InMemoryAnagramHistoryTest {

    private final TextNormalizer normalizer = new LetterOnlyTextNormalizer();
    private final AnagramHistory history = new InMemoryAnagramHistory();

    private void add(String text) {
        history.add(normalizer.normalize(text));
    }

    private List<String> findAnagramsOf(String text) {
        return history.findAnagramsOf(normalizer.normalize(text)).stream()
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
    void deduplicatesOnTheNormalisedForm() {
        add("listen");
        add("LISTEN");
        add("  listen!  ");
        add("silent");

        assertEquals(1, findAnagramsOf("silent").size());
        assertEquals(List.of("listen"), findAnagramsOf("silent"), "the first spelling wins");
        assertEquals(2, history.size());
    }

    @Test
    @DisplayName("an unknown set of letters yields nothing")
    void returnsEmptyForAnUnseenSignature() {
        add("listen");

        assertEquals(List.of(), findAnagramsOf("hello"));
        assertEquals(List.of(), new InMemoryAnagramHistory().findAnagramsOf(normalizer.normalize("x")));
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

    @Test
    @DisplayName("concurrent writers neither lose nor duplicate entries")
    void isSafeUnderConcurrentUse() throws InterruptedException {
        int threads = 8;
        int wordsPerThread = 250;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);

        try {
            for (int t = 0; t < threads; t++) {
                pool.execute(() -> {
                    try {
                        start.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    // Every thread writes the same words, so the store must collapse them to one
                    // entry each rather than racing into duplicates or lost updates.
                    for (int i = 0; i < wordsPerThread; i++) {
                        String word = "word" + alphabeticSuffix(i);
                        add(word);
                        add(new StringBuilder(word).reverse().toString());
                    }
                });
            }
            start.countDown();
            pool.shutdown();
            assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS), "workers finished");
        } finally {
            pool.shutdownNow();
        }

        assertEquals(2 * wordsPerThread, history.size(), "each distinct word stored exactly once");
        assertEquals(
                2 * wordsPerThread,
                history.all().stream().map(NormalizedText::letters).collect(Collectors.toSet()).size(),
                "no duplicates leaked into the listing");
    }

    /**
     * A distinct alphabetic suffix per index. Digits would not do: the normaliser strips them, so
     * "word1" and "word2" are the same word and the store would rightly collapse them into one.
     */
    private static String alphabeticSuffix(int index) {
        StringBuilder suffix = new StringBuilder();
        int remaining = index;
        do {
            suffix.append((char) ('a' + remaining % 26));
            remaining /= 26;
        } while (remaining > 0);
        return suffix.toString();
    }
}
