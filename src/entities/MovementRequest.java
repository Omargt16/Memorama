package entities;

import java.io.*;

public class MovementRequest implements Serializable {
    public int x1, y1, x2, y2;

    public MovementRequest() {

    }

    public MovementRequest(String plainRequest) {
        String[] parts = plainRequest.split(" ");
        String[] first = parts[0].split(",");
        String[] second = parts[1].split(",");

        this.x1 = Integer.parseInt(first[0]);
        this.y1 = Integer.parseInt(first[1]);
        this.x2 = Integer.parseInt(second[0]);
        this.y2 = Integer.parseInt(second[1]);
    }

    public byte[] toBytes() throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(this);
            return bos.toByteArray();
        }
    }

    public static MovementRequest fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (MovementRequest) ois.readObject();
        }
    }
}
