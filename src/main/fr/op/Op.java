package main.fr.op;

public abstract class Op {
    public OpType type;

    Op(OpType type) {
        this.type = type;
    }
}
