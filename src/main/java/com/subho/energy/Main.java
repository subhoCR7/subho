package com.subho.energy;

import com.subho.energy.parser.Nem12Parser;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java -jar nem12-parser.jar <inputFile> <outputFile>");
            System.exit(1);
        }

        try {
            new Nem12Parser().parse(args[0], args[1]);
            System.out.println("Processing completed successfully!");
        } catch (Exception e) {
            System.err.println("Processing failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
