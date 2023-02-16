package main.fr.op;

public class OpPush extends Op {
    public int arg;

    public OpPush(int arg) {
        super(OpType.OP_PUSH);
        this.arg = arg;
    }
}
