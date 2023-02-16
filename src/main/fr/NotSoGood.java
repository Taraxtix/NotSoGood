package main.fr;

import main.fr.lexing.Lexer;

import java.io.IOException;

import static java.lang.System.exit;

public class NotSoGood {

    private static void usage() {
        System.err.println("""
                            Usage: java src/NotSoGood <command> <filename>
                            \tCommands :
                            \tsim\t\tSimulate the program
                            \tcom\t\tCompile the program
                            """);
    }
    public static void main(String[] args) throws UnreachableCodeException {
        if (args.length < 2) {
            System.err.println("[ERROR]: Not enough arguments provided to the program");
            usage();
            exit(1);
        }
        String option = args[0];
        String program_path = args[1];
        if (!program_path.endsWith(".nsg")){
            System.out.println("[ERROR]: The program path provided did not use the correct extension.");
            exit(1);
        }
        String fileBaseName = program_path.replace(".nsg", "");

        Program program = Lexer.lex_file(program_path);
        if (option.equals("sim")) {
            program.simulate();
        } else if (option.contains("com")) {
            program.compile(program_path);
            ProcessBuilder processBuilder = new ProcessBuilder();
            try {
                processBuilder.command("bash", "-c", "nasm -f elf64 -o "+ fileBaseName +".o "+ fileBaseName +".asm").start();
                System.out.println("[INFO] : nasm -f elf64 -o "+ fileBaseName +".o "+ fileBaseName +".asm");

                processBuilder.command("bash", "-c", "ld -o "+ fileBaseName +" "+ fileBaseName +".o").start();
                System.out.println("[INFO] : ld -o "+ fileBaseName +" "+ fileBaseName +".o");
            } catch (IOException e) {
                System.err.println("[ERROR]: "+ e.getMessage());
            }
        }
    }
}
