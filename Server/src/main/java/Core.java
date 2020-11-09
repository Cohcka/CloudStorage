import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ObjectDecoder;

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
        private boolean authOk = false;

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf in = (ByteBuf) msg;
            try {
                while (in.isReadable()) {
                    System.out.print((char) in.readByte());
                }
            } finally {
                in.release();
            }

            /*
            if (authOk) {
                ctx.fireChannelRead(input);
                return;
            }
            if (input[0] == Patterns.LOGINCODE) {
                authOk = true;
                ctx.pipeline().addLast(new MainHandler(login));
                System.out.println("hi");
            }
             */
        }
    }

    private static class MainHandler extends ChannelInboundHandlerAdapter {
        private String username;

        public MainHandler(String username) {
            this.username = username;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            String input = (String) msg;
            System.out.println(username + ": " + input);
        }
    }

    public String[] checkCommand(ByteBuf command) throws UnsupportedEncodingException {
        byte[] tmp;
        switch(command.readByte()){
            case Patterns.LOGINCODE:
                int loginLen = command.readInt();
                tmp = new byte[loginLen];
                command.readBytes(tmp);
                String login = new String(tmp, "UTF-8");
                int passLen = command.readInt();
                tmp = new byte[passLen];
                command.readBytes(tmp);
                String password = new String(tmp, "UTF-8");
            case Patterns.CHANGEDIR:
                int pathCDLen = command.readInt();
                tmp = new byte[pathCDLen];
                command.readBytes(tmp);
                String pathChange = new String(tmp, "UTF-8");
            case Patterns.DOWNLOADFILE:
                int pathDWFLen = command.readInt();
                tmp = new byte[pathDWFLen];
                command.readBytes(tmp);
                String pathDWF = new String(tmp, "UTF-8");
                int fileDWFLen = command.readInt();
                tmp = new byte[fileDWFLen];
                command.readBytes(tmp);
                String filenameDWF = new String(tmp, "UTF-8");
            case Patterns.UPLOADFILE:
                int pathUPFLen = command.readInt();
                tmp = new byte[pathUPFLen];
                command.readBytes(tmp);
                String pathUPF = new String(tmp, "UTF-8");
                int fileUPLen = command.readInt();
                tmp = new byte[fileUPLen];
                command.readBytes(tmp);
                String filenameUP = new String(tmp, "UTF-8");
                //передача байтов самого файла
            case Patterns.DELETEFILE:
                int pathDFLen = command.readInt();
                tmp = new byte[pathDFLen];
                command.readBytes(tmp);
                String pathDF = new String(tmp, "UTF-8");
                int fileDFLen = command.readInt();
                tmp = new byte[fileDFLen];
                command.readBytes(tmp);
                String filenameDF = new String(tmp, "UTF-8");
        }
    }
}