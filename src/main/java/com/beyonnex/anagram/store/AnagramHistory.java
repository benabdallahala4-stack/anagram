package com.beyonnex.anagram.store;

import com.beyonnex.anagram.domain.NormalizedText;
import java.util.List;

/**
 * The texts seen so far, indexed so that anagrams can be retrieved without scanning.
 *
 * <p>An interface rather than a concrete class because the task's "no need to persist across
 * executions" is a requirement of today, not a property of the problem: a database- or
 * Redis-backed implementation can be dropped in without the service noticing.
 */
public interface AnagramHistory {

    /**
     * Records a text. Recording a text that is already known is a no-op, so a text entered many
     * times is still reported once.
     */
    void add(NormalizedText text);

    /**
     * @return every recorded text that is an anagram of {@code query}, in the order the texts were
     *         first recorded, excluding {@code query}'s own word or phrase
     */
    List<NormalizedText> findAnagramsOf(NormalizedText query);

    /** @return every recorded text, in the order first recorded */
    List<NormalizedText> all();
}
