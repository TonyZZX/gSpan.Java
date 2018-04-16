package io.github.tonyzzx.gspan;

import org.apache.commons.cli.*;

import java.io.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Arguments arguments = Arguments.Instance(args);

        File inFile = new File(arguments.inFilePath);
        File outFile = new File(arguments.outFilePath);
        try (FileReader reader = new FileReader(inFile)) {
            try (FileWriter writer = new FileWriter(outFile)) {
                gSpan gSpan = new gSpan();
                System.out.println("gSpan is mining...");
                gSpan.run(reader, writer, arguments.minSup, arguments.maxNodeNum, arguments.minNodeNum);
            }
        }
    }

    private static class Arguments {
        private static Arguments arguments;

        private String[] args;

        String inFilePath;
        long minSup;
        long minNodeNum = 0;
        long maxNodeNum = Long.MAX_VALUE;
        String outFilePath;

        private Arguments(String[] args) {
            this.args = args;
        }

        static Arguments Instance(String[] args) {
            arguments = new Arguments(args);
            if (args.length > 0) {
                arguments.InitFromCmd();
            } else {
                arguments.InitFromRun();
            }
            return arguments;
        }

        /***
         * User inputs args.
         */
        private void InitFromCmd() {
            Options options = new Options();
            options.addRequiredOption("d", "data", true, "(Required) File path of data set");
            options.addRequiredOption("s", "sup", true, "(Required) Minimum support");
            options.addOption("i", "min-node", true, "Minimum number of nodes");
            options.addOption("a", "max-node", true, "Maximum number of nodes");
            options.addOption("r", "result", true, "File path of result");
            options.addOption("h", "help", false, "Help");

            CommandLineParser parser = new DefaultParser();
            HelpFormatter formatter = new HelpFormatter();
            CommandLine cmd = null;
            try {
                cmd = parser.parse(options, args);
                if (cmd.hasOption("h")) {
                    formatter.printHelp("gSpan", options);
                    System.exit(0);
                }
            } catch (ParseException e) {
                formatter.printHelp("gSpan", options);
                System.exit(1);
            }

            inFilePath = cmd.getOptionValue("d");
            minSup = Long.parseLong(cmd.getOptionValue("s"));
            minNodeNum = Long.parseLong(cmd.getOptionValue("i", "0"));
            maxNodeNum = Long.parseLong(cmd.getOptionValue("a", String.valueOf(Long.MAX_VALUE)));
            outFilePath = cmd.getOptionValue("r", inFilePath + "_result");
        }

        /***
         * User runs it directly.
         */
        private void InitFromRun() {
            System.out.println("Please input the file path of data set: ");
            try (Scanner sc = new Scanner(System.in)) {
                inFilePath = sc.nextLine();
                System.out.println("Please set the minimum support: ");
                minSup = sc.nextInt();
                outFilePath = inFilePath + "_result";
            }
        }
    }
}
