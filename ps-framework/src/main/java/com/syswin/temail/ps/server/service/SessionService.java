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

package com.syswin.temail.ps.server.service;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import io.netty.channel.Channel;

/**
 * 会话管理服务
 *
 * @see AbstractSessionService
 */
public interface SessionService {

  /**
   * 客户端请求登录接口
   *
   * @param channel 当前客户端连接的通道
   * @param reqPacket 登录请求数据包
   */
  void login(Channel channel, CDTPPacket reqPacket);

  /**
   * 客户端请求前将通道绑定到会话中。
   *
   * @param channel 当前客户端连接的通道
   * @param reqPacket 登录请求数据包
   */
  void bind(Channel channel, CDTPPacket reqPacket);

  /**
   * 客户端请求登出接口
   *
   * @param channel 当前客户端连接的通道
   * @param reqPacket 登出请求数据包
   */
  void logout(Channel channel, CDTPPacket reqPacket);

  /**
   * 客户端通道断开(主动或者被动)时，需要处理的服务，如移除该通道相关的会话
   *
   * @param channel 当前客户端连接的通道
   */
  void disconnect(Channel channel);
}
