package main.fr;

import main.fr.op.*;

import java.io.*;
import java.util.List;
import java.util.Stack;

import static java.lang.System.exit;

public record Program(List<Op> ops) {

    private static final int MEMORY_BUFFER = 64_000;

    public void crossReferencing(){
        assert OpType.values().length == 22 : "Exhaustive handling of OpTypes";

        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < ops().size(); i++) {
            Op op = ops.get(i);
            switch (op.type) {
                case OP_IF, OP_DO, OP_WHILE -> stack.push(i);
                case OP_ELSE -> {
                    int do_addr = stack.pop();
                    ops.get(do_addr).arg = i + 1;
                    stack.push(i);
                }
                case OP_END -> {
                    int pop1_addr = stack.pop();
                    ops.get(pop1_addr).arg = i + 1;
                    int pop2_addr;
                    if(!stack.isEmpty()) pop2_addr = stack.pop();
                    else pop2_addr = pop1_addr;
                    if (ops.get(pop2_addr).type == OpType.OP_IF) op.arg = i+1;
                    else if (ops.get(pop2_addr).type == OpType.OP_WHILE) op.arg = pop2_addr+1;
                }
                default -> {}
            }
        }
    }

    public void simulate(PrintStream out) {
        assert OpType.values().length == 22 : "Exhaustive handling of OpTypes";

        Stack<Integer> stack = new Stack<>();
        byte[] memory = new byte[64_000];

        for (int i = 0; i < ops().size(); i++) {
            Op op = ops.get(i);

            switch (op.type) {
                case OP_PUSH -> stack.push(op.arg);
                case OP_PRINT -> {
                    int a = stack.pop();
                    out.println(a);
                }
                case OP_PLUS -> {
                    int a = stack.pop();
                    int b = stack.pop();
                    stack.push(a + b);
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
                case OP_IF, OP_WHILE -> {}
                case OP_DO -> {
                    int a = stack.pop();
                    if(a == 0) i = op.arg - 1;
                }
                case OP_END, OP_ELSE -> i = op.arg - 1;
                case OP_EQUAL -> {
                    int b = stack.pop();
                    int a = stack.pop();
                    stack.push(a == b ? 1 : 0);
                }case OP_UNEQUAL -> {
                    int b = stack.pop();
                    int a = stack.pop();
                    stack.push(a != b ? 1 : 0);
                }case OP_LESS -> {
                    int b = stack.pop();
                    int a = stack.pop();
                    stack.push(a < b ? 1 : 0);
                }case OP_GREATER -> {
                    int b = stack.pop();
                    int a = stack.pop();
                    stack.push(a > b ? 1 : 0);
                }case OP_LESS_E -> {
                    int b = stack.pop();
                    int a = stack.pop();
                    stack.push(a <= b ? 1 : 0);
                }case OP_GREATER_E -> {
                    int b = stack.pop();
                    int a = stack.pop();
                    stack.push(a >= b ? 1 : 0);
                }case OP_MEM -> stack.push(4202496);
                 case OP_STORE8 -> {
                    int a = stack.pop();
                    int b = stack.pop();
                    memory[b - 4202496] = (byte) a;
                }case OP_LOAD8 -> {
                    int a = stack.pop();
                    stack.push((int) memory[a - 4202496]);
                }
                default -> exit(1);
            }
        }
    }

    public void compile(String filepath) throws IOException {
        assert OpType.values().length == 22 : "Exhaustive handling of OpTypes";

        PrintWriter out = new PrintWriter(new FileWriter(filepath));

        out.println("""
                    global _start
                    section .text
                    
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

        for (int i = 0; i < ops.size(); i++) {
            Op op = ops.get(i);
            switch (op.type) {
                case OP_PUSH -> {
                    out.println("instruction_"+i+":");
                    out.println("    ; -- PUSH " + op.arg + " --");
                    out.println("    mov rax, " + op.arg);
                    out.println("    push rax");
                }case OP_PRINT -> {
                    out.println("instruction_"+i+":");
                    out.println("    ; -- PRINT --");
                    out.println("    pop rdi;");
                    out.println("    call print");
                }case OP_PLUS -> {
                    out.println("instruction_"+i+":");
                    out.println("    ; -- PLUS --");
                    out.println("    pop rax");
                    out.println("    pop rbx");
                    out.println("    add rax, rbx");
                    out.println("    push rax");
                }case OP_MINUS -> {
                    out.println("instruction_"+i+":");
                    out.println("    ; -- MINUS --");
                    out.println("    pop rbx");
                    out.println("    pop rax");
                    out.println("    sub rax, rbx");
                    out.println("    push rax");
                }case OP_MUL -> {
                    out.println("instruction_"+i+":");
                    out.println("    ; -- MUL --");
                    out.println("    pop rax");
                    out.println("    pop rbx");
                    out.println("    imul rax, rbx");
                    out.println("    push rax");
                }case OP_DUP -> {
                    out.println("instruction_"+i+":");
                    out.println("    ; -- DUP --");
                    out.println("    pop rax");
                    out.println("    push rax");
                    out.println("    push rax");
                }case OP_DROP -> {
                    out.println("instruction_"+i+":");
                    out.println("    ; -- DROP --");
                    out.println("    pop rax");
                }case OP_IF -> {
                    out.println("instruction_"+i+":");
                    out.println("    ; -- IF --");
                }case OP_WHILE -> {
                    out.println("instruction_"+i+":");
                    out.println("    ; -- WHILE --");
                }case OP_ELSE -> {
                    out.println("instruction_"+i+":");
                    out.println("    ; -- ELSE --");
                    out.println("    jmp instruction_"+op.arg);
                }case OP_DO -> {
                     out.println("instruction_"+i+":");
                     out.println("    ; -- DO --");
                     out.println("    pop rax");
                     out.println("    test rax, rax");
                     out.println("    je instruction_"+op.arg);
                }case OP_END -> {
                    out.println("instruction_"+i+":");
                    out.println("    ; -- END --");
                    out.println("    jmp instruction_"+op.arg);
                }case OP_EQUAL -> {
                    out.println("instruction_"+i+":");
                    out.println("    ; -- EQUAL --");
                    out.println("    mov rax, 0");
                    out.println("    pop rcx");
                    out.println("    pop rbx");
                    out.println("    cmp rbx, rcx");
                    out.println("    mov rbx, 1");
                    out.println("    cmove rax, rbx");
                    out.println("    push rax");
                }case OP_UNEQUAL -> {
                    out.println("instruction_"+i+":");
                    out.println("    ; -- UNEQUAL --");
                    out.println("    mov rax, 0");
                    out.println("    pop rcx");
                    out.println("    pop rbx");
                    out.println("    cmp rbx, rcx");
                    out.println("    mov rbx, 1");
                    out.println("    cmovne rax, rbx");
                    out.println("    push rax");
                }case OP_LESS -> {
                    out.println("instruction_"+i+":");
                    out.println("    ; -- LESS --");
                    out.println("    mov rax, 0");
                    out.println("    pop rcx");
                    out.println("    pop rbx");
                    out.println("    cmp rbx, rcx");
                    out.println("    mov rbx, 1");
                    out.println("    cmovl rax, rbx");
                    out.println("    push rax");
                }case OP_GREATER -> {
                    out.println("instruction_"+i+":");
                    out.println("    ; -- GREATER --");
                    out.println("    mov rax, 0");
                    out.println("    pop rcx");
                    out.println("    pop rbx");
                    out.println("    cmp rbx, rcx");
                    out.println("    mov rbx, 1");
                    out.println("    cmovg rax, rbx");
                    out.println("    push rax");
                }case OP_LESS_E -> {
                    out.println("instruction_"+i+":");
                    out.println("    ; -- LESS_E --");
                    out.println("    mov rax, 0");
                    out.println("    pop rcx");
                    out.println("    pop rbx");
                    out.println("    cmp rbx, rcx");
                    out.println("    mov rbx, 1");
                    out.println("    cmovle rax, rbx");
                    out.println("    push rax");
                }case OP_GREATER_E -> {
                    out.println("instruction_"+i+":");
                    out.println("    ; -- GREATER_E --");
                    out.println("    mov rax, 0");
                    out.println("    pop rcx");
                    out.println("    pop rbx");
                    out.println("    cmp rbx, rcx");
                    out.println("    mov rbx, 1");
                    out.println("    cmovge rax, rbx");
                    out.println("    push rax");
                }case OP_MEM -> {
                    out.println("instruction_"+i+":");
                    out.println("    ; -- MEM --");
                    out.println("    lea rax, [mem]");
                    out.println("    push rax");
                }case OP_LOAD8 -> {
                    out.println("instruction_"+i+":");
                    out.println("    ; -- LOAD8 --");
                    out.println("    pop rbx");
                    out.println("    mov al, [rbx]");
                    out.println("    movzx rax, al");
                    out.println("    push rax");
                }case OP_STORE8 -> {
                    out.println("instruction_"+i+":");
                    out.println("    ; -- STORE8 --");
                    out.println("    pop rax");
                    out.println("    pop rbx");
                    out.println("    mov [rbx], al");
                }
                default -> exit(1);
            }
        }

        out.println("instruction_"+ops.size()+":");
        out.println("""
                        mov rax, 60 ; system call for exit
                        mov rdi, 0 ; exit code 0
                        syscall ; invoke operating system to exit
                    """);

        out.println("section .bss");
        out.println("mem resb " + MEMORY_BUFFER);

        out.close();
    }
}
