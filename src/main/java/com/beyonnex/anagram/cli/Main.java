package com.beyonnex.anagram.cli;

import com.beyonnex.anagram.service.AnagramService;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/** Entry point: wires standard input and output into the CLI. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        // UTF-8 explicitly, rather than the platform default, so accented and non-Latin text
        // survives the round trip on every OS.
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8));

        new AnagramCli(new AnagramService(), in, out).run();
        out.flush();
    }
}
