# Anagram tool

[![build](https://github.com/benabdallahala4-stack/anagram/actions/workflows/build.yml/badge.svg)](https://github.com/benabdallahala4-stack/anagram/actions/workflows/build.yml)

Small interactive CLI with two features:

1. **check**: are two texts anagrams of each other?
2. **find**: which previously entered texts are anagrams of a given text?

Java 17, Gradle, no runtime dependencies. History is kept in memory for one run.

## Running it

You need a JDK 17 or newer on the `PATH` (`java -version` to check). The Gradle wrapper fetches
Gradle itself, so there is nothing else to install.

**Linux / macOS**

```bash
./gradlew run          # builds, then starts the interactive prompt
./gradlew test
```

**Windows** (PowerShell or cmd)

```
gradlew.bat run
gradlew.bat test
```

Gradle draws its progress bar over the prompt while the program waits for input; it is still
working. Add `--console=plain` to hide the bar, or run the jar instead.

**As a jar** (any OS). Build it once, or download it from the
[latest release](https://github.com/benabdallahala4-stack/anagram/releases/latest) and skip the build:

```bash
./gradlew jar          # gradlew.bat jar on Windows
java -jar build/libs/anagram-1.0.0.jar
```

**With Docker**, if there is no JDK around:

```bash
docker build -t anagram .
docker run --rm -it anagram
```

`-i` matters: the program reads stdin, so without it there is nothing to read and it exits at once.

On an older Windows console, run `chcp 65001` first if you want to type accented characters; the
program itself always reads and writes UTF-8.

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

Five classes in one package:

```
NormalizedText   a text plus its letters; the anagram rule lives here
TextNormalizer   raw text -> NormalizedText; rejects texts with no letters
AnagramHistory   what was entered, indexed by signature
AnagramService   the two features, no I/O
AnagramCli       the command loop and main()
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

`AnagramCli` takes a reader and a writer rather than using `System.in`/`out` directly, which is
what lets a test drive a whole session.

## Tests

44, covering the task's example, the "different word" rule, case/punctuation, Unicode
composition, locale independence, input rejection, history ordering and deduplication, and a
full CLI session.
