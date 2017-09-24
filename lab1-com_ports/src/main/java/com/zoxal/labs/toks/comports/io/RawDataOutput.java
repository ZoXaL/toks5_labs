package com.zoxal.labs.toks.comports.io;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Interface for any com-port consumer.
 */
@FunctionalInterface
public interface RawDataOutput {
    // using utf-16 to guarantee 2-byte symbol size
    Charset DEFAULT_TRANSPORT_ENCODING = StandardCharsets.UTF_16;

    void write(byte[] buffer, long bufferSize);
}
