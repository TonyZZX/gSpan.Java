package io.github.tonyzzx.gspan;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        int minSup;
        int maxPat = Integer.MAX_VALUE;
        int minNodes = 0;

        Scanner sc = new Scanner(System.in);
        System.out.println("Input file name");
        String filepath = sc.nextLine();
        System.out.println("Input support");
        minSup = sc.nextInt();
        File readFile = new File(filepath);
        File writeFile = new File(readFile.getName() + "_result");
        FileReader reader = new FileReader(readFile);
        FileWriter writer = new FileWriter(writeFile);
        writer.flush();

        gSpan gSpan = new gSpan();
        gSpan.run(reader, writer, minSup, maxPat, minNodes, false);

        sc.close();
    }
}
