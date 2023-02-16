package main.fr.lexing;

public class Location {
    private final String filepath;
    private final int line;
    private final int column;

    public Location(String filepath, int line, int column) {
        this.filepath = filepath;
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return "[" + filepath + ":" + line + ":" + column + "]";
    }
}
