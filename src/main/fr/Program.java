package main.fr;

import main.fr.op.*;

import java.io.*;
import java.util.List;
import java.util.Stack;

import static java.lang.System.exit;

public record Program(List<Op> ops) {

    public void simulate(PrintStream out) {
        assert OpType.values().length == 8 : "Exhaustive handling of OpTypes";

        Stack<Integer> stack = new Stack<>();
        for (Op op : ops) {

            switch (op.type) {
                case OP_PUSH -> stack.push(op.arg);
                case OP_PRINT -> {
                    int a = stack.pop();
                    out.println(a);
                }
                case OP_PLUS -> {
                    int a = stack.pop();
                    int b = stack.pop();
                    stack.push(a+b);
                }
                case OP_MINUS -> {
                    int b = stack.pop();
                    int a = stack.pop();
                    stack.push(a - b);
                }case OP_MUL -> {
                    int a = stack.pop();
                    int b = stack.pop();
                    stack.push(a * b);
                }case OP_DUP -> {
                    int a = stack.pop();
                    stack.push(a);
                    stack.push(a);
                }case OP_DROP -> stack.pop();
                default -> exit(1);
            }
        }
    }

    public void compile(String filepath) throws IOException {
        assert OpType.values().length == 8 : "Exhaustive handling of OpTypes";

        PrintWriter out = new PrintWriter(new FileWriter(filepath));

        out.println("""
                    global _start
                    
                    print:
                        mov r8, -3689348814741910323
                        sub rsp, 40
                        mov BYTE [rsp+31], 10
                        lea rcx, [rsp+30]
                    .L2:
                        mov rax, rdi
                        mul r8
                        mov rax, rdi
                        shr rdx, 3
                        lea rsi, [rdx+rdx*4]
                        add rsi, rsi
                        sub rax, rsi
                        mov rsi, rcx
                        sub rcx, 1
                        add eax, 48
                        mov BYTE [rcx+1], al
                        mov rax, rdi
                        mov rdi, rdx
                        cmp rax, 9
                        ja .L2
                        lea rdx, [rsp+32]
                        mov edi, 1
                        xor eax, eax
                        sub rdx, rsi
                        mov rax, 1
                        syscall
                        add rsp, 40
                        ret
                    
                    _start:""");

        for (Op op : ops) {
            switch (op.type) {
                case OP_PUSH -> {
                    out.println("    ; -- PUSH " + op.arg + " --");
                    out.println("    mov rax, " + op.arg);
                    out.println("    push rax");
                }
                case OP_PRINT -> {
                    out.println("    ; -- PRINT --");
                    out.println("    pop rdi;");
                    out.println("    call print");
                }
                case OP_PLUS -> {
                    out.println("   ; -- PLUS --");
                    out.println("   pop rax");
                    out.println("   pop rbx");
                    out.println("   add rax, rbx");
                    out.println("   push rax");
                }
                case OP_MINUS -> {
                    out.println("   ; -- MINUS --");
                    out.println("   pop rbx");
                    out.println("   pop rax");
                    out.println("   sub rax, rbx");
                    out.println("   push rax");
                }case OP_MUL -> {
                    out.println("   ; -- MUL --");
                    out.println("   pop rax");
                    out.println("   pop rbx");
                    out.println("   imul rax, rbx");
                    out.println("   push rax");
                }case OP_DUP -> {
                    out.println("   ; -- DUP --");
                    out.println("   pop rax");
                    out.println("   push rax");
                    out.println("   push rax");
                }case OP_DROP -> {
                    out.println("   ; -- DROP --");
                    out.println("   pop rax");
                }
                default -> exit(1);
            }
        }

        out.println("""
                        
                        mov rax, 60 ; system call for exit
                        mov rdi, 0 ; exit code 0
                        syscall ; invoke operating system to exit
                    """);

        out.close();
    }
}
