package com.zoxal.labs.toks.comports.io;

/**
 * Allows to add specific logic for com-port output, for example,
 * message packaging.
 */
public abstract class ComPortOutput implements RawDataOutput {
    protected RawDataOutput comPortOutput;
    protected DebugOutput debugOutput;

    public void setDebugOutput(DebugOutput debugOutput) {
        this.debugOutput = debugOutput;
    }

    public void setComPortOutput(RawDataOutput comPortOutput) {
        this.comPortOutput = comPortOutput;
    }
}
