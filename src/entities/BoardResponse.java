package entities;

import java.io.*;

public class BoardResponse implements Serializable {
    public String board;
    public int flag;

    public BoardResponse(String board, int flag) {
        this.board = board;
        this.flag = flag;
    }

    public byte[] toBytes() throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(this);
            return bos.toByteArray();
        }
    }

    public static BoardResponse fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (BoardResponse) ois.readObject();
        }
    }
}
