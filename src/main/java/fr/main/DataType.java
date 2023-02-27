package fr.main;

public enum DataType {
    INT("`INTEGER`"),
    PTR("`POINTER`"),
    BOOL("`BOOLEAN`");

    private final String value;

    DataType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
