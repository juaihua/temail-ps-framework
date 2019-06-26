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

package com.syswin.temail.ps.common.entity;

import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.INTERNAL_ERROR_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.PING_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.PONG_CODE;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class CDTPPacket {

  private short commandSpace;
  private short command;
  private short version;
  private CDTPHeader header;
  private byte[] data;

  public CDTPPacket() {
  }

  public CDTPPacket(CDTPPacket other) {
    commandSpace = other.commandSpace;
    command = other.command;
    version = other.version;
    if (other.header != null) {
      header = other.header.clone();
    }
    if (other.data != null) {
      data = Arrays.copyOf(other.data, other.data.length);
    }
  }

  public boolean isHeartbeat() {
    return commandSpace == CHANNEL_CODE &&
        (command == PING_CODE || command == PONG_CODE);
  }

  public boolean isInternalError() {
    return commandSpace == CHANNEL_CODE &&
        (command == INTERNAL_ERROR_CODE);
  }
}
