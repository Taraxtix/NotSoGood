package fr.main.lexing;

import static java.lang.System.exit;

public enum TokenType {
    WORD,
    INT,
    STRING;

    public String argType() {
        switch (this) {
            case WORD -> {
                return "word";
            }
            case INT -> {
                return "integer";
            }
            case STRING -> {
                return "string";
            }
            default -> exit(1);
        }
        return null;
    }
}
