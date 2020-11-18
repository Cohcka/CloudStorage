import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.*;
import java.util.stream.Stream;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Core {
    private static String loginTmp = "login";
    private static String passwordTmp = "Pass";
    private static final Path root = Paths.get("SrvStorage");

    Core(){
        try {
            run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new AuthHandler());
                        }
                    });
            ChannelFuture f = b.bind(8888).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private static class AuthHandler extends ChannelInboundHandlerAdapter {
        private boolean auth = false;

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf buf = (ByteBuf) msg;
            byte codeByte = buf.readByte();

            if (auth) {
                ctx.fireChannelRead(buf);
                return;
            } else if (codeByte == Patterns.LOGINCODE) {
                int reqLen;
                reqLen = buf.readInt();
                byte[] ll = new byte[reqLen];
                buf.readBytes(ll);

                reqLen = buf.readInt();
                byte[] pp = new byte[reqLen];
                buf.readBytes(pp);
                String login = new String(ll);
                String password = new String(pp);

                System.out.println(login);
                System.out.println(password);

                if (loginTmp.equals(login) & passwordTmp.equals(password)){
                    System.out.println("Ok");
                    auth = true;
                    ctx.pipeline().addLast(new MainHandler(login));
                    ctx.pipeline().remove(this);
                } else {
                    System.out.println("Wrong log/pass");
                }
            }


        }
    }

    private static class MainHandler extends ChannelInboundHandlerAdapter {
        private String username;

        public MainHandler(String username) {
            this.username = username;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            checkCommand((ByteBuf) msg);
            //ctx.writeAndFlush((byte) 1); // ответное сообщение
        }
    }

    public static void checkCommand(ByteBuf command) throws UnsupportedEncodingException {
        byte[] tmp;
        switch(command.readByte()) {
            case Patterns.CHANGEDIR:
                int pathCDLen = command.readInt();
                tmp = new byte[pathCDLen];
                command.readBytes(tmp);
                String pathChange = new String(tmp, "UTF-8");
                return;
            case Patterns.UPLOADFILE:
                int pathDWFLen = command.readInt();
                tmp = new byte[pathDWFLen];
                command.readBytes(tmp);
                String fileName = new String(tmp, "UTF-8");
                if (Files.exists(Paths.get(fileName))) {
                    System.out.println("File already exists");
                    while(command.isReadable()){
                        command.readByte();
                    }
                    //отправлять сообщение клиенту чтобы он не продолжал слать файл
                } else {
                    try {
                        Long fileSize = command.readLong();
                        Long countPack = fileSize/256;
                        if(fileSize % 256 != 0){countPack++;}
                        System.out.println("count pack: "+countPack);
                        Long readPack = 0l;
                        System.out.println("filesize: "+fileSize);
                        Files.createFile(Paths.get(fileName));
                        tmp = new byte[256];
                        while(readPack<countPack){
                            //tmp = new byte[fileSize.intValue()];
                            //command.readBytes(tmp);
                            //Files.write(Paths.get(fileName),tmp);
                            if(readPack+1==countPack){
                                System.out.println("Readpack: "+readPack);
                                System.out.println("last pack bytes: "+(fileSize-(readPack*256)));
                                tmp = new byte[(int)(fileSize-(readPack*256))];
                            }
                            if(command.readableBytes()>=tmp.length){
                                command.readBytes(tmp);
                            }
                            readPack++;
                            Files.write(Paths.get(fileName),tmp,StandardOpenOption.APPEND);
                        }
                        System.out.println("Upload file: " + fileName+" done");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return;
            case Patterns.DOWNLOADFILE:
                int pathUPFLen = command.readInt();
                tmp = new byte[pathUPFLen];
                command.readBytes(tmp);
                String pathDWF = new String(tmp, "UTF-8");
                //передача байтов самого файла
                return;
            case Patterns.DELETEFILE:
                int pathDFLen = command.readInt();
                tmp = new byte[pathDFLen];
                command.readBytes(tmp);
                String pathDF = new String(tmp, "UTF-8");
                try {
                    Files.delete(Paths.get(pathDF));
                    System.out.println("File: " + pathDF +" deleted ");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            case Patterns.GETFILELIST:
                int pathFLLen = command.readInt();
                tmp = new byte[pathFLLen];
                command.readBytes(tmp);
                String pathFL = new String(tmp, "UTF-8");
                try {
                Stream<Path> dir = Files.list(Paths.get(pathFL));
                Object[] dir2 = dir.toArray();
                    for (Object obj : dir2) {
                        System.out.println(obj.toString().replace(root+"/",""));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            default:
                System.out.println("Wrong command");
        }
    }
}