package com.beyonnex.anagram.store;

import com.beyonnex.anagram.domain.NormalizedText;
import java.util.List;

/** Texts entered so far. An interface so a persistent store could replace the in-memory one. */
public interface AnagramHistory {

    /** Adding a text that is already known is a no-op. */
    void add(NormalizedText text);

    /** Anagrams of {@code query} in insertion order, never including the query's own word. */
    List<NormalizedText> findAnagramsOf(NormalizedText query);

    /** Every distinct text, in insertion order. */
    List<NormalizedText> all();
}
