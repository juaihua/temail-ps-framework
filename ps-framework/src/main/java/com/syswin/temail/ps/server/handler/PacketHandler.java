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

package com.syswin.temail.ps.server.handler;

import static com.syswin.temail.ps.common.Constants.CDTP_VERSION;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.INTERNAL_ERROR;
import static com.syswin.temail.ps.common.entity.CommandType.LOGIN;
import static com.syswin.temail.ps.common.entity.CommandType.LOGOUT;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPServerError;
import com.syswin.temail.ps.common.exception.PacketException;
import com.syswin.temail.ps.server.service.RequestService;
import com.syswin.temail.ps.server.service.SessionService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Sharable
public class PacketHandler extends SimpleChannelInboundHandler<CDTPPacket> {

  private final SessionService sessionService;
  private final RequestService requestService;

  public PacketHandler(
      SessionService sessionService,
      RequestService requestService) {
    this.sessionService = sessionService;
    this.requestService = requestService;
  }

  @Override
  public void channelRead0(ChannelHandlerContext ctx, CDTPPacket packet) {
    try {
      Channel channel = ctx.channel();
      short commandSpace = packet.getCommandSpace();
      short command = packet.getCommand();

      validateHeader(packet);

      if (commandSpace == CHANNEL_CODE) {
        if (command == LOGIN.getCode()) {
          sessionService.login(channel, packet);
        } else if (command == LOGOUT.getCode()) {
          sessionService.logout(channel, packet);
        } else {
          log.warn("Received unknown command {} {}", Integer.toHexString(commandSpace),
              Integer.toHexString(command));
        }
      } else {
        sessionService.bind(channel, packet);
        requestService.handleRequest(packet, msg -> ctx.writeAndFlush(msg, ctx.voidPromise()));
      }
    } catch (Exception e) {
      throw new PacketException(e, packet);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    log.error("Failed to handle packet on channel: {}", ctx.channel(), cause);
    if (ctx.channel().isActive()) {
      CDTPPacket packet;
      if (cause instanceof PacketException && ((PacketException) cause).getPacket() != null) {
        PacketException packetException = (PacketException) cause;
        packet = packetException.getPacket();
      } else {
        CDTPHeader header = new CDTPHeader();
        packet = new CDTPPacket();
        packet.setHeader(header);
        packet.setVersion(CDTP_VERSION);
      }
      packet.setCommandSpace(CHANNEL_CODE);
      packet.setCommand(INTERNAL_ERROR.getCode());
      CDTPServerError.Builder builder = CDTPServerError.newBuilder();
      builder.setCode(INTERNAL_ERROR.getCode());
      if (cause != null) {
        builder.setDesc(cause.getMessage());
      }
      packet.setData(builder.build().toByteArray());
      ctx.writeAndFlush(packet, ctx.voidPromise());
    }
  }

  private void validateHeader(CDTPPacket packet) {
    if (packet.getHeader() == null
        || StringUtil.isNullOrEmpty(packet.getHeader().getDeviceId())
        || StringUtil.isNullOrEmpty(packet.getHeader().getSender())) {
      throw new IllegalArgumentException("Sender and device ID must not be empty");
    }
  }
}
