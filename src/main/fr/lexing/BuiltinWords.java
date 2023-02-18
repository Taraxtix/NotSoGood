package main.fr.lexing;

import main.fr.op.OpType;

public enum BuiltinWords {
    PRINT("print", OpType.OP_PRINT),
    PLUS("+", OpType.OP_PLUS),
    MINUS("-", OpType.OP_MINUS),
    MUL("*", OpType.OP_MUL),
    DUP("dup", OpType.OP_DUP),
    DROP("drop", OpType.OP_DROP),
    NONE("", OpType.OP_NONE);

    public final String word;
    public final OpType opType;

    BuiltinWords(String word, OpType opType) {
        assert OpType.values().length == 8 : "Exhaustive handling of OpTypes";

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
