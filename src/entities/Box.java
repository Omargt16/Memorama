package entities;

public class Box {

    public boolean Open;
    public String Value;

    public Box(char value) {
        Open = false;
        Value = String.valueOf(value);
    }

    @Override
    public String toString() {
        return Open ? Value : "-";
    }

    @Override
    public boolean equals(Object obj) {
        return Value.equals(((Box) obj).Value);
    }
}
