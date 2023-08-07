package entities;

public class Board {

    private final static int lines = 7;
    private final static int columns = 8;
    private boolean show;
    public Box[][] Boxes;

    public Board() {
        Boxes = new Box[lines][columns];
    }

    public static Board RandomBoard() {
        Board board = new Board();
        int[] charUses = new int[28];

        for (int i = 0; i < lines; i++)
            for (int j = 0; j < columns; j++) {
                int charIndex;
                do {
                    charIndex = (int) (Math.random() * 28);
                } while (charUses[charIndex] >= 2);

                charUses[charIndex]++;
                charIndex += 'A';
                char value = charIndex == 91 ? 'Ñ' : charIndex == 92 ? '*' : (char) charIndex;
                board.Boxes[i][j] = new Box(value);
            }

        return board;
    }

    public boolean checkPair(MovementRequest mr) {
        if (mr.x1 < 0 || mr.x1 >= lines
                || mr.y1 < 0 || mr.y1 >= columns
                || mr.x2 < 0 || mr.x2 >= lines
                || mr.y2 < 0 || mr.y2 >= columns)
            throw new IllegalArgumentException("Coordenadas invalidas");

        if (mr.x1 == mr.x2 && mr.y1 == mr.y2)
            throw new IllegalArgumentException("Las coordenadas no pueden ser iguales");

        Box box1 = Boxes[mr.x1][mr.y1];
        Box box2 = Boxes[mr.x2][mr.y2];
        if (box1.equals(box2)) {
            box1.Open = true;
            box2.Open = true;
            return true;
        }
        return false;
    }

    public boolean isComplete() {
        for (Box[] line : Boxes)
            for (Box box : line)
                if (!box.Open)
                    return false;
        return true;
    }

    public String showOpen() {
        show = true;
        String result = toString();
        show = false;
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" ");
        for (int i = 0; i < columns; i++)
            sb.append(" ").append(i);

        for (int i = 0; i < lines; i++) {
            sb.append("\n").append(i);
            for (Box box : Boxes[i])
                sb.append(" ").append(show ? box.Value : box);
        }
        return sb.toString();
    }
}
