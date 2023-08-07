package entities;

import java.nio.channels.SocketChannel;

public class Player {

    public int score;
    public SocketChannel channel;

    public Player() {
        this.score = -1;
    }

    public Player(SocketChannel channel) {
        this.channel = channel;
        this.score = 0;
    }

    public int getPort() {
        return channel.socket().getPort();
    }
}
