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

package com.syswin.temail.ps.server.service.channels.strategy.one2one;

import static java.util.Collections.emptyList;

import com.syswin.temail.ps.server.entity.Session;
import io.netty.channel.Channel;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class DeviceChannelHolder {

  // very unlikely to have concurrent device IDs situation, since a single device has to make 2 connections at the same time
  // and requests on a single channel are handled serially.
  private final Map<Channel, String> channelDevIdMap = new ConcurrentHashMap<>();
  private final Map<String, ChannelSessionsBinder> devIdBinderMap = new ConcurrentHashMap<>();

  Collection<Session> addSession(String temail,
      String deviceId,
      Channel channel,
      BiConsumer<Channel, Session> existingChannelHandler,
      BiConsumer<Channel, Collection<Session>> replacedChannelHandler) {

    ChannelSessionsBinder currentBinder = devIdBinderMap
        .computeIfAbsent(deviceId, t -> new ChannelSessionsBinder(channel));

    if (channel.equals(currentBinder.getChannel())) {
      Session session = currentBinder.addSession(temail, deviceId);
      channelDevIdMap.put(channel, deviceId);
      existingChannelHandler.accept(channel, session);
      log.info("Added temail {} device {} channel {} mapping", temail, deviceId, channel);
      return Collections.emptyList();
    }

    bindToNewChannel(temail, deviceId, channel);

    Channel oldChannel = currentBinder.getChannel();
    channelDevIdMap.remove(oldChannel);
    channelDevIdMap.put(channel, deviceId);

    // close before cleaning up temail:channel mapping to avoid temail binding on old channel again on request
    oldChannel.close();
    log.info("Closed and replaced channel {} with {} in temail {} device {} mapping due to new connection from this device",
        oldChannel,
        channel,
        temail,
        deviceId);

    Collection<Session> sessionsExpired = currentBinder.getSessions();
    sessionsExpired.removeIf(session -> isSameSession(temail, deviceId, session));
    replacedChannelHandler.accept(channel, sessionsExpired);

    return sessionsExpired;
  }

  private Session bindToNewChannel(String temail, String deviceId, Channel channel) {
    ChannelSessionsBinder binder = new ChannelSessionsBinder(channel);
    Session session = binder.addSession(temail, deviceId);
    devIdBinderMap.put(deviceId, binder);
    return session;
  }

  void removeSession(String temail, String deviceId) {
    ChannelSessionsBinder binder = devIdBinderMap.get(deviceId);
    if (binder != null) {
      binder.getSessions()
          .removeIf(session -> isSameSession(temail, deviceId, session));
    }
  }

  Collection<Session> removeChannel(Channel channel) {
    String devId = channelDevIdMap.remove(channel);
    if (devId == null) {
      return emptyList();
    }
    ChannelSessionsBinder binder = devIdBinderMap.remove(devId);

    return binder != null ? binder.getSessions() : emptyList();
  }

  private boolean isSameSession(String temail, String deviceId, Session session) {
    return session.getDeviceId().equals(deviceId)
        && session.getTemail().equals(temail);
  }
}