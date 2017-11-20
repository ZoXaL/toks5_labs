package com.zoxal.labs.toks.comports.io;

import com.fazecast.jSerialComm.SerialPort;

import java.util.function.Consumer;

public interface IOFactory {
    ComPortOutput getComPortOutput();
    ComPortInputListener getComPortInputListener(Consumer<String> inputDataConsumer);
    DebugOutput getDebugOutput();
    ComPortConnectionAlgorithm getComPortConnectionAlgorithm(
            SerialPort port, DebugOutput debugOutput, Consumer<ComPortOutput> onSuccess, Runnable onError);
}
