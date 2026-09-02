package com.beyonnex.anagram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Texts entered so far, in two maps: {@code byLetters} dedupes and keeps insertion order,
 * {@code buckets} groups texts by signature so finding anagrams is one lookup rather than a
 * scan of everything entered.
 */
public final class AnagramHistory {

    private final Map<String, NormalizedText> byLetters = new LinkedHashMap<>();
    private final Map<String, List<NormalizedText>> buckets = new HashMap<>();

    /** Adding a text that is already known (in any casing) is a no-op. */
    public void add(NormalizedText text) {
        if (byLetters.putIfAbsent(text.letters(), text) == null) {
            buckets.computeIfAbsent(text.signature(), key -> new ArrayList<>()).add(text);
        }
    }

    /** Anagrams of {@code query} in insertion order, never including the query's own word. */
    public List<NormalizedText> findAnagramsOf(NormalizedText query) {
        // Same bucket means same letters; only the query's own word is left out.
        return buckets.getOrDefault(query.signature(), List.of()).stream()
                .filter(candidate -> !query.isSameWordAs(candidate))
                .toList();
    }

    /** Every distinct text, in insertion order. */
    public List<NormalizedText> all() {
        return List.copyOf(byLetters.values());
    }
}
