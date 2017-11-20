package com.zoxal.labs.toks.comports.io;

import com.fazecast.jSerialComm.SerialPort;

import java.util.function.Consumer;

public abstract class ComPortConnectionAlgorithm {
    protected Consumer<ComPortOutput> onSuccess;
    protected Runnable onError;
    protected SerialPort port;
    protected DebugOutput debugOutput;

    public ComPortConnectionAlgorithm(SerialPort port, DebugOutput debugOutput, Consumer<ComPortOutput> onSuccess, Runnable onError) {
        this.onSuccess = onSuccess;
        this.onError = onError;
        this.port = port;
        this.debugOutput = debugOutput;
    }

    public abstract void connect();
}
