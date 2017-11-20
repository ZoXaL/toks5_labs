package com.zoxal.labs.toks.comports.io;

public class SymbolComPortOutput extends ComPortOutput {
    @Override
    public void writeBytes(byte[] buffer, long bufferSize) {
        if (rawOutput == null) {
            throw new IllegalStateException("ComPortOutput is configured wrong: "
                    + "no underlying serial port");
        }
        rawOutput.writeBytes(buffer, bufferSize);
    }
}
