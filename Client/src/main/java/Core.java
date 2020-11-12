import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Core {
    private SocketChannel channel;
    private ByteBuffer buf = ByteBuffer.allocate(256);

    Core(){
        startConnect();
    }

    private void startConnect(){
        try {
            channel = SocketChannel.open();
            channel.connect(new InetSocketAddress(8888));
            channel.configureBlocking(false);
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    void sendCommand(Byte code, String[] msg){
        buf.put(code);
        for (int i = 0; i < msg.length; i++) {
            buf.putInt(msg[i].getBytes().length);;
            buf.put(msg[i].getBytes());
        }
        try {
            buf.flip();
            channel.write(buf);
            buf.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
