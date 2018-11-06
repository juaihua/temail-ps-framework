package com.syswin.temail.ps.common.packet;

import static com.syswin.temail.ps.common.entity.DataEncryptType.NONE;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.DataEncryptType;

/**
 * @author 姚华成
 * @date 2018-11-05
 */
public abstract class AbstractPacketEncryptor implements PacketEncryptor {

  /**
   * 根据用户ID获取对应密钥进行加密
   *
   * @param userId 账户ID e.g. temail地址
   * @param data 待加密的明文
   * @return 明文加密后对应的密文Base64编码
   */
  public abstract byte[] encrypt(String userId, byte[] data, DataEncryptType dataEncryptType);

  @Override
  public void encrypt(CDTPPacket packet) {
    encrypt(packet, getDefaultDataEncryptType());
  }

  @Override
  public void encrypt(CDTPPacket packet, DataEncryptType dataEncryptType) {
    byte[] data;
    CDTPHeader header;
    if (dataEncryptType != NONE &&
        packet != null &&
        ((data = packet.getData()) != null && data.length != 0) &&
        (header = packet.getHeader()) != null) {
      packet.setData(encrypt(header.getReceiver(), data, dataEncryptType));
      header.setDataEncryptionMethod(dataEncryptType.getCode());
    }
  }
}