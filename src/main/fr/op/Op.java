package main.fr.op;

import main.fr.lexing.Location;

public class Op {

    public final OpType type;
    public final Location loc;
    public int arg = 0;

    public Op(Location loc, OpType type){
        this.loc = loc;
        this.type = type;
    }

    public Op(Location loc, OpType type, int intValue) {
        this(loc, type);
        this.arg = intValue;
    }


}
