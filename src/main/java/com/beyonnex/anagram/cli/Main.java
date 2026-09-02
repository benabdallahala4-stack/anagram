package com.beyonnex.anagram.cli;

import com.beyonnex.anagram.service.AnagramService;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        // Explicit UTF-8 so accented input survives whatever the platform default is.
        BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8));
        PrintWriter out = new PrintWriter(
                new OutputStreamWriter(System.out, StandardCharsets.UTF_8));

        new AnagramCli(new AnagramService(), in, out).run();
        out.flush();
    }
}
