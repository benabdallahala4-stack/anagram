package com.beyonnex.anagram.service;

import com.beyonnex.anagram.domain.AnagramChecker;
import com.beyonnex.anagram.domain.NormalizedText;
import com.beyonnex.anagram.store.AnagramHistory;
import com.beyonnex.anagram.store.InMemoryAnagramHistory;
import java.util.List;
import java.util.Objects;

/**
 * The two features, free of any I/O so that the user interface stays replaceable.
 */
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
     * Feature #1 &mdash; are these two texts anagrams of each other?
     *
     * <p>Both texts are recorded whatever the answer, which is what makes the task's worked example
     * come out right: {@code C} is offered to feature #2 even though {@code f1(A, C)} was false.
     * Recording happens only after both texts validate, so a rejected call leaves no trace.
     *
     * @throws IllegalArgumentException if either text contains no letters
     */
    public boolean areAnagrams(String left, String right) {
        NormalizedText first = checker.normalize(left);
        NormalizedText second = checker.normalize(right);

        history.add(first);
        history.add(second);

        return first.isAnagramOf(second);
    }

    /**
     * Feature #2 &mdash; every previously entered text that is an anagram of {@code text}.
     *
     * <p>A pure query: the task scopes the history to "all past inputs from feature #1", so asking a
     * question does not itself add to it. The query is therefore never in its own results, and
     * neither is any text that is merely a restyling of it, such as a different capitalisation.
     *
     * @return the matching texts as they were first entered, in the order they were first entered;
     *         empty if there are none
     * @throws IllegalArgumentException if the text contains no letters
     */
    public List<String> findAnagramsOf(String text) {
        NormalizedText query = checker.normalize(text);
        return history.findAnagramsOf(query).stream().map(NormalizedText::raw).toList();
    }

    /** Every distinct text recorded so far, in arrival order. Supports the CLI's history command. */
    public List<String> recordedTexts() {
        return history.all().stream().map(NormalizedText::raw).toList();
    }
}
