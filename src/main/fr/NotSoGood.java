package main.fr;

import main.fr.op.*;
import java.io.IOException;
import java.io.PrintStream;

public class NotSoGood {

    private static void usage(PrintStream out) {
        out.println("""
                            Usage: java src/NotSoGood <command> <filename>
                            \tCommands :
                            \tsim\t\tSimulate the program
                            \tcom\t\tCompile the program
                            """);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("[ERROR]: Not enough arguments provided to the program");
            usage(System.err);
            System.exit(1);
        }

        Program program = new Program();
        program.ops.add(new OpPush(10));
        program.ops.add(new OpPush(20));
        program.ops.add(new OpDump());
        program.ops.add(new OpDump());

        if (args[0].equals("sim")) {
            program.simulate();
        } else if (args[0].contains("com")) {
            program.compile();
            ProcessBuilder processBuilder = new ProcessBuilder();
            try {
                processBuilder.command("bash", "-c", "nasm -f elf64 -o generated/test.o generated/test.asm").start();
                System.out.println("[INFO] : nasm -f elf64 -o generated/test.o generated/test.asm");

                processBuilder.command("bash", "-c", "ld -o generated/test generated/test.o").start();
                System.out.println("[INFO] : ld -o generated/test generated/test.o");
            } catch (IOException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }
    }
}