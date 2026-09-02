package com.beyonnex.anagram.store;

import com.beyonnex.anagram.domain.AnagramSignature;
import com.beyonnex.anagram.domain.NormalizedText;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * In-memory history holding two views of the same texts.
 *
 * <ul>
 *   <li>{@code byLetters} keyed on the normalised letter sequence: it deduplicates (re-entering
 *       {@code "listen"}, or entering {@code "LISTEN"}, does not create a second entry) and, being a
 *       {@link LinkedHashMap}, remembers the order texts first arrived.</li>
 *   <li>{@code buckets} keyed on {@link AnagramSignature}: every text built from a given set of
 *       letters, in arrival order.</li>
 * </ul>
 *
 * <p>The second view is the point. Candidate anagrams of a query are exactly one bucket, so a lookup
 * costs O(m log m) on the length of the query alone and does not slow down as the history grows;
 * comparing the query against every stored text would instead cost O(n·m). Insertion-ordered
 * structures throughout mean results are deterministic rather than dependent on hash order.
 *
 * <p>The first spelling of a word wins and is what gets echoed back to the user.
 *
 * <p>Thread-safe via a single read/write lock, so the read-heavy lookup path stays concurrent. A
 * single-run CLI never contends, but a store is exactly the component that later ends up behind a
 * request handler.
 */
public final class InMemoryAnagramHistory implements AnagramHistory {

    private final Map<String, NormalizedText> byLetters = new LinkedHashMap<>();
    private final Map<AnagramSignature, List<NormalizedText>> buckets = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void add(NormalizedText text) {
        Objects.requireNonNull(text, "text");
        lock.writeLock().lock();
        try {
            if (byLetters.putIfAbsent(text.letters(), text) == null) {
                buckets.computeIfAbsent(text.signature(), key -> new ArrayList<>()).add(text);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<NormalizedText> findAnagramsOf(NormalizedText query) {
        Objects.requireNonNull(query, "query");
        lock.readLock().lock();
        try {
            List<NormalizedText> family = buckets.get(query.signature());
            if (family == null) {
                return List.of();
            }
            List<NormalizedText> anagrams = new ArrayList<>(family.size());
            for (NormalizedText candidate : family) {
                // Sharing a bucket already proves "same letters", so this only has to enforce
                // "a different word or phrase" -- which is what keeps the query out of its own
                // results, whether or not it was ever entered itself.
                if (query.isAnagramOf(candidate)) {
                    anagrams.add(candidate);
                }
            }
            return List.copyOf(anagrams);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<NormalizedText> all() {
        lock.readLock().lock();
        try {
            return List.copyOf(byLetters.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int size() {
        lock.readLock().lock();
        try {
            return byLetters.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}
