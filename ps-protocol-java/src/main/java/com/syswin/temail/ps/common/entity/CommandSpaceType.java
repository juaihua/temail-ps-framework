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

import com.syswin.temail.ps.common.exception.PacketException;
import lombok.Getter;

@Getter
public enum CommandSpaceType {
  CHANNEL(0),
  SINGLE_MESSAGE(1),
  GROUP_MESSAGE(2),
  SYNC_STATUS(3),
  STRATEGY(4),
  ;

  public static final short CHANNEL_CODE = CHANNEL.code;
  public static final short SINGLE_MESSAGE_CODE = SINGLE_MESSAGE.code;
  public static final short GROUP_MESSAGE_CODE = GROUP_MESSAGE.code;
  public static final short SYNC_STATUS_CODE = SYNC_STATUS.code;
  public static final short STRATEGY_CODE = STRATEGY.code;
  private short code;

  CommandSpaceType(int code) {
    this.code = (short) code;
  }

  public static CommandSpaceType valueOf(short code) {
    for (CommandSpaceType value : values()) {
      if (value.code == code) {
        return value;
      }
    }
    throw new PacketException("Unsupported commandSpace code：" + code);
  }
}
