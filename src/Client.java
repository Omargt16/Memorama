import entities.Game;
import entities.MovementRequest;
import entities.BoardResponse;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Client {

    public boolean myTurn = true;
    public BufferedReader reader;

    public static void main(String[] args) {
        new Client("127.0.0.1", 5000);
    }

    public Client(String host, int port) {
        try {
            reader = new BufferedReader(new InputStreamReader(System.in));
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            Selector selector = Selector.open();
            channel.connect(new InetSocketAddress(host, port));
            channel.register(selector, SelectionKey.OP_CONNECT);

            while (true) {
                selector.select();
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey k = it.next();
                    it.remove();
                    if (k.isConnectable()) {
                        SocketChannel ch = (SocketChannel) k.channel();
                        if (ch.isConnectionPending()) {
                            try {
                                ch.finishConnect();
                                System.out.println("Conectado al servidor");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        ch.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        continue;
                    }
                    if (k.isWritable()) {
                        SocketChannel ch2 = (SocketChannel) k.channel();
                        String msj = "join";
                        ByteBuffer b = ByteBuffer.wrap(msj.getBytes());
                        ch2.write(b);
                        k.interestOps(SelectionKey.OP_READ);
                    } else if (k.isReadable()) {
                        SocketChannel ch2 = (SocketChannel) k.channel();
                        ByteBuffer b = ByteBuffer.allocate(2000);
                        b.clear();
                        int n = ch2.read(b);
                        b.flip();

                        BoardResponse boardResponse = BoardResponse.fromBytes(b.array());
                        System.out.println("\n" + boardResponse.board);
                        MovementRequest movementRequest = HandleTurn(boardResponse);
                        if (movementRequest == null)
                            continue;

                        b = ByteBuffer.wrap(movementRequest.toBytes());
                        ch2.write(b);
                        k.interestOps(SelectionKey.OP_READ);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MovementRequest HandleTurn(BoardResponse boardResponse) {
        if (boardResponse.flag == Game.NEW_MOVEMENT) {
            while (true)
                try {
                    System.out.println("Es tu turno, introduce las coordenadas de tu movimiento (f,c f,c)");
                    return new MovementRequest(reader.readLine());
                } catch (Exception e) {
                    System.out.println("Error al leer el movimiento");
                }
        } else if (boardResponse.flag == Game.ANOTHER_PLAYER) {
            System.out.println("Es el turno de otro jugador, espera.");
        } else if (boardResponse.flag == Game.WINNER) {
            System.out.println("Ganador!");
            System.exit(0);
        } else if (boardResponse.flag == Game.LOSER) {
            System.out.println("Perdedor!");
            System.exit(0);
        }
        return null;
    }
}
