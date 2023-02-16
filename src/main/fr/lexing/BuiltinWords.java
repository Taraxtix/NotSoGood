package main.fr.lexing;

import main.fr.op.OpType;

public enum BuiltinWords {
    PRINT("print", OpType.OP_PRINT),
    NONE("", OpType.OP_NONE);

    public final String word;
    public final OpType opType;

    BuiltinWords(String word, OpType opType) {
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
