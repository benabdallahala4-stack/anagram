package com.beyonnex.anagram.domain;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

/**
 * The normaliser that matches Wikipedia's definition: anagrams rearrange the <em>letters</em> of a
 * word or phrase, so everything that is not a letter is discarded and case is irrelevant.
 *
 * <p>Wikipedia's own examples require both rules: {@code "anagram"} &rarr; {@code "nag a ram"} only
 * works if spaces are ignored, and {@code "New York Times"} &rarr; {@code "monkeys write"} only
 * works if case is too.
 *
 * <p>The steps are ordered deliberately:
 *
 * <ol>
 *   <li><b>NFC first.</b> Unicode can spell "é" as one code point or as "e" plus a combining accent.
 *       Without composing first, the combining accent (which is a mark, not a letter) would be
 *       stripped and the two spellings would disagree.</li>
 *   <li><b>Case-fold with {@link Locale#ROOT}.</b> The default locale would be a latent bug: under a
 *       Turkish locale {@code "I".toLowerCase()} yields a dotless {@code "ı"}, so the same two texts
 *       would compare differently depending on where the program runs.</li>
 *   <li><b>NFC again.</b> Case mapping can itself produce decomposed output; {@code "İ"} lowercases
 *       to {@code "i"} plus a combining dot.</li>
 *   <li><b>Keep letters only,</b> walking code points so that supplementary-plane letters survive
 *       intact.</li>
 * </ol>
 *
 * <p>Diacritics are kept significant: {@code "café"} and {@code "face"} are not anagrams, because in
 * the languages that use them accented letters are distinct letters.
 *
 * <p>Stateless and therefore thread-safe.
 */
public final class LetterOnlyTextNormalizer implements TextNormalizer {

    @Override
    public NormalizedText normalize(String raw) {
        Objects.requireNonNull(raw, "raw");

        String composed = Normalizer.normalize(raw, Normalizer.Form.NFC);
        String folded = Normalizer.normalize(composed.toLowerCase(Locale.ROOT), Normalizer.Form.NFC);

        StringBuilder letters = new StringBuilder(folded.length());
        folded.codePoints().filter(Character::isLetter).forEach(letters::appendCodePoint);

        String normalized = letters.toString();
        return new NormalizedText(raw, normalized, AnagramSignature.of(normalized));
    }
}
