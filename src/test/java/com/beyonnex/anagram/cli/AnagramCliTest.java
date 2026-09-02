package com.beyonnex.anagram.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.beyonnex.anagram.service.AnagramService;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AnagramCliTest {

    /** Drives a full session and returns everything the program printed. */
    private static String session(String... inputLines) {
        StringWriter captured = new StringWriter();
        BufferedReader in = new BufferedReader(new StringReader(String.join("\n", inputLines) + "\n"));
        PrintWriter out = new PrintWriter(captured);

        new AnagramCli(new AnagramService(), in, out).run();

        out.flush();
        return captured.toString();
    }

    @Test
    @DisplayName("the one-line form of feature #1 reports both outcomes")
    void checksTwoTextsOnOneLine() {
        String output = session("check listen | silent", "check listen | hello", "quit");

        assertTrue(output.contains("\"listen\" and \"silent\" ARE anagrams."), output);
        assertTrue(output.contains("\"listen\" and \"hello\" are NOT anagrams."), output);
        assertTrue(output.contains("Bye."), output);
    }

    @Test
    @DisplayName("the prompted form of feature #1 accepts texts containing spaces")
    void checksTwoTextsAcrossPrompts() {
        String output = session("check", "New York Times", "monkeys write", "quit");

        assertTrue(output.contains("\"New York Times\" and \"monkeys write\" ARE anagrams."), output);
    }

    @Test
    @DisplayName("feature #2 lists earlier entries, and reports when there are none")
    void findsAnagramsAmongEarlierEntries() {
        String output = session(
                "check listen | silent",
                "check listen | hello",
                "check listen | enlist",
                "find listen",
                "find hello",
                "quit");

        assertTrue(output.contains("2 anagram(s) of \"listen\" entered so far:"), output);
        assertTrue(output.contains("- silent"), output);
        assertTrue(output.contains("- enlist"), output);
        assertTrue(output.contains("No previously entered text is an anagram of \"hello\"."), output);
    }

    @Test
    @DisplayName("history lists what has been entered; querying adds nothing to it")
    void showsHistory() {
        String output = session("check listen | hello", "find silent", "history", "quit");

        assertTrue(output.contains("2 text(s) entered so far:"), output);
        assertFalse(output.contains("- silent"), "a query must not be recorded: " + output);
    }

    @Test
    @DisplayName("bad input is explained rather than crashing the session")
    void reportsInvalidInputAndKeepsGoing() {
        String output = session(
                "check listen",             // missing the second text
                "check listen | !!!",       // no letters
                "wibble",                   // unknown command
                "",                         // blank line, ignored
                "check listen | silent",    // still works afterwards
                "quit");

        assertTrue(output.contains("Two texts are needed."), output);
        assertTrue(output.contains("A text must contain at least one letter."), output);
        assertTrue(output.contains("Unknown command: 'wibble'"), output);
        assertTrue(output.contains("ARE anagrams."), output);
    }

    @Test
    @DisplayName("commands are case-insensitive and have numeric aliases")
    void acceptsAliases() {
        String output = session("CHECK listen | silent", "2 listen", "quit");

        assertTrue(output.contains("ARE anagrams."), output);
        assertTrue(output.contains("- silent"), output);
    }

    @Test
    @DisplayName("end of input ends the session cleanly")
    void endsOnEndOfInput() {
        String output = session("check listen | silent");

        assertTrue(output.contains("ARE anagrams."), output);
    }

    @Test
    @DisplayName("end of input part-way through a prompt does not hang or crash")
    void endsCleanlyMidPrompt() {
        String output = session("check", "listen");

        assertTrue(output.contains("second text"), output);
    }
}
