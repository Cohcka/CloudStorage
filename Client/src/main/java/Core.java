import com.sun.xml.internal.stream.util.BufferAllocator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Core {
    private SocketChannel channel;
    private ByteBuffer buf = ByteBuffer.allocate(256);
    private final Path clientRoot = Paths.get("ClientStorage");
    private final Path serverRoot = Paths.get("SrvStorage"); // заменить
    //private Path clientPath;
    //private Path serverPath;

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

    void auth(String login, String pass){
        sendCommand(Patterns.LOGINCODE, new String[] {login,pass});
    }

    void updateFileList(String path){
        sendCommand(Patterns.GETFILELIST, path);
    }

    void deleteFile(String path){
        sendCommand(Patterns.DELETEFILE, path);
    }

    void uploadFile(String fileName){
        String pathUpl = Paths.get(clientRoot.toString(), fileName).toString();
        String pathDown = Paths.get(serverRoot.toString(), fileName).toString();
        FileChannel fl = null;
        try {
            fl = new RandomAccessFile(pathUpl, "r").getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        buf.put(Patterns.UPLOADFILE);
        buf.putInt(pathDown.getBytes().length);
        buf.put(pathDown.getBytes());

        try {
            long fileSize = Files.size(Paths.get(pathUpl));
            buf.putLong(fileSize);
            buf.flip();
            channel.write(buf);
            System.out.println("Client start transfer file");
            Long countPack = fileSize/256;
            if(fileSize % 256 != 0){countPack++;}
            System.out.println("count pack: "+countPack);
            Long sendPack = 0l;
            System.out.println("filesize: "+fileSize);

            //buf.put("TransFerMan".getBytes());
            //buf.flip();
            //channel.write(buf);

            while(sendPack<countPack){
                buf.clear();
                System.out.println("Sendpack: "+sendPack);
                if(sendPack+1==countPack){
                    System.out.println("last pack bytes: "+(fileSize-(sendPack*256)));
                    buf = ByteBuffer.allocate((int)(fileSize-(sendPack*256)));
                }
                fl.read(buf);
                buf.flip();
                channel.write(buf);
                sendPack++;
            }
            //buf = ByteBuffer.allocate(256);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Client finished transfer file");
    }

    void downloadFile(String fileName){
        sendCommand(Patterns.DOWNLOADFILE, fileName);
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

    void sendCommand(Byte code, String path){
        buf.put(code);
        buf.putInt(path.getBytes().length);
        buf.put(path.getBytes());
        try {
            buf.flip();
            channel.write(buf);
            buf.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
