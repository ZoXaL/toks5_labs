package com.zoxal.labs.toks.comports.io;


import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortPacketListener;

import java.util.function.Consumer;

public abstract class ComPortInputListener implements SerialPortPacketListener {
    protected Consumer<String> inputDataConsumer;
    protected DebugOutput debugOutput;

    public ComPortInputListener(Consumer<String> inputDataConsumer) {
        this.inputDataConsumer = inputDataConsumer;
    }


    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
    }

    @Override
    public int getPacketSize() {
        return 2;
    }


    public void setDebugOutput(DebugOutput debugOutput) {
        this.debugOutput = debugOutput;
    }

    public void setInputDataConsumer(Consumer<String> inputDataConsumer) {
        this.inputDataConsumer = inputDataConsumer;
    }
}
