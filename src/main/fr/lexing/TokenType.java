package main.fr.lexing;

import main.fr.UnreachableCodeException;

public enum TokenType {
    WORD,
    INT;

    public String argType() throws UnreachableCodeException {
        switch (this){
            case WORD -> {
                return "string";
            }
            case INT -> {
                return "integer";
            }
        }
        throw new UnreachableCodeException();
    }
}
