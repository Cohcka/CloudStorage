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

    void authSend(String log, String pass){
        byte[] msg = (Patterns.LOGINCODE + log.length() + log + pass.length() + pass).getBytes();
        try {
            buf.put(msg);
            buf.flip();
            channel.write(buf);
            buf.rewind();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
