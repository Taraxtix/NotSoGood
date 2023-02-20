package main.fr.lexing;

import main.fr.op.OpType;

public enum BuiltinWords {
    PRINT("print", OpType.OP_PRINT),
    PLUS("+", OpType.OP_PLUS),
    MINUS("-", OpType.OP_MINUS),
    MUL("*", OpType.OP_MUL),
    DUP("dup", OpType.OP_DUP),
    DROP("drop", OpType.OP_DROP),
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
    MEM("mem", OpType.OP_MEM),
    STORE("store8", OpType.OP_STORE8),
    LOAD("load8", OpType.OP_LOAD8),
    NONE("", OpType.OP_NONE);

    public final String word;
    public final OpType opType;

    BuiltinWords(String word, OpType opType) {
        assert OpType.values().length == 22 : "Exhaustive handling of OpTypes";

        this.word = word;
        this.opType = opType;
    }
    
    public static OpType getOpTypeFromWord(String word) {
        for (BuiltinWords bWord : BuiltinWords.values()) {
            if( bWord.word.equals(word)){
                return bWord.opType;
            }
        }
        return OpType.OP_NONE;
    }
}
