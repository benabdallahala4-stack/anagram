package com.beyonnex.anagram.cli;

import com.beyonnex.anagram.service.AnagramService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * The interactive loop, written against a {@link BufferedReader}/{@link PrintWriter} pair rather
 * than {@code System.in}/{@code System.out} so that it can be driven end to end from a test.
 *
 * <p>Deliberately thin: it parses commands, delegates to {@link AnagramService}, and formats the
 * answer. No rule about anagrams lives here.
 *
 * <p>Texts may contain spaces ("New York Times"), so a bare command word prompts for each text on
 * its own line, which is unambiguous for any input. A {@code |}-separated one-liner is also accepted
 * because it makes the program easy to script and to pipe input into.
 */
public final class AnagramCli {

    private static final String SEPARATOR = "|";

    private final AnagramService service;
    private final BufferedReader in;
    private final PrintWriter out;

    public AnagramCli(AnagramService service, BufferedReader in, PrintWriter out) {
        this.service = Objects.requireNonNull(service, "service");
        this.in = Objects.requireNonNull(in, "in");
        this.out = Objects.requireNonNull(out, "out");
    }

    /** Runs until the user quits or input ends. */
    public void run() {
        printBanner();

        boolean running = true;
        while (running) {
            String line = prompt("> ");
            if (line == null) {
                // End of input: Ctrl-D, or a pipe running dry.
                out.println();
                break;
            }

            String input = line.trim();
            if (input.isEmpty()) {
                continue;
            }

            int split = indexOfFirstSpace(input);
            String command = (split < 0 ? input : input.substring(0, split)).toLowerCase(Locale.ROOT);
            String argument = split < 0 ? "" : input.substring(split + 1).trim();

            switch (command) {
                case "check", "1" -> check(argument);
                case "find", "2" -> find(argument);
                case "history" -> printHistory();
                case "help", "?" -> printHelp();
                case "quit", "exit" -> {
                    out.println("Bye.");
                    running = false;
                }
                default -> out.println(
                        "Unknown command: '" + command + "'. Type 'help' to see the commands.");
            }
            // One flush per command handled, rather than one at the end of every branch. The only
            // other place output has to be pushed is prompt(), which does not end with a newline.
            out.flush();
        }
        out.flush();
    }

    /** Feature #1. Accepts {@code check A | B}, or prompts for each text when given no argument. */
    private void check(String argument) {
        String left;
        String right;

        if (argument.isEmpty()) {
            left = prompt("  first text : ");
            if (left == null) {
                return;
            }
            right = prompt("  second text: ");
            if (right == null) {
                return;
            }
        } else {
            int separator = argument.indexOf(SEPARATOR);
            if (separator < 0) {
                out.println("  Two texts are needed. Use 'check <first> | <second>', "
                        + "or just 'check' to be prompted for each.");
                return;
            }
            left = argument.substring(0, separator).trim();
            right = argument.substring(separator + SEPARATOR.length()).trim();
        }

        try {
            boolean anagrams = service.areAnagrams(left, right);
            out.println("  " + quote(left) + " and " + quote(right)
                    + (anagrams ? " ARE anagrams." : " are NOT anagrams."));
        } catch (IllegalArgumentException e) {
            out.println("  " + e.getMessage());
        }
    }

    /** Feature #2. Accepts {@code find A}, or prompts for the text when given no argument. */
    private void find(String argument) {
        String text = argument;
        if (text.isEmpty()) {
            text = prompt("  text: ");
            if (text == null) {
                return;
            }
        }

        try {
            List<String> anagrams = service.findAnagramsOf(text);
            if (anagrams.isEmpty()) {
                out.println("  No previously entered text is an anagram of " + quote(text) + ".");
            } else {
                out.println("  " + anagrams.size() + " anagram(s) of " + quote(text) + " entered so far:");
                for (String anagram : anagrams) {
                    out.println("    - " + anagram);
                }
            }
        } catch (IllegalArgumentException e) {
            out.println("  " + e.getMessage());
        }
    }

    private void printHistory() {
        List<String> texts = service.recordedTexts();
        if (texts.isEmpty()) {
            out.println("  Nothing entered yet.");
        } else {
            out.println("  " + texts.size() + " text(s) entered so far:");
            for (String text : texts) {
                out.println("    - " + text);
            }
        }
    }

    private void printBanner() {
        out.println("Anagram tool. Type 'help' for the commands, 'quit' to leave.");
    }

    private void printHelp() {
        out.println("""
                  check                     compare two texts, prompting for each (feature #1)
                  check <first> | <second>  the same, on one line
                  find <text>               list previously entered anagrams of a text (feature #2)
                  history                   list every text entered so far
                  help                      show this
                  quit                      leave

                Texts entered via 'check' are remembered for 'find', whether or not they matched.
                Asking 'find' does not itself record anything.""");
    }

    /** Writes a prompt and reads one line. @return the line, or null when input has ended */
    private String prompt(String label) {
        out.print(label);
        out.flush();
        try {
            return in.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read from input", e);
        }
    }

    private static int indexOfFirstSpace(String input) {
        for (int i = 0; i < input.length(); i++) {
            if (Character.isWhitespace(input.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private static String quote(String text) {
        return "\"" + text + "\"";
    }
}
