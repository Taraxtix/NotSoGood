package main.fr.lexing;

import static java.lang.System.exit;
import static main.fr.lexing.TokenType.*;

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

    public Token(Location loc, String strValue){
        this(WORD, loc);
        this.strValue = strValue;
    }

    public String strValue(){
        if(type != WORD){
            System.err.println(loc + " [ERROR]: Trying to get the " + WORD.argType() + "instead of a" + type.argType());
            exit(1);
        }
        return strValue;
    }

    public Token(Location loc, int intValue){
        this(INT, loc);
        this.intValue = intValue;
    }

    public int intValue(){
        if(type != INT){
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
