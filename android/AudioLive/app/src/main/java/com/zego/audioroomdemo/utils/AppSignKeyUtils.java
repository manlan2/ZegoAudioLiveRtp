package com.zego.audioroomdemo.utils;

/**
 * Copyright © 2017 Zego. All rights reserved.<br><br>
 *
 * @author realuei on 2017/7/11.
 */

public class AppSignKeyUtils {
    final static private byte[] signData_rtmp = new byte[] {
            (byte) 0x91, (byte) 0x93, (byte) 0xcc, (byte) 0x66, (byte) 0x2a, (byte) 0x1c, (byte) 0x0e, (byte) 0xc1,
            (byte) 0x35, (byte) 0xec, (byte) 0x71, (byte) 0xfb, (byte) 0x07, (byte) 0x19, (byte) 0x4b, (byte) 0x38,
            (byte) 0x41, (byte) 0xd4, (byte) 0xad, (byte) 0x83, (byte) 0x78, (byte) 0xf2, (byte) 0x59, (byte) 0x90,
            (byte) 0xe0, (byte) 0xa4, (byte) 0x0c, (byte) 0x7f, (byte) 0xf4, (byte) 0x28, (byte) 0x41, (byte) 0xf7
    };

    final static private byte[] signData_udp = new byte[] {
            (byte)0x1e, (byte)0xc3, (byte)0xf8, (byte)0x5c, (byte)0xb2, (byte)0xf2, (byte)0x13, (byte)0x70,
            (byte)0x26, (byte)0x4e, (byte)0xb3, (byte)0x71, (byte)0xc8, (byte)0xc6, (byte)0x5c, (byte)0xa3,
            (byte)0x7f, (byte)0xa3, (byte)0x3b, (byte)0x9d, (byte)0xef, (byte)0xef, (byte)0x2a, (byte)0x85,
            (byte)0xe0, (byte)0xc8, (byte)0x99, (byte)0xae, (byte)0x82, (byte)0xc0, (byte)0xf6, (byte)0xf8
    };

    final static private byte[] signData_international = new byte[] {
            (byte)0x5d, (byte)0xe6, (byte)0x83, (byte)0xac, (byte)0xa4, (byte)0xe5, (byte)0xad, (byte)0x43,
            (byte)0xe5, (byte)0xea, (byte)0xe3, (byte)0x70, (byte)0x6b, (byte)0xe0, (byte)0x77, (byte)0xa4,
            (byte)0x18, (byte)0x79, (byte)0x38, (byte)0x31, (byte)0x2e, (byte)0xcc, (byte)0x17, (byte)0x19,
            (byte)0x32, (byte)0xd2, (byte)0xfe, (byte)0x22, (byte)0x5b, (byte)0x6b, (byte)0x2b, (byte)0x2f
    };

    static public byte[] requestSignKey(long appId) {
        if (appId == 1L) {
            return signData_rtmp;
        } else if (appId == 1739272706L) {
            return signData_udp;
        } else if (appId == 3L) {
            return signData_international;
        }
        return null;
    }
}
