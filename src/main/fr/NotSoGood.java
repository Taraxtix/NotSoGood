package main.fr;

import main.fr.lexing.Lexer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.System.exit;

public class NotSoGood {

    private static void usage() {
        System.err.println("""
                            Usage: java NotSoGood <command> <filename> [test_option]
                            \tCommands :
                            \tsim\t\tSimulate the given program
                            \tcom\t\tCompile the given program
                            \ttest\t\tRun the tests on the given directory
                            
                            \tTest Option :
                            \trecord\t\tRecord the result of each file in the given directory as the correct output
                            """);
    }
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("[ERROR]: Not enough arguments provided to the program");
            usage();
            exit(1);
        }

        String command = args[0];
        String filename = args[1];

        Program program;

        switch (command) {

            case "sim" -> {
                if (!filename.endsWith(".nsg")){
                    System.out.println("[ERROR]: The provided filename did not use the correct extension.");
                    exit(1);
                }
                program = Lexer.lex_file(filename);
                program.simulate(System.out);
            }
            case "com" -> {
                if (!filename.endsWith(".nsg")){
                    System.out.println("[ERROR]: The provided filename did not use the correct extension.");
                    exit(1);
                }
                String fileBaseName = filename.replace(".nsg", "");
                program = Lexer.lex_file(filename);
                program.compile(fileBaseName + ".asm");
                ProcessBuilder processBuilder = new ProcessBuilder();
                try {
                    processBuilder.command("bash", "-c", "nasm -f elf64 -o " + fileBaseName + ".o " + fileBaseName + ".asm").start();
                    System.out.println("[INFO] : nasm -f elf64 -o " + fileBaseName + ".o " + fileBaseName + ".asm");

                    processBuilder.command("bash", "-c", "ld -o " + fileBaseName + " " + fileBaseName + ".o").start();
                    System.out.println("[INFO] : ld -o " + fileBaseName + " " + fileBaseName + ".o");
                } catch (IOException e) {
                    System.err.println("[ERROR]: " + e.getMessage());
                }
            }
            case "test" -> {
                File directory = new File(filename);
                if (!directory.isDirectory()) {
                    System.err.println("[ERROR] : test should be launch on a directory not on a file !");
                    exit(1);
                }
                File[] files = directory.listFiles();
                assert files != null;
                for (File file : files) {
                    if (!file.getPath().endsWith(".nsg")) continue;
                    program = Lexer.lex_file(file.getPath());

                    String recordPath = filename + "/record/" + file.getName().replace(".nsg", ".txt");
                    if (args.length >= 3 && args[2].equals("record")) {
                        boolean ret = new File(filename + "/record/").mkdir();
                        ret = ret && new File(recordPath).createNewFile();
                        if(!ret) throw new IOException("Cannot create "+recordPath);

                        program.simulate(new PrintStream(recordPath));
                        continue;
                    }
                    if (!new File(recordPath).exists()) {
                        System.err.println("[ERROR] : Record file for " + file.getPath() + "\nTry running record on this directory first");
                        exit(1);
                    }

                    program.simulate(new PrintStream("temp.txt"));

                    String tempFileContent = null;
                    String recordFileContent = null;
                    try {
                        tempFileContent = Files.readString(Path.of("temp.txt")) + "\n";
                        recordFileContent = Files.readString(Path.of(recordPath)) + "\n";
                    } catch (IOException e) {
                        System.err.println("[ERROR]: Cannot read temp.txt");
                        exit(1);
                    }

                    if (tempFileContent.equals(recordFileContent))
                        System.out.println("[SUCCESS] : Test " + file.getPath() + " passed successfully");
                    else
                        System.err.println("[FAILURE] : Test " + file.getPath() + " failed");
                }
            }
        }
    }
}
