package fr.main.lexing;

import fr.main.op.OpType;

public enum BuiltinWords {
    ARGV("argv", OpType.OP_ARGV),
    ARGC("argc", OpType.OP_ARGC),
    PRINT("print", OpType.OP_PRINT),
    PLUS("+", OpType.OP_PLUS),
    MINUS("-", OpType.OP_MINUS),
    MUL("*", OpType.OP_MUL),
    DUP("dup", OpType.OP_DUP),
    _2DUP("2dup", OpType.OP_2DUP),
    OVER("over", OpType.OP_OVER),
    DROP("drop", OpType.OP_DROP),
    SWAP("swap", OpType.OP_SWAP),
    IF("if", OpType.OP_IF),
    ELSE("else", OpType.OP_ELSE),
    WHILE("while", OpType.OP_WHILE),
    DO("do", OpType.OP_DO),
    END("end", OpType.OP_END),
    EQUAL("=", OpType.OP_EQUAL),
    UNEQUAL("!=", OpType.OP_UNEQUAL),
    LESS("<", OpType.OP_LESS),
    GREATER(">", OpType.OP_GREATER),
    LESS_E("<=", OpType.OP_LESS_E),
    GREATER_E(">=", OpType.OP_GREATER_E),
    OR("|", OpType.OP_OR),
    AND("&", OpType.OP_AND),
    BNOT("b!", OpType.OP_BNOT),
    NOT("!", OpType.OP_NOT),
    XOR("xor", OpType.OP_XOR),
    LSHIFT("<<", OpType.OP_LSHIFT),
    RSHIFT(">>", OpType.OP_RSHIFT),
    MEM("mem", OpType.OP_MEM),
    STORE8("store8", OpType.OP_STORE8),
    LOAD8("load8", OpType.OP_LOAD8),
    STORE16("store16", OpType.OP_STORE16),
    LOAD16("load16", OpType.OP_LOAD16),
    STORE32("store32", OpType.OP_STORE32),
    LOAD32("load32", OpType.OP_LOAD32),
    STORE64("store64", OpType.OP_STORE64),
    LOAD64("load64", OpType.OP_LOAD64),
    SYSCALL0("syscall0", OpType.OP_SYSCALL0),
    SYSCALL1("syscall1", OpType.OP_SYSCALL1),
    SYSCALL2("syscall2", OpType.OP_SYSCALL2),
    SYSCALL3("syscall3", OpType.OP_SYSCALL3),
    SYSCALL4("syscall4", OpType.OP_SYSCALL4),
    SYSCALL5("syscall5", OpType.OP_SYSCALL5),
    SYSCALL6("syscall6", OpType.OP_SYSCALL6),
    FUNC("func", OpType.OP_FUNC),
    MACRO("macro", OpType.OP_MACRO),
    INCLUDE("include", OpType.OP_INCLUDE),
    NONE("", OpType.OP_NONE);

    public final String word;
    public final OpType opType;

    BuiltinWords(String word, OpType opType) {
        assert OpType.values().length == 52 : "Exhaustive handling of OpTypes. Expected : " + OpType.values().length;

        this.word = word;
        this.opType = opType;
    }

    public static OpType getOpTypeFromWord(String word) {
        for (BuiltinWords bWord : BuiltinWords.values()) {
            if (bWord.word.equals(word)) {
                return bWord.opType;
            }
        }
        return OpType.OP_NONE;
    }
}
