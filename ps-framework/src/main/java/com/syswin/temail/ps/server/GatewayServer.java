/*
 * MIT License
 *
 * Copyright (c) 2019 Syswin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.syswin.temail.ps.server;

import com.syswin.temail.ps.common.Constants;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.server.handler.HeartbeatAwarePacketHandler;
import com.syswin.temail.ps.server.handler.IdleHandler;
import com.syswin.temail.ps.server.handler.PacketHandler;
import com.syswin.temail.ps.server.service.HeartBeatService;
import com.syswin.temail.ps.server.service.RequestService;
import com.syswin.temail.ps.server.service.SessionService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatewayServer {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final IdleHandler idleHandler;
  private final PacketHandler packetHandler;
  private final int port;
  private final int idleTimeSeconds;
  private final Supplier<MessageToByteEncoder<CDTPPacket>> packetEncoderSupplier;
  private final Supplier<ByteToMessageDecoder> packetDecoderSupplier;
  private final boolean enableEpoll;

  public GatewayServer(SessionService sessionService,
      RequestService requestService,
      Supplier<MessageToByteEncoder<CDTPPacket>> packetEncoderSupplier,
      Supplier<ByteToMessageDecoder> packetDecoderSupplier,
      int port,
      int idleTimeSeconds) {

    this(sessionService, requestService, packetEncoderSupplier, packetDecoderSupplier, port, idleTimeSeconds, false);
  }

  public GatewayServer(SessionService sessionService,
      RequestService requestService,
      Supplier<MessageToByteEncoder<CDTPPacket>> packetEncoderSupplier,
      Supplier<ByteToMessageDecoder> packetDecoderSupplier,
      int port,
      int idleTimeSeconds,
      boolean enableEpoll) {

    this.idleHandler = new IdleHandler(sessionService, idleTimeSeconds);
    this.packetHandler = new HeartbeatAwarePacketHandler(sessionService, requestService, new HeartBeatService());
    this.packetEncoderSupplier = packetEncoderSupplier;
    this.packetDecoderSupplier = packetDecoderSupplier;
    this.port = port;
    this.idleTimeSeconds = idleTimeSeconds;
    this.enableEpoll = enableEpoll;
  }

  public Stoppable run() {
    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;// ?????? cpu

    if(enableEpoll) {
      bossGroup = new EpollEventLoopGroup(1);
      workerGroup = new EpollEventLoopGroup();
      LOGGER.info("Using epoll event loop group");
    } else {
      bossGroup = new NioEventLoopGroup(1);
      workerGroup = new NioEventLoopGroup();
      LOGGER.info("Using Nio event loop group");
    }

    ServerBootstrap bootstrap = new ServerBootstrap();

    bootstrap.group(bossGroup, workerGroup)
        // ???????????????????????????????????????
        .channel(NioServerSocketChannel.class)
        // ????????????NIO??????Channel
        .localAddress(new InetSocketAddress(port))
        // ??????NoDelay??????Nagle,???????????????????????????
        .childOption(ChannelOption.TCP_NODELAY, true)
        // ?????????????????????
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childHandler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel channel) {
            channel.pipeline()
                .addLast("idleStateHandler", new IdleStateHandler(idleTimeSeconds, 0, 0))
                .addLast("idleHandler", idleHandler)
                .addLast("lengthFieldBasedFrameDecoder",
                    new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, Constants.LENGTH_FIELD_LENGTH, 0, 0))
                .addLast("lengthFieldPrepender",
                    new LengthFieldPrepender(Constants.LENGTH_FIELD_LENGTH, 0, false))
                .addLast("packetEncoder", packetEncoderSupplier.get())
                .addLast("packetDecoder", packetDecoderSupplier.get())
                .addLast("packetHandler", packetHandler);
          }
        });

    // ????????????????????????;??????sync????????????????????????????????????
    bootstrap.bind().syncUninterruptibly();
    LOGGER.info("Temail ??????????????????,????????????{}", port);
    return stoppable(bossGroup, workerGroup);
  }

  private Stoppable stoppable(EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
    return () -> {
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
    };
  }
}
