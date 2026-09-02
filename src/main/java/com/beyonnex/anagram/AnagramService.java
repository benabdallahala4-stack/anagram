package com.beyonnex.anagram;

import java.util.List;

/** The two features. No I/O in here, so the CLI could be swapped for something else. */
public final class AnagramService {

    private final AnagramHistory history = new AnagramHistory();

    /**
     * Feature 1. Both texts are recorded whether or not they match: the task's example expects
     * C to be in the history after f1(A, C) returned false.
     *
     * @throws IllegalArgumentException if either text has no letters
     */
    public boolean areAnagrams(String left, String right) {
        NormalizedText first = TextNormalizer.normalize(left);
        NormalizedText second = TextNormalizer.normalize(right);

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
        NormalizedText query = TextNormalizer.normalize(text);
        return history.findAnagramsOf(query).stream().map(NormalizedText::raw).toList();
    }

    /** Everything entered so far, for the CLI's history command. */
    public List<String> recordedTexts() {
        return history.all().stream().map(NormalizedText::raw).toList();
    }
}
