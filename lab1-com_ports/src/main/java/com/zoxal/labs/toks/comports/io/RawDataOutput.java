package com.zoxal.labs.toks.comports.io;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@FunctionalInterface
public interface RawDataOutput {
    Charset DEFAULT_TRANSPORT_ENCODING = StandardCharsets.UTF_8;

    void writeBytes(byte[] data, long dataLength);
}
