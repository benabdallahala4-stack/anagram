package com.beyonnex.anagram.service;

import com.beyonnex.anagram.domain.AnagramChecker;
import com.beyonnex.anagram.domain.NormalizedText;
import com.beyonnex.anagram.store.AnagramHistory;
import com.beyonnex.anagram.store.InMemoryAnagramHistory;
import java.util.List;
import java.util.Objects;

/** The two features. No I/O in here, so the CLI can be swapped for something else. */
public final class AnagramService {

    private final AnagramChecker checker;
    private final AnagramHistory history;

    public AnagramService() {
        this(new AnagramChecker(), new InMemoryAnagramHistory());
    }

    public AnagramService(AnagramChecker checker, AnagramHistory history) {
        this.checker = Objects.requireNonNull(checker, "checker");
        this.history = Objects.requireNonNull(history, "history");
    }

    /**
     * Feature 1. Both texts are recorded whether or not they match: the task's example expects
     * C to be in the history after f1(A, C) returned false.
     *
     * @throws IllegalArgumentException if either text has no letters
     */
    public boolean areAnagrams(String left, String right) {
        NormalizedText first = checker.normalize(left);
        NormalizedText second = checker.normalize(right);

        history.add(first);
        history.add(second);

        return first.isAnagramOf(second);
    }

    /**
     * Feature 2. Read-only: the history is made of feature 1 inputs, so querying adds nothing.
     *
     * @return matching texts as first entered, in the order first entered
     * @throws IllegalArgumentException if the text has no letters
     */
    public List<String> findAnagramsOf(String text) {
        NormalizedText query = checker.normalize(text);
        return history.findAnagramsOf(query).stream().map(NormalizedText::raw).toList();
    }

    /** Everything entered so far, for the CLI's history command. */
    public List<String> recordedTexts() {
        return history.all().stream().map(NormalizedText::raw).toList();
    }
}
