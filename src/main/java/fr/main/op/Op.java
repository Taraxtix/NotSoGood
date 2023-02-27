package fr.main.op;

import fr.main.lexing.Location;

public class Op {

    public final OpType type;
    public final Location loc;
    public int arg = 0;
    public String strArg;

    public Op(Location loc, OpType type) {
        this.loc = loc;
        this.type = type;
    }

    public Op(Location loc, OpType type, int intValue) {
        this(loc, type);
        this.arg = intValue;
    }

    public Op(Location loc, OpType type, String strValue) {
        this(loc, type);
        this.strArg = strValue;
    }
}
