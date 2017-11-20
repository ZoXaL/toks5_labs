package com.zoxal.labs.toks.comports.io;

/**
 * Allows to add specific logic for com-port output, for example,
 * message packaging.
 */
public abstract class ComPortOutput implements RawDataOutput {
    protected RawDataOutput rawOutput;
    protected DebugOutput debugOutput;

    @Override
    public void writeBytes(byte[] buffer, long bufferSize) {
        if (rawOutput == null) {
            throw new IllegalStateException("ComPortOutput is configured wrong: "
                    + "no underlying output");
        }
        rawOutput.writeBytes(buffer, bufferSize);
    }

    public void setDebugOutput(DebugOutput debugOutput) {
        this.debugOutput = debugOutput;
    }

    public void setComPortOutput(RawDataOutput rawOutput) {
        this.rawOutput = rawOutput;
    }
}
