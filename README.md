# Anagram tool

[![build](https://github.com/benabdallahala4-stack/anagram/actions/workflows/build.yml/badge.svg)](https://github.com/benabdallahala4-stack/anagram/actions/workflows/build.yml)

Small interactive CLI with two features:

1. **check**: are two texts anagrams of each other?
2. **find**: which previously entered texts are anagrams of a given text?

Java 17, Gradle, no runtime dependencies. History is kept in memory for one run.

## Running it

```bash
./gradlew run          # interactive
./gradlew test
```

Or with a jar (also attached to the [latest release](https://github.com/benabdallahala4-stack/anagram/releases/latest)):

```bash
./gradlew jar
java -jar build/libs/anagram-1.0.0.jar
```

Or with Docker, if there is no JDK around:

```bash
docker build -t anagram .
docker run --rm -it anagram
```

### Commands

| Command | What it does |
| --- | --- |
| `check` | feature 1, prompts for each text on its own line |
| `check <first> \| <second>` | same thing on one line |
| `find <text>` | feature 2 |
| `history` | everything entered so far |
| `help`, `quit` | |

Texts can contain spaces, so the prompted form of `check` is the safe one; the `|` form is for
scripting.

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

That is the example from the task (`f1(A,B)`, `f1(A,C)`, `f1(A,D)` → `f2(A)=[B,D]`, `f2(B)=[A,D]`,
`f2(C)=[]`). It is also a test, `AnagramServiceTest.SpecificationExample`.

## What counts as an anagram

Following the Wikipedia definition: a word or phrase formed by rearranging the letters of a
*different* word or phrase, using all the letters exactly once. Two rules, both in
`NormalizedText.isAnagramOf`:

1. **Same letters.** Only letters count and case is ignored (`anagram` / `nag a ram`,
   `New York Times` / `monkeys write`).
2. **Different word.** A text is not an anagram of itself. This is compared on the normalized
   letters, so `Listen` vs `listen` is the same word, not an anagram pair. The task's example
   relies on this: `f2(A)` is `[B, D]`, not `[A, B, D]`.

Some consequences:

| Input | Result | Why |
| --- | --- | --- |
| `abc1` vs `abc2` | not anagrams | digits are ignored, so it is `abc` twice |
| `café` vs `face` | not anagrams | accented letters are different letters |
| `café` typed two ways (NFC / NFD) | same word | input is NFC-normalized first |
| `""`, `"!!!"` | rejected | no letters, nothing to rearrange |
| `LISTEN` after `listen` | one entry, shown as `listen` | first spelling wins |

Known gap: `ß` vs `SS` are treated as different letters, because `toLowerCase` is case mapping,
not full case folding. Fixing that properly needs ICU4J; not worth the dependency here.

## Design

```
domain/   AnagramSignature, NormalizedText, TextNormalizer, AnagramChecker   the rules
store/    AnagramHistory, InMemoryAnagramHistory                            what was entered
service/  AnagramService                                                    the two features
cli/      AnagramCli, Main                                                  input/output only
```

Feature 2 does not scan the history. Every text is stored under its signature (letters, sorted),
so the anagrams of a query are exactly one bucket:

```
"listen" ─┐
"silent" ─┼─► eilnst ─► [listen, silent, enlist]
"enlist" ─┘
"hello"  ───► ehllo  ─► [hello]
```

Lookup cost depends on the length of the query, not on how much has been entered. Buckets keep
insertion order, so results are deterministic.

`AnagramHistory` is an interface so the in-memory store could be swapped for a persistent one.
The in-memory one is behind a read/write lock; not needed for a CLI, but cheap.

## Tests

47, covering the task's example, the "different word" rule, case/punctuation, Unicode
composition, locale independence, input rejection, store ordering and deduplication, concurrent
writes, and a full CLI session.
