import entities.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;

public class Server {

    public int currentPlayer;
    public Player[] players;
    public Board board;
    public ServerSocketChannel serverChannel;
    public boolean gameOver;
    public int winnerPort = -1;


    public static void main(String[] args) throws Exception {
        int numPlayers = askNumPlayers();
        new Server(5000, numPlayers);
    }

    public Server(int port, int numPlayers) {
        players = new Player[numPlayers];
        board = Board.RandomBoard();

        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            serverChannel.socket().bind(new InetSocketAddress(port));
            Selector selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Esperando jugadores...");
            waitForPlayers(selector);

            System.out.println("\n- Iniciando juego -\n");
            System.out.println(board.showOpen());
            while (!board.isComplete()) {
                gameLoop(selector);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void gameLoop(Selector selector) throws IOException {
        selector.select();
        Iterator<SelectionKey> it = selector.selectedKeys().iterator();
        while (it.hasNext()) {
            SelectionKey key = it.next();
            it.remove();
            if (key.isReadable()) {
                SocketChannel channel = (SocketChannel) key.channel();
                ByteBuffer buffer = ByteBuffer.allocate(2000);
                buffer.clear();
                int n = channel.read(buffer);
                buffer.flip();
                byte[] bytes = buffer.array();

                try {
                    MovementRequest mr = MovementRequest.fromBytes(bytes);
                    if (board.checkPair(mr)) {
                        players[currentPlayer].score++;
                        System.out.println("Pareja encontrada, puntaje del jugador " + currentPlayer + ": " + players[currentPlayer].score);

                        if (board.isComplete()) {
                            int winner = -1;
                            gameOver = true;
                            for (int i = 0; i < players.length; i++)
                                if (winner == -1 || players[i].score > players[winner].score)
                                    winner = i;
                            winnerPort = players[winner].getPort();

                            System.out.println("\n- Juego terminado -\n");
                            System.out.println("El ganador es el jugador: " + winner);
                        }
                    } else {
                        currentPlayer = currentPlayer == players.length - 1 ? 0 : currentPlayer + 1;
                        System.out.println("Pareja no encontrada, turno del jugador " + currentPlayer);
                    }

                    for (int i = 0; i < players.length; i++) {
                        System.out.println("Puntaje del jugador " + i + ": " + players[i].score);
                        send(players[i].channel);
                    }
                } catch (Exception e) {
                    send(channel);
                }

            }
        }
    }

    public void send(SocketChannel channel) throws IOException {
        int flag = getFlag(channel.socket().getPort());
        BoardResponse boardResponse = new BoardResponse(board.toString(), flag);
        ByteBuffer b = ByteBuffer.wrap(boardResponse.toBytes());
        channel.write(b);
    }

    public int getFlag(int port) {
        if (gameOver)
            if (port == winnerPort)
                return Game.WINNER;
            else
                return Game.LOSER;
        else if (port == players[currentPlayer].getPort())
            return Game.NEW_MOVEMENT;
        else
            return Game.ANOTHER_PLAYER;
    }


    public void waitForPlayers(Selector selector) throws IOException {
        int count = 0;
        while (count < players.length) {
            selector.select();
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey k = it.next();
                it.remove();
                if (k.isAcceptable()) {
                    SocketChannel channel = serverChannel.accept();
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_READ);
                    players[count] = new Player(channel);
                    count++;
                    System.out.println("Nuevo jugador aceptado (" + count + "/" + players.length + ")");
                }
            }
        }
    }

    public static int askNumPlayers() throws Exception {
        Scanner s = new Scanner(System.in);
        System.out.println("Introduzca el número de jugadores:");
        int num = s.nextInt();
        if (num < 1)
            throw new Exception("El número de jugadores debe ser mayor o igual a 2");

        return num;
    }
}
