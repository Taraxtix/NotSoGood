package fr.main;

import fr.main.lexing.Location;
import fr.main.op.Op;
import fr.main.op.OpType;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import static fr.main.DataType.*;
import static fr.main.NotSoGood.logError;
import static fr.main.op.OpType.*;

public class Program {
    private static final int MEMORY_BUFFER = 64_000;
    private static int ip = 0;
    public final List<Op> ops;
    public final HashMap<String, List<Op>> functions;
    public final HashMap<String, List<Op>> macros;
    private final List<String> strList = new ArrayList<>();

    public Program(List<Op> ops, HashMap<String, List<Op>> functions, HashMap<String, List<Op>> macros) {
        this.ops = ops;
        this.functions = functions;
        this.macros = macros;
    }

    public void typeCheck() {
        assert OpType.values().length == 52 : "Exhaustive handling of OpTypes. Expected : " + OpType.values().length;

        Stack<DataType> typeStack = new Stack<>();
        Stack<Location> locStack = new Stack<>();
        Stack<Stack<DataType>> typeStackCheckpoint = new Stack<>();
        Stack<Op> checkpointType = new Stack<>();
        ArrayList<Op> opsCopy = new ArrayList<>(ops.stream().toList());
        for (int i = 0; i < opsCopy.size(); i++) {
            Op op = opsCopy.get(i);
            if (op.type == OpType.OP_CALL && functions.containsKey(op.strArg)) {
                opsCopy.remove(i);
                List<Op> macro = functions.get(op.strArg);
                for (int j = 0; j < macro.size(); j++) {
                    opsCopy.add(j + i, functions.get(op.strArg).get(j));
                }
                i--;
            }
        }

        for (Op op : opsCopy) {
            switch (op.type) {
                case OP_PUSH_INT, OP_ARGC -> {
                    typeStack.push(INT);
                    locStack.push(op.loc);
                }
                case OP_PUSH_STR -> {
                    typeStack.push(INT);
                    typeStack.push(PTR);
                    locStack.push(op.loc);
                    locStack.push(op.loc);
                }
                case OP_ARGV, OP_MEM -> {
                    typeStack.push(PTR);
                    locStack.push(op.loc);
                }
                case OP_PRINT -> {
                    if (typeStack.empty()) logError(op.loc + " Not enough argument for `PRINT` operation");
                    typeStack.pop();
                    locStack.pop();
                }
                case OP_PLUS -> {
                    if (typeStack.size() < 2) logError(op.loc + " Not enough argument for `PLUS` operation");
                    DataType aType = typeStack.pop();
                    DataType bType = typeStack.pop();
                    locStack.pop();
                    locStack.pop();
                    if (aType == INT && bType == INT) {
                        typeStack.push(INT);
                    } else if (aType == PTR && bType == INT) {
                        typeStack.push(PTR);
                    } else if (bType == PTR && aType == INT) {
                        typeStack.push(PTR);
                    } else
                        logError(op.loc + " " + aType + " and " + bType + " are not a valid combination of DataTypes for the `PLUS` operation`");
                    locStack.push(op.loc);
                }
                case OP_MINUS -> {
                    if (typeStack.size() < 2) logError(op.loc + " Not enough argument for `MINUS` operation");
                    DataType aType = typeStack.pop();
                    DataType bType = typeStack.pop();
                    locStack.pop();
                    locStack.pop();
                    if (aType == INT && bType == INT) {
                        typeStack.push(INT);
                    } else if (aType == PTR && bType == PTR) {
                        typeStack.push(INT);
                    } else if (bType == PTR && aType == INT) {
                        typeStack.push(PTR);
                    } else
                        logError(op.loc + " " + aType + " and " + bType + " are not a valid combination of DataTypes for the `MINUS` operation`");
                    locStack.push(op.loc);
                }
                case OP_MUL -> {
                    if (typeStack.size() < 2) logError(op.loc + " Not enough argument for `MUL` operation");
                    DataType aType = typeStack.pop();
                    DataType bType = typeStack.pop();
                    locStack.pop();
                    locStack.pop();
                    if (aType == INT && bType == INT) {
                        typeStack.push(INT);
                    } else
                        logError(op.loc + " " + aType + " and " + bType + " are not a valid combination of DataTypes for the `MUL` operation`");
                    locStack.push(op.loc);
                }
                case OP_DUP -> {
                    if (typeStack.empty()) logError(op.loc + " Not enough argument for `DUP` operation");
                    DataType aType = typeStack.pop();
                    typeStack.push(aType);
                    typeStack.push(aType);
                    locStack.push(op.loc);
                }
                case OP_2DUP -> {
                    if (typeStack.size() < 2) logError(op.loc + " Not enough argument for `2DUP` operation");
                    DataType aType = typeStack.pop();
                    DataType bType = typeStack.pop();
                    typeStack.push(bType);
                    typeStack.push(aType);
                    typeStack.push(bType);
                    typeStack.push(aType);
                    locStack.push(op.loc);
                    locStack.push(op.loc);
                }
                case OP_OVER -> {
                    if (typeStack.size() < 2) logError(op.loc + " Not enough argument for `OVER` operation");
                    DataType aType = typeStack.pop();
                    DataType bType = typeStack.pop();
                    typeStack.push(bType);
                    typeStack.push(aType);
                    typeStack.push(bType);
                    locStack.push(op.loc);
                }
                case OP_DROP -> {
                    if (typeStack.empty()) logError(op.loc + " Not enough argument for `DROP` operation");
                    typeStack.pop();
                    locStack.pop();
                }
                case OP_SWAP -> {
                    if (typeStack.size() < 2) logError(op.loc + " Not enough argument for `SWAP` operation");
                    DataType aType = typeStack.pop();
                    DataType bType = typeStack.pop();
                    Location aLoc = locStack.pop();
                    Location bLoc = locStack.pop();
                    typeStack.push(aType);
                    locStack.push(aLoc);
                    typeStack.push(bType);
                    locStack.push(bLoc);
                }
                case OP_IF, OP_WHILE -> {
                    typeStackCheckpoint.push(new Stack<>());
                    for (DataType dataType : typeStack) {
                        typeStackCheckpoint.peek().push(dataType);
                    }
                    checkpointType.push(op);
                }
                case OP_ELSE -> {
                    typeStackCheckpoint.pop();
                    checkpointType.pop();

                    typeStackCheckpoint.push(new Stack<>());
                    for (int i = 1; i <= typeStack.size(); i++) {
                        typeStackCheckpoint.peek().push(typeStack.get(typeStack.size() - i));
                    }
                    checkpointType.push(op);
                }
                case OP_DO -> {
                    if (typeStack.empty()) logError(op.loc + "Not enough argument for the `DO` operation");
                    DataType type = typeStack.pop();
                    Location loc = locStack.pop();
                    if (type != BOOL) logError(loc + " Expected " + BOOL + " on top of the stack but got " + type);
                    if (!typeStackCheckpoint.peek().equals(typeStack))
                        logError(op.loc + " Condition between `IF` or `WHILE` and `DO` is not allowed to alter the stack");
                }
                case OP_END -> {
                    if (!typeStackCheckpoint.peek().equals(typeStack)) {
                        OpType type = checkpointType.pop().type;
                        if (type == OP_IF)
                            logError(op.loc + " `ELSE-LESS IF` blocks are not allowed to alter the stack "
                                             + "\nExpected: " + typeStackCheckpoint.pop()
                                             + "\n Provided: " + typeStack);
                        else if (type == OP_ELSE)
                            logError(op.loc + " Both banches of `IF-ELSE` blocks does not alter the stack in the same way."
                                             + "\n`IF` branch:   " + typeStackCheckpoint.pop()
                                             + "\n`ELSE` branch: " + typeStack);
                        else if (type == OP_WHILE)
                            logError(op.loc + " `WHILE` blocks are not allowed to alter the stack"
                                             + "\nExpected: " + typeStackCheckpoint.pop()
                                             + "\nProvided: " + typeStack);
                    }
                    typeStackCheckpoint.pop();
                }
                case OP_EQUAL -> {
                    if (typeStack.size() < 2) logError(op.loc + " Not enough argument for `EQUAL` operation");
                    DataType aType = typeStack.pop();
                    DataType bType = typeStack.pop();
                    locStack.pop();
                    locStack.pop();
                    if (aType != bType)
                        logError(op.loc + " Type mismatch for the `EQUAL` operation. Got : " + aType + ", " + bType);
                    typeStack.push(BOOL);
                    locStack.push(op.loc);
                }
                case OP_UNEQUAL -> {
                    if (typeStack.size() < 2) logError(op.loc + " Not enough argument for `UNEQUAL` operation");
                    DataType aType = typeStack.pop();
                    DataType bType = typeStack.pop();
                    locStack.pop();
                    locStack.pop();
                    if (aType != bType)
                        logError(op.loc + " Type mismatch for the `UNEQUAL` operation. Got : " + aType + ", " + bType);
                    typeStack.push(BOOL);
                    locStack.push(op.loc);
                }
                case OP_LESS -> {
                    if (typeStack.size() < 2) logError(op.loc + " Not enough argument for `LESS` operation");
                    DataType aType = typeStack.pop();
                    DataType bType = typeStack.pop();
                    locStack.pop();
                    locStack.pop();
                    if (aType != bType)
                        logError(op.loc + " Type mismatch for the `LESS` operation. Got : " + aType + ", " + bType);
                    typeStack.push(BOOL);
                    locStack.push(op.loc);
                }
                case OP_GREATER -> {
                    if (typeStack.size() < 2) logError(op.loc + " Not enough argument for `GREATER` operation");
                    DataType aType = typeStack.pop();
                    DataType bType = typeStack.pop();
                    locStack.pop();
                    locStack.pop();
                    if (aType != bType)
                        logError(op.loc + " Type mismatch for the `GREATER` operation. Got : " + aType + ", " + bType);
                    typeStack.push(BOOL);
                    locStack.push(op.loc);
                }
                case OP_LESS_E -> {
                    if (typeStack.size() < 2) logError(op.loc + " Not enough argument for `LESS_E` operation");
                    DataType aType = typeStack.pop();
                    DataType bType = typeStack.pop();
                    locStack.pop();
                    locStack.pop();
                    if (aType != bType)
                        logError(op.loc + " Type mismatch for the `LESS_E` operation. Got : " + aType + ", " + bType);
                    typeStack.push(BOOL);
                    locStack.push(op.loc);
                }
                case OP_GREATER_E -> {
                    if (typeStack.size() < 2) logError(op.loc + " Not enough argument for `GREATER_E` operation");
                    DataType aType = typeStack.pop();
                    DataType bType = typeStack.pop();
                    locStack.pop();
                    locStack.pop();
                    if (aType != bType)
                        logError(op.loc + " Type mismatch for the `GREATER_E` operation. Got : " + aType + ", " + bType);
                    typeStack.push(BOOL);
                    locStack.push(op.loc);
                }
                case OP_OR -> {
                    if (typeStack.size() < 2) logError(op.loc + " Not enough argument for `OR` operation");
                    DataType aType = typeStack.pop();
                    DataType bType = typeStack.pop();
                    Location aLoc = locStack.pop();
                    Location bLoc = locStack.pop();
                    if (aType != bType)
                        logError(op.loc + " Type mismatch for the `OR` operation. Got : " + aType + ", " + bType);
                    if (aType == PTR) logError(aLoc + " POINTERS are not allowed in `OR` operation");
                    if (bType == PTR) logError(bLoc + " POINTERS are not allowed in `OR` operation");
                    typeStack.push(aType);
                    locStack.push(op.loc);
                }
                case OP_AND -> {
                    if (typeStack.size() < 2) logError(op.loc + " Not enough argument for `AND` operation");
                    DataType aType = typeStack.pop();
                    DataType bType = typeStack.pop();
                    Location aLoc = locStack.pop();
                    Location bLoc = locStack.pop();
                    if (aType != bType)
                        logError(op.loc + " Type mismatch for the `AND` operation. Got : " + aType + ", " + bType);
                    if (aType == PTR) logError(aLoc + " POINTERS are not allowed in `AND` operation");
                    if (bType == PTR) logError(bLoc + " POINTERS are not allowed in `AND` operation");
                    typeStack.push(aType);
                    locStack.push(op.loc);
                }
                case OP_BNOT -> {
                    if (typeStack.empty()) logError(op.loc + " Not enough argument for `BNOT` operation");
                    DataType type = typeStack.pop();
                    Location aLoc = locStack.pop();
                    if (type != INT) logError(aLoc + " Expected " + INT + " on top of the stack but got " + type);
                    typeStack.push(INT);
                    locStack.push(op.loc);
                }
                case OP_NOT -> {
                    if (typeStack.empty()) logError(op.loc + " Not enough argument for `NOT` operation");
                    DataType type = typeStack.pop();
                    Location aLoc = locStack.pop();
                    if (type != BOOL) logError(aLoc + " Expected " + BOOL + " on top of the stack but got " + type);
                    typeStack.push(BOOL);
                    locStack.push(op.loc);
                }
                case OP_XOR -> {
                    if (typeStack.size() < 2) logError(op.loc + " Not enough argument for `XOR` operation");
                    DataType aType = typeStack.pop();
                    DataType bType = typeStack.pop();
                    Location aLoc = locStack.pop();
                    Location bLoc = locStack.pop();
                    if (aType != bType)
                        logError(op.loc + " Type mismatch for the `XOR` operation. Got : " + aType + ", " + bType);
                    if (aType == PTR) logError(aLoc + " POINTERS are not allowed in `XOR` operation");
                    if (bType == PTR) logError(bLoc + " POINTERS are not allowed in `XOR` operation");
                    typeStack.push(aType);
                    locStack.push(op.loc);
                }
                case OP_LSHIFT -> {
                    if (typeStack.size() < 2) logError(op.loc + " Not enough argument for `LSHIFT` operation");
                    DataType aType = typeStack.pop();
                    DataType bType = typeStack.pop();
                    locStack.pop();
                    locStack.pop();
                    if (aType != INT || bType != aType)
                        logError(op.loc + " " + aType + " and " + bType + " are not a valid combination of DataTypes for the `LSHIFT` operation`");
                    typeStack.push(INT);
                    locStack.push(op.loc);
                }
                case OP_RSHIFT -> {
                    if (typeStack.size() < 2) logError(op.loc + " Not enough argument for `RSHIFT` operation");
                    DataType aType = typeStack.pop();
                    DataType bType = typeStack.pop();
                    locStack.pop();
                    locStack.pop();
                    if (aType != INT || bType != aType)
                        logError(op.loc + " " + aType + " and " + bType + " are not a valid combination of DataTypes for the `RSHIFT` operation`");
                    typeStack.push(INT);
                    locStack.push(op.loc);
                }
                case OP_STORE8, OP_STORE16, OP_STORE32, OP_STORE64 -> {
                    if (typeStack.size() < 2) logError(op.loc + " Not enough arguments for `STORE` operation");
                    typeStack.pop();
                    DataType bType = typeStack.pop();
                    locStack.pop();
                    Location bLoc = locStack.pop();
                    if (bType != PTR)
                        logError(bLoc + " Expected " + PTR + " on second place of the stack but got " + bType);
                }
                case OP_LOAD8, OP_LOAD16, OP_LOAD32, OP_LOAD64 -> {
                    if (typeStack.empty()) logError(op.loc + " Not enough argument for `LOAD` operation");
                    DataType type = typeStack.pop();
                    Location loc = locStack.pop();
                    if (type != PTR) logError(loc + " Expected " + PTR + " on top of the stack but got " + type);
                    typeStack.push(INT);
                    locStack.push(op.loc);
                }
                case OP_SYSCALL0 -> {
                    if (typeStack.empty()) logError(op.loc + " Not enough argument for `SYSCALL0` operation");
                    DataType type = typeStack.pop();
                    Location loc = locStack.pop();
                    if (type != INT) logError(loc + " Expected " + INT + " on top of the stack but got " + type);
                    typeStack.push(INT);
                    locStack.push(op.loc);
                }
                case OP_SYSCALL1 -> {
                    if (typeStack.size() < 2) logError(op.loc + " Not enough argument for `SYSCALL1` operation");
                    DataType type = typeStack.pop();
                    Location loc = locStack.pop();
                    typeStack.pop();
                    locStack.pop();
                    if (type != INT) logError(loc + " Expected " + INT + " on top of the stack but got " + type);
                    typeStack.push(INT);
                    locStack.push(op.loc);
                }
                case OP_SYSCALL2 -> {
                    if (typeStack.size() < 3) logError(op.loc + " Not enough argument for `SYSCALL2` operation");
                    DataType type = typeStack.pop();
                    Location loc = locStack.pop();
                    for (int i = 0; i < 2; i++) {
                        locStack.pop();
                        typeStack.pop();
                    }
                    if (type != INT) logError(loc + " Expected " + INT + " on top of the stack but got " + type);
                    typeStack.push(INT);
                    locStack.push(op.loc);
                }
                case OP_SYSCALL3 -> {
                    if (typeStack.size() < 4) logError(op.loc + " Not enough argument for `SYSCALL3` operation");
                    DataType type = typeStack.pop();
                    Location loc = locStack.pop();
                    for (int i = 0; i < 3; i++) {
                        locStack.pop();
                        typeStack.pop();
                    }
                    if (type != INT) logError(loc + " Expected " + INT + " on top of the stack but got " + type);
                    typeStack.push(INT);
                    locStack.push(op.loc);
                }
                case OP_SYSCALL4 -> {
                    if (typeStack.size() < 4) logError(op.loc + " Not enough argument for `SYSCALL4` operation");
                    DataType type = typeStack.pop();
                    Location loc = locStack.pop();
                    for (int i = 0; i < 4; i++) {
                        locStack.pop();
                        typeStack.pop();
                    }
                    if (type != INT) logError(loc + " Expected " + INT + " on top of the stack but got " + type);
                    typeStack.push(INT);
                    locStack.push(op.loc);
                }
                case OP_SYSCALL5 -> {
                    if (typeStack.size() < 5) logError(op.loc + " Not enough argument for `SYSCALL5` operation");
                    DataType type = typeStack.pop();
                    Location loc = locStack.pop();
                    for (int i = 0; i < 5; i++) {
                        locStack.pop();
                        typeStack.pop();
                    }
                    if (type != INT) logError(loc + " Expected " + INT + " on top of the stack but got " + type);
                    typeStack.push(INT);
                    locStack.push(op.loc);
                }
                case OP_SYSCALL6 -> {
                    if (typeStack.size() < 7) logError(op.loc + " Not enough argument for `SYSCALL6` operation");
                    DataType type = typeStack.pop();
                    Location loc = locStack.pop();
                    for (int i = 0; i < 6; i++) {
                        locStack.pop();
                        typeStack.pop();
                    }
                    if (type != INT) logError(loc + " Expected " + INT + " on top of the stack but got " + type);
                    typeStack.push(INT);
                    locStack.push(op.loc);
                }
                case OP_CALL -> {

                }
                case OP_FUNC, OP_MACRO, OP_INCLUDE, OP_NONE -> {
                }
            }
        }
        if (!typeStack.empty()) {
            logError(locStack.pop() + " Unhandled data on the stack.\nStack : " + typeStack);
        }
    }

