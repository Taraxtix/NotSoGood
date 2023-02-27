package fr.main;

import fr.main.lexing.Lexer;

import java.io.IOException;
import java.util.logging.Logger;

import static java.lang.System.exit;

public class NotSoGood {

    public static final ProcessBuilder processBuilder = new ProcessBuilder();
    private static final Logger log = Logger.getLogger("Logger");

    private static String usage() {
        return "Usage: java NotSoGood <filename> [OPTION]";
    }

    public static void logError(String msg) {
        log.severe(msg);
        exit(1);
    }

    public static void logInfo(String msg) {
        System.out.println("INFO: " + msg);
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            logError("Not enough arguments provided to the program\n" + usage());
        }

        String filename = args[0];
        if (!filename.endsWith(".nsg")) {
            logError("The provided filename did not use the correct extension.");
        }
        String fileBaseName = filename.replace(".nsg", "");

        String option = "";
        if (args.length >= 2) option = args[1];

        Program program = Lexer.lexFile(filename);
        program.expandMacros();
        program.crossReferencing();
        program.typeCheck();
        program.compile(fileBaseName + ".asm");
        try {
            processBuilder.command("bash", "-c", "nasm -f elf64 -o " + fileBaseName + ".o " + fileBaseName + ".asm").start();
            logInfo("nasm -f elf64 -o " + fileBaseName + ".o " + fileBaseName + ".asm");

            processBuilder.command("bash", "-c", "ld -o " + fileBaseName + " " + fileBaseName + ".o").start();
            logInfo("ld -o " + fileBaseName + " " + fileBaseName + ".o");

            if (!option.equals("-ASM")) {
                processBuilder.command("bash", "-c", "rm " + fileBaseName + ".o " + fileBaseName + ".asm").start();
                logInfo("rm " + fileBaseName + ".o " + fileBaseName + ".asm");
            } else {
                processBuilder.command("bash", "-c", "rm " + fileBaseName + ".o").start();
                logInfo("rm " + fileBaseName + ".o");
            }
        } catch (IOException e) {
            logError(e.getMessage());
        }
    }
}
