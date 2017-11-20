package com.zoxal.labs.toks.packages.io;

import com.fazecast.jSerialComm.SerialPort;
import com.zoxal.labs.toks.comports.io.*;

import java.util.function.Consumer;

/**
 * Lab 2 implementation of Factory interface.
 *
 * @author Mike
 * @version 10/15/2017
 */
public class PackageIOFactory implements IOFactory {
    @Override
    public ComPortOutput getComPortOutput() {
        return new PackageComPortOutput();
    }

    @Override
    public ComPortInputListener getComPortInputListener(Consumer<String> inputDataConsumer) {
        return new PackageComPortInputListener(inputDataConsumer);
    }

    @Override
    public DebugOutput getDebugOutput() {
        return new DebugOutput();
    }

    @Override
    public ComPortConnectionAlgorithm getComPortConnectionAlgorithm(
            SerialPort port, DebugOutput debugOutput, Consumer<ComPortOutput> onSuccess, Runnable onError) {
        return new PackageComPortConnectionAlgorithm(port, debugOutput, onSuccess, onError);
    }
}