    private void expandMacro(List<Op> func) {
        for (int i = 0; i < func.size(); i++) {
            Op op = func.get(i);
            if (op.type == OpType.OP_CALL && macros.containsKey(op.strArg)) {
                func.remove(i);
                List<Op> macro = macros.get(op.strArg);
                for (int j = 0; j < macro.size(); j++) {
                    func.add(j + i, macros.get(op.strArg).get(j));
                }
                i--;
            }
        }
    }

    public void expandMacros() {
        expandMacro(ops);
        for (List<Op> func : functions.values()) {
            expandMacro(func);
        }
    }

    public void crossReferencing() {
        assert OpType.values().length == 52 : "Exhaustive handling of OpTypes. Expected : " + OpType.values().length;

        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < ops.size(); i++) {
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
                    if (!stack.isEmpty()) pop2_addr = stack.pop();
                    else pop2_addr = pop1_addr;
                    if (ops.get(pop2_addr).type == OP_IF) op.arg = i + 1;
                    else if (ops.get(pop2_addr).type == OpType.OP_WHILE) op.arg = pop2_addr + 1;
                }
                default -> {
                }
            }
        }
    }

    private void writeAsmFromOp(Op op, PrintWriter out) {
        assert OpType.values().length == 52 : "Exhaustive handling of OpTypes. Expected : " + OpType.values().length;
        out.println("instruction_" + ip + ":");
        switch (op.type) {
            case OP_PUSH_INT -> {
                out.println("    ; -- PUSH_INT " + op.arg + " --");
                out.println("    mov rax, " + op.arg);
                out.println("    push rax");
            }
            case OP_PUSH_STR -> {
                if (!strList.contains(op.strArg)) strList.add(op.strArg);
                int index = strList.indexOf(op.strArg);
                out.println("    ; -- PUSH_STR --");
                out.println("    mov rax, " + op.strArg.length());
                out.println("    push rax");
                out.println("    lea rax, [str_" + index + "]");
                out.println("    push rax");
            }
            case OP_ARGC -> {
                out.println("    ; -- ARGC --");
                out.println("    push r14");
            }
            case OP_ARGV -> {
                out.println("    ; -- ARGV --");
                out.println("    push r13");
            }
            case OP_PRINT -> {
                out.println("    ; -- PRINT --");
                out.println("    pop rdi");
                out.println("    call print");
            }
            case OP_PLUS -> {
                out.println("    ; -- PLUS --");
                out.println("    pop rax");
                out.println("    pop rbx");
                out.println("    add rax, rbx");
                out.println("    push rax");
            }
            case OP_MINUS -> {
                out.println("    ; -- MINUS --");
                out.println("    pop rbx");
                out.println("    pop rax");
                out.println("    sub rax, rbx");
                out.println("    push rax");
            }
            case OP_MUL -> {
                out.println("    ; -- MUL --");
                out.println("    pop rax");
                out.println("    pop rbx");
                out.println("    imul rax, rbx");
                out.println("    push rax");
            }
            case OP_DUP -> {
                out.println("    ; -- DUP --");
                out.println("    pop rax");
                out.println("    push rax");
                out.println("    push rax");
            }
            case OP_2DUP -> {
                out.println("    ; -- 2DUP --");
                out.println("    pop rax");
                out.println("    pop rbx");
                out.println("    push rbx");
                out.println("    push rax");
                out.println("    push rbx");
                out.println("    push rax");
            }
            case OP_OVER -> {
                out.println("    ; -- OVER --");
                out.println("    pop rax");
                out.println("    pop rbx");
                out.println("    push rbx");
                out.println("    push rax");
                out.println("    push rbx");
            }
            case OP_DROP -> {
                out.println("    ; -- DROP --");
                out.println("    pop rax");
            }
            case OP_SWAP -> {
                out.println("    ; -- SWAP --");
                out.println("    pop rax");
                out.println("    pop rbx");
                out.println("    push rax");
                out.println("    push rbx");
            }
            case OP_IF -> out.println("    ; -- IF --");
            case OP_WHILE -> out.println("    ; -- WHILE --");
            case OP_ELSE -> {
                out.println("    ; -- ELSE --");
                out.println("    jmp instruction_" + op.arg);
            }
            case OP_DO -> {
                out.println("    ; -- DO --");
                out.println("    pop rax");
                out.println("    test rax, rax");
                out.println("    je instruction_" + op.arg);
            }
            case OP_END -> {
                out.println("    ; -- END --");
                out.println("    jmp instruction_" + op.arg);
            }
            case OP_EQUAL -> {
                out.println("    ; -- EQUAL --");
                out.println("    mov rax, 0");
                out.println("    pop rcx");
                out.println("    pop rbx");
                out.println("    cmp rbx, rcx");
                out.println("    mov rbx, 1");
                out.println("    cmove rax, rbx");
                out.println("    push rax");
            }
            case OP_UNEQUAL -> {
                out.println("    ; -- UNEQUAL --");
                out.println("    mov rax, 0");
                out.println("    pop rcx");
                out.println("    pop rbx");
                out.println("    cmp rbx, rcx");
                out.println("    mov rbx, 1");
                out.println("    cmovne rax, rbx");
                out.println("    push rax");
            }
            case OP_LESS -> {
                out.println("    ; -- LESS --");
                out.println("    mov rax, 0");
                out.println("    pop rcx");
                out.println("    pop rbx");
                out.println("    cmp rbx, rcx");
                out.println("    mov rbx, 1");
                out.println("    cmovl rax, rbx");
                out.println("    push rax");
            }
            case OP_GREATER -> {
                out.println("    ; -- GREATER --");
                out.println("    mov rax, 0");
                out.println("    pop rcx");
                out.println("    pop rbx");
                out.println("    cmp rbx, rcx");
                out.println("    mov rbx, 1");
                out.println("    cmovg rax, rbx");
                out.println("    push rax");
            }
            case OP_LESS_E -> {
                out.println("    ; -- LESS_E --");
                out.println("    mov rax, 0");
                out.println("    pop rcx");
                out.println("    pop rbx");
                out.println("    cmp rbx, rcx");
                out.println("    mov rbx, 1");
                out.println("    cmovle rax, rbx");
                out.println("    push rax");
            }
            case OP_GREATER_E -> {
                out.println("    ; -- GREATER_E --");
                out.println("    mov rax, 0");
                out.println("    pop rcx");
                out.println("    pop rbx");
                out.println("    cmp rbx, rcx");
                out.println("    mov rbx, 1");
                out.println("    cmovge rax, rbx");
                out.println("    push rax");
            }
            case OP_OR -> {
                out.println("    ; -- OR --");
                out.println("    pop rbx");
                out.println("    pop rax");
                out.println("    or rax, rbx");
                out.println("    push rax");
            }
            case OP_XOR -> {
                out.println("    ; -- XOR --");
                out.println("    pop rbx");
                out.println("    pop rax");
                out.println("    xor rax, rbx");
                out.println("    push rax");
            }
            case OP_AND -> {
                out.println("    ; -- AND --");
                out.println("    pop rbx");
                out.println("    pop rax");
                out.println("    and rax, rbx");
                out.println("    push rax");
            }
            case OP_BNOT -> {
                out.println("    ; -- BNOT --");
                out.println("    pop rax");
                out.println("    not rax");
                out.println("    push rax");
            }
            case OP_NOT -> {
                out.println("    ; -- NOT --");
                out.println("    mov rbx, 0");
                out.println("    mov rcx, 1");
                out.println("    pop rax");
                out.println("    cmp rax, 0");
                out.println("    cmove rbx, rcx");
                out.println("    push rbx");
            }
            case OP_LSHIFT -> {
                out.println("    ; -- LSHIFT --");
                out.println("    pop rcx");
                out.println("    pop rax");
                out.println("    sal rax, cl");
                out.println("    push rax");
            }
            case OP_RSHIFT -> {
                out.println("    ; -- RSHIFT --");
                out.println("    pop rcx");
                out.println("    pop rax");
                out.println("    sar rax, cl");
                out.println("    push rax");
            }
            case OP_MEM -> {
                out.println("    ; -- MEM --");
                out.println("    lea rax, [mem]");
                out.println("    push rax");
            }
            case OP_LOAD8 -> {
                out.println("    ; -- LOAD8 --");
                out.println("    pop rbx");
                out.println("    mov al, [rbx]");
                out.println("    movzx rax, al");
                out.println("    push rax");
            }
            case OP_STORE8 -> {
                out.println("    ; -- STORE8 --");
                out.println("    pop rax");
                out.println("    pop rbx");
                out.println("    mov [rbx], al");
            }
            case OP_LOAD16 -> {
                out.println("    ; -- LOAD16 --");
                out.println("    pop rbx");
                out.println("    mov ax, [rbx]");
                out.println("    movzx rax, ax");
                out.println("    push rax");
            }
            case OP_STORE16 -> {
                out.println("    ; -- STORE16 --");
                out.println("    pop rax");
                out.println("    pop rbx");
                out.println("    mov [rbx], ax");
            }
            case OP_LOAD32 -> {
                out.println("    ; -- LOAD32 --");
                out.println("    pop rbx");
                out.println("    mov eax, [rbx]");
                out.println("    movzx rax, eax");
                out.println("    push rax");
            }
            case OP_STORE32 -> {
                out.println("    ; -- STORE32 --");
                out.println("    pop rax");
                out.println("    pop rbx");
                out.println("    mov [rbx], eax");
            }
            case OP_LOAD64 -> {
                out.println("    ; -- LOAD64 --");
                out.println("    pop rbx");
                out.println("    mov rax, [rbx]");
                out.println("    push rax");
            }
            case OP_STORE64 -> {
                out.println("    ; -- STORE64 --");
                out.println("    pop rax");
                out.println("    pop rbx");
                out.println("    mov [rbx], rax");
            }
            case OP_SYSCALL0 -> {
                out.println("    ; -- SYSCALL0 --");
                out.println("    pop rax");
                out.println("    syscall");
                out.println("    push rax");
            }
            case OP_SYSCALL1 -> {
                out.println("    ; -- SYSCALL1 --");
                out.println("    pop rax");
                out.println("    pop rdi");
                out.println("    syscall");
                out.println("    push rax");
            }
            case OP_SYSCALL2 -> {
                out.println("    ; -- SYSCALL2 --");
                out.println("    pop rax");
                out.println("    pop rdi");
                out.println("    pop rsi");
                out.println("    syscall");
                out.println("    push rax");
            }
            case OP_SYSCALL3 -> {
                out.println("    ; -- SYSCALL3 --");
                out.println("    pop rax");
                out.println("    pop rdi");
                out.println("    pop rsi");
                out.println("    pop rdx");
                out.println("    syscall");
                out.println("    push rax");
            }
            case OP_SYSCALL4 -> {
                out.println("    ; -- SYSCALL4 --");
                out.println("    pop rax");
                out.println("    pop rdi");
                out.println("    pop rsi");
                out.println("    pop rdx");
                out.println("    pop r10");
                out.println("    syscall");
                out.println("    push rax");
            }
            case OP_SYSCALL5 -> {
                out.println("    ; -- SYSCALL5 --");
                out.println("    pop rax");
                out.println("    pop rdi");
                out.println("    pop rsi");
                out.println("    pop rdx");
                out.println("    pop r10");
                out.println("    pop r8");
                out.println("    syscall");
                out.println("    push rax");
            }
            case OP_SYSCALL6 -> {
                out.println("    ; -- SYSCALL6 --");
                out.println("    pop rax");
                out.println("    pop rdi");
                out.println("    pop rsi");
                out.println("    pop rdx");
                out.println("    pop r10");
                out.println("    pop r8");
                out.println("    pop r9");
                out.println("    syscall");
                out.println("    push rax");
            }
            case OP_CALL -> {
                if (functions.containsKey(op.strArg)) {
                    out.println("    ; -- CALL " + op.strArg + " --");
                    out.println("    call " + op.strArg);
                } else if (macros.containsKey(op.strArg)) {
                    assert false : op.loc + " Trying to access " + op.strArg + " macro";
                } else assert false : "Unreachable";
            }
            case OP_FUNC, OP_MACRO, OP_INCLUDE, OP_NONE -> logError(op.loc + "Unreachable case : " + op.type);
        }
    }

    public void compile(String filepath) throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(filepath));
        int start_ip = ip;

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
                                                
                            _start:
                                pop r14
                                pop r13
                            """);
        for (; ip < ops.size() + start_ip; ip++) {
            Op op = ops.get(ip - start_ip);
            writeAsmFromOp(op, out);
        }

        out.println("instruction_" + ip + ":");
        out.println("""
                                mov rax, 60 ; system call for exit
                                mov rdi, 0 ; exit code 0
                                syscall ; invoke operating system to exit
                            """);

        out.println("   ; START OF FUNCTION DECLARATION\n");
        for (String fName : functions.keySet()) {
            start_ip = ++ip;

            out.println(fName + ":");
            out.println("    pop r15");
            for (; ip < functions.get(fName).size() + start_ip; ip++) {
                Op op = functions.get(fName).get(ip - start_ip);
                writeAsmFromOp(op, out);
            }
            out.println("    ; -- RET --");
            out.println("    push r15");
            out.println("    ret\n");
        }

        out.println("section .bss");
        out.println("mem: resb " + MEMORY_BUFFER);
        out.println("section .data");
        for (int i = 0; i < strList.size(); i++) {
            out.print("str_" + i + " db ");
            for (int c : strList.get(i).toCharArray()) {
                out.print(c + ", ");
            }
            out.println("0");
        }

        out.close();
    }
}
