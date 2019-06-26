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

import static com.seanyinx.github.unit.scaffolding.AssertUtils.expectFailing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.exception.PacketException;
import com.syswin.temail.ps.server.service.RequestService;
import com.syswin.temail.ps.server.service.SessionService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class PacketHandlerTest {

  private final ChannelHandlerContext context = Mockito.mock(ChannelHandlerContext.class);
  private final Channel channel = Mockito.mock(Channel.class);

  private final SessionService sessionService = Mockito.mock(SessionService.class);
  private final RequestService requestService = Mockito.mock(RequestService.class);

  private final CDTPPacket packet = new CDTPPacket();
  private final CDTPHeader header = new CDTPHeader();
  private final PacketHandler packetHandler = new PacketHandler(sessionService, requestService);

  @Before
  public void setUp() {
    when(context.channel()).thenReturn(channel);
    packet.setHeader(header);
  }

  @Test
  public void blowsUpIfNoSenderProvider() {
    try {
      header.setDeviceId("iPhoneX");
      packetHandler.channelRead0(context, packet);
      expectFailing(PacketException.class);
    } catch (PacketException e) {
      assertThat(e.getPacket()).isEqualTo(packet);
    }
  }

  @Test
  public void blowsUpIfNoDeviceIdProvider() {
    try {
      header.setSender("sean@t.email");
      packetHandler.channelRead0(context, packet);
      expectFailing(PacketException.class);
    } catch (PacketException e) {
      assertThat(e.getPacket()).isEqualTo(packet);
    }
  }

  @Test
  public void blowsUpIfNoHeader() {
    try {
      packet.setHeader(null);
      packetHandler.channelRead0(context, packet);
      expectFailing(PacketException.class);
    } catch (PacketException e) {
      assertThat(e.getPacket()).isEqualTo(packet);
    }
  }
}