package com.beyonnex.anagram.domain;

/** Reduces raw text to the letters that count when comparing anagrams. */
@FunctionalInterface
public interface TextNormalizer {

    NormalizedText normalize(String raw);
}
