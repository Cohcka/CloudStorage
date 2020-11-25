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
    private ByteBuffer buf = ByteBuffer.allocate(Patterns.BYTESIZE);
    private final Path clientRoot = Paths.get("ClientStorage");
    private final Path serverRoot = Paths.get("SrvStorage"); // заменить
    private long fileSize = 0l;
    FileChannel fl = null;

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
        try {
            fl = new RandomAccessFile(pathUpl, "r").getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        buf.clear();
        buf.put(Patterns.UPLOADFILE);
        buf.putInt(pathDown.getBytes().length);
        buf.put(pathDown.getBytes());

        try {
            long fileSize = Files.size(Paths.get(pathUpl));
            buf.putLong(fileSize);
            buf.flip();
            channel.write(buf);
            System.out.println("Client start transfer file");
            long countPack = fileSize/Patterns.BYTESIZE;
            if(countPack==0 || fileSize % Patterns.BYTESIZE != 0){countPack++;}
            System.out.println("count pack: "+countPack);
            long sendPack = 0l;
            System.out.println("filesize: "+fileSize);

            while(sendPack<countPack){
                buf.clear();
                System.out.println("Sendpack: "+sendPack+" / "+countPack);
                System.out.println(sendPack/(double) (countPack/100)+" %");
                if(sendPack+1==countPack){
                    System.out.println("last pack bytes: "+(fileSize-(sendPack*Patterns.BYTESIZE)));
                    buf = ByteBuffer.allocate((int)(fileSize-(sendPack*Patterns.BYTESIZE)));
                }
                fl.read(buf);
                buf.flip();
                channel.write(buf);
                sendPack++;
            }
            buf.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Client finished transfer file");
    }

    void downloadFile(String fileName){
        String pathUpl = Paths.get(serverRoot.toString(), fileName).toString();
        String pathDwn = Paths.get(clientRoot.toString(), fileName).toString();
        sendCommand(Patterns.DOWNLOADFILE, pathUpl);
        try {
            buf.clear();
            while (buf.position()<8){
                channel.read(buf);
            }
            buf.flip();
            fileSize = buf.getLong();
            if(fileSize!=0){
                System.out.println("in");
                System.out.println(fileSize);
                Files.createFile(Paths.get(pathDwn));
                fl = new RandomAccessFile(pathDwn, "rw").getChannel();
                long upBytes = 0;
                buf.compact();
                while(upBytes<fileSize){
                    if(fileSize - upBytes < Patterns.BYTESIZE){
                        buf.limit((int)(fileSize - upBytes));
                    }
                    channel.read(buf);
                    upBytes += buf.position();
                    buf.flip();
                    fl.write(buf);
                    buf.clear();
                }
                buf.limit(Patterns.BYTESIZE);
            } else {
                System.out.println("FILE NOT FOUND");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void sendCommand(Byte code, String[] msg){
        buf.clear();
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
        buf.clear();
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
