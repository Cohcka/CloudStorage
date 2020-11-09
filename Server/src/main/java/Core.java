import java.io.IOException;
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
    private static String login = "login";
    private static String password = "Pass";
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

    public void checkCommand(ByteBuffer command){
        switch(command.[0]){
            case Patterns.LOGINCODE:
                int state = 1;
                if(state == 1){

                }
            case Patterns.PASSWORDCODE:
            case Patterns.CHANGEDIR:
            case Patterns.DOWNLOADFILE:
            case Patterns.UPLOADFILE:
            case Patterns.DELETEFILE:
        }
    }

}
