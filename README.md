# Anagram tool

[![build](https://github.com/benabdallahala4-stack/anagram/actions/workflows/build.yml/badge.svg)](https://github.com/benabdallahala4-stack/anagram/actions/workflows/build.yml)

An interactive command-line program with two features:

1. **Check** whether two texts are anagrams of each other.
2. **Find** every previously entered text that is an anagram of a given text.

Java 17, Gradle, no runtime dependencies. History is kept in memory for the duration of one run.

## Running it

```bash
./gradlew run          # interactive
./gradlew test         # 47 tests
```

Or build a jar and run that:

```bash
./gradlew jar
java -jar build/libs/anagram-1.0.0.jar
```

### Commands

| Command | Does |
| --- | --- |
| `check` | Feature #1, prompting for each text on its own line |
| `check <first> \| <second>` | Feature #1 on one line |
| `find <text>` | Feature #2 |
| `history` | Every text entered so far |
| `help` / `quit` | |

Texts may contain spaces, so the prompted form of `check` is the unambiguous one; the `|` form
exists to make the program easy to script and pipe into.

### A session

```
> check listen | silent
  "listen" and "silent" ARE anagrams.
> check listen | hello
  "listen" and "hello" are NOT anagrams.
> check listen | enlist
  "listen" and "enlist" ARE anagrams.
> find listen
  2 anagram(s) of "listen" entered so far:
    - silent
    - enlist
> find hello
  No previously entered text is an anagram of "hello".
```

That is the example from the task statement: with `f1(A,B)`, `f1(A,C)`, `f1(A,D)` where A, B and D
are anagrams, `f2(A)` gives `[B, D]`, `f2(B)` gives `[A, D]`, and `f2(C)` gives `[]`. It is pinned
down as a test in `AnagramServiceTest.SpecificationExample`.

## What counts as an anagram

Wikipedia defines an anagram as a word or phrase formed by rearranging the letters of **a different**
word or phrase, **using all the original letters exactly once**. Two rules follow, and both live in
`NormalizedText.isAnagramOf`:

**1. The same letters, each used as often.** Only letters count, and case is irrelevant — Wikipedia's
own examples require this, since `anagram` → `nag a ram` gains a space and `New York Times` →
`monkeys write` changes case. So every text is reduced to its letters, lowercased, and compared as a
multiset.

**2. A *different* word or phrase.** This is the rule that is easy to miss, and the task's example
confirms it: A is entered three times, yet `f2(A)` returns `[B, D]` rather than `[A, B, D]`. So
`f1(X, X)` is false, and a text is never returned among its own anagrams.

"Different" is judged on the *normalised* letters, not the raw text. Otherwise `Listen` and `listen`
would count as an anagram pair, which is plainly wrong — they are one word typed two ways. The same
reasoning makes `dog` and `d o g` the same word.

### Decisions this leaves open, and how they went

| Case | Behaviour | Why |
| --- | --- | --- |
| `abc1` vs `abc2` | Not anagrams | Digits are not letters, so both are the word `abc` — the same word twice |
| `café` vs `face` | Not anagrams | Accented letters are distinct letters in the languages that use them |
| `café` composed vs decomposed | The same word | Text is NFC-normalised first, so both Unicode spellings of `é` agree |
| `""`, `"  "`, `"!!!"` | Rejected | A text with no letters is not a word or phrase; without this rule they would all land in one bucket and be reported as mutual non-anagrams |
| `LISTEN` after `listen` | One entry, displayed as `listen` | The same word; the first spelling seen is kept |

A **known limitation**: case folding uses `String.toLowerCase(Locale.ROOT)`, which does not apply
full Unicode case folding, so German `ß` and `SS` are treated as different letters. Fixing it means
full case folding (via ICU4J, or a hand-rolled mapping); it was left out rather than pulling in a
dependency for one letter. Everything else Unicode-related is handled: the default locale is never
used (under a Turkish locale `"I".toLowerCase()` is a dotless `ı`, which would make results depend on
where the program runs), and text is walked by code point so that letters outside the Basic
Multilingual Plane count as one letter rather than two surrogate halves.

## Design

```
domain/   AnagramSignature, NormalizedText, TextNormalizer,
          LetterOnlyTextNormalizer, AnagramChecker    pure rules, no I/O
store/    AnagramHistory + InMemoryAnagramHistory     the texts seen so far
service/  AnagramService                              the two features
cli/      AnagramCli, Main                            parsing and formatting only
```

The dependencies point inwards: the domain knows nothing about storage, and neither knows anything
about the console. That is what lets `AnagramCli` be tested end to end against a `StringReader`
rather than a terminal, and what would let a REST layer replace the CLI without touching a rule.

**Feature #2 does not scan the history.** Each text is filed under a signature — its letters, sorted —
so the candidate anagrams of a query are exactly the contents of one bucket:

```
"listen" ─┐
"silent" ─┼─► eilnst ─► [listen, silent, enlist]
"enlist" ─┘
"hello"  ───► ehllo  ─► [hello]
```

A lookup costs **O(m log m)** on the length of the query alone and does not slow down as the history
grows; comparing the query against every stored text would cost O(n·m). Buckets are insertion-ordered,
so results are deterministic rather than dependent on hash iteration order — which is why the two
orderings in the task's example come out exactly as written.

Two other choices worth naming:

- **Feature #1 records both texts whatever the answer.** The task's example depends on it: `C` is
  available to feature #2 even though `f1(A, C)` was false. Recording happens only after both texts
  validate, so a rejected call leaves nothing behind.
- **Feature #2 records nothing.** The task scopes the history to "all past inputs from feature #1",
  so asking a question does not change the answer to the next one.

`AnagramHistory` is an interface because "no need to persist across executions" is a requirement of
today, not a property of the problem. `InMemoryAnagramHistory` is thread-safe behind a read/write
lock — a CLI never contends, but a store is exactly the component that later ends up behind a request
handler.

## Tests

47 tests covering the task's worked example verbatim, the self-anagram rule, case and punctuation
insensitivity, Unicode composition, supplementary-plane letters, locale independence, input
rejection, store ordering and deduplication, concurrent writers, and a full CLI session driven
end to end.

```bash
./gradlew test
```
