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
public enum CommandType {
  PING(1),
  PONG(2),
  LOGIN(101),
  LOGOUT(102),

  INTERNAL_ERROR(600),
  ;

  public static final short PING_CODE = PING.code;
  public static final short PONG_CODE = PONG.code;
  public static final short LOGIN_CODE = LOGIN.code;
  public static final short LOGOUT_CODE = LOGOUT.code;
  public static final short INTERNAL_ERROR_CODE = INTERNAL_ERROR.code;

  private final short code;

  CommandType(int code) {
    this.code = (short) code;
  }

  public static CommandType valueOf(short code) {
    for (CommandType commandType : CommandType.values()) {
      if (commandType.getCode() == code) {
        return commandType;
      }
    }
    throw new PacketException("Unsupported command code：" + code);
  }

}
