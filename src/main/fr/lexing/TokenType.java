package main.fr.lexing;

import static java.lang.System.exit;

public enum TokenType {
    WORD,
    INT;

    public String argType(){
        switch (this){
            case WORD -> {
                return "string";
            }
            case INT -> {
                return "integer";
            }
            default -> exit(1);
        }
        return null;
    }
}
