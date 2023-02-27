package fr.main.lexing;

import static fr.main.lexing.TokenType.INT;
import static fr.main.lexing.TokenType.WORD;
import static java.lang.System.exit;

public class Token {
    public final TokenType type;
    public final Location loc;
    public String strValue;
    public int intValue;

    private Token(TokenType type, Location loc) {
        assert TokenType.values().length == 3 : "Exhaustive handling of TokenType";
        this.type = type;
        this.loc = loc;
    }

    public Token(TokenType type, Location loc, String strValue) {
        this(type, loc);
        this.strValue = strValue;
    }

    public Token(TokenType type, Location loc, int intValue) {
        this(type, loc);
        this.intValue = intValue;
    }

    public String strValue() {
        if (type != WORD) {
            System.err.println(loc + " [ERROR]: Trying to get the " + WORD.argType() + "instead of a" + type.argType());
            exit(1);
        }
        return strValue;
    }

    public int intValue() {
        if (type != INT) {
            System.err.println(loc + " [ERROR]: Trying to get the " + INT.argType() + "instead of a" + type.argType());
            exit(1);
        }
        return intValue;
    }

    @Override
    public String toString() {
        String value;
        if (type == INT) {
             value = String.valueOf(intValue);
        }else{
            value = strValue;
        }
        return "Token{" +
                "type=" + type +
                ", value="+ value +
                "}";
    }
}
