package com.zoxal.labs.toks.comports.io;

import com.fazecast.jSerialComm.SerialPort;

import java.util.function.Consumer;

public class SymbolIOFactory implements IOFactory {
    @Override
    public ComPortOutput getComPortOutput() {

        return new SymbolComPortOutput();
    }

    @Override
    public ComPortInputListener getComPortInputListener(Consumer<String> inputDataConsumer) {
        return new SymbolComPortInputListener(inputDataConsumer);
    }

    @Override
    public DebugOutput getDebugOutput() {
        return new DebugOutput();
    }

    @Override
    public ComPortConnectionAlgorithm getComPortConnectionAlgorithm(
            SerialPort port, DebugOutput debugOutput, Consumer<ComPortOutput> onSuccess, Runnable onError) {
        return new SymbolComPortConnectionAlgorithm(port, debugOutput, onSuccess, onError);
    }
}
