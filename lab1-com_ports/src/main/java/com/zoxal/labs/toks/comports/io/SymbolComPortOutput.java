package com.zoxal.labs.toks.comports.io;

public class SymbolComPortOutput extends ComPortOutput {
    @Override
    public void write(byte[] buffer, long bufferSize) {
        if (comPortOutput == null) {
            throw new IllegalStateException("ComPortOutput is configured wrong: "
                    + "no underlying comPortOutput");
        }
        comPortOutput.write(buffer, bufferSize);
    }
}
