package main.fr;

import main.fr.op.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Stack;

import static java.lang.System.exit;

public record Program(List<Op> ops) {

    public void simulate() {
        Stack<Integer> stack = new Stack<>();

        for (Op op : ops) {

            switch (op.type) {
                case OP_PUSH -> stack.push(op.arg);
                case OP_PRINT -> {
                    int a = stack.pop();
                    System.out.println(a);
                }
                default -> exit(1);
            }
        }
    }

    public void compile(String filepath) {
        File file = new File(filepath);
        FileWriter writer;
        try {
            writer = new FileWriter(file);
            PrintWriter out = new PrintWriter(writer);

            out.println("global _start"
                                + ""
                                + "    dump:"
                                + "    mov r8, -3689348814741910323"
                                + "    sub rsp, 40"
                                + "    mov BYTE [rsp+31], 10"
                                + "    lea rcx, [rsp+30]"
                                + ".L2:"
                                + "    mov rax, rdi"
                                + "    mul r8"
                                + "    mov rax, rdi"
                                + "    shr rdx, 3"
                                + "    lea rsi, [rdx+rdx*4]"
                                + "    add rsi, rsi"
                                + "    sub rax, rsi"
                                + "    mov rsi, rcx"
                                + "    sub rcx, 1"
                                + "    add eax, 48"
                                + "    mov BYTE [rcx+1], al"
                                + "    mov rax, rdi"
                                + "    mov rdi, rdx"
                                + "    cmp rax, 9"
                                + "    ja .L2"
                                + "    lea rdx, [rsp+32]"
                                + "    mov edi, 1"
                                + "    xor eax, eax"
                                + "    sub rdx, rsi"
                                + "    mov rax, 1"
                                + "    syscall"
                                + "    add rsp, 40"
                                + "    ret"
                                + ""
                                + "_start:");

            for (Op op : ops) {
                switch (op.type) {
                    case OP_PUSH -> {
                        out.println("    ; -- PUSH " + op.arg + " --");
                        out.println("    mov rax, " + op.arg);
                        out.println("    push rax");
                    }
                    case OP_PRINT -> {
                        out.println("    ; -- DUMP --");
                        out.println("    pop rdi;");
                        out.println("    call dump");
                    }
                    default -> exit(1);
                }
            }

            out.println("    mov rax, 60 ; system call for exit");
            out.println("    mov rdi, 0 ; exit code 0");
            out.println("    syscall ; invoke operating system to exit");

            out.close();
        } catch (IOException e) {
            System.err.println("[ERROR]: " + e.getMessage());
            e.printStackTrace(System.err);

        }
    }
}
