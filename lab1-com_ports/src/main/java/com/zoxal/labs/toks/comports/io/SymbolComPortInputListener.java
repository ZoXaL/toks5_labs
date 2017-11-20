package com.zoxal.labs.toks.comports.io;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.application.Platform;

import java.util.function.Consumer;

public class SymbolComPortInputListener extends ComPortInputListener {

    public SymbolComPortInputListener(Consumer<String> inputDataConsumer) {
        super(inputDataConsumer);
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (inputDataConsumer == null) {
            throw new IllegalStateException("SymbolComPortInputListener is configured wrong: no inputDataConsumer");
        }
        byte[] data = serialPortEvent.getReceivedData();
        String result = new String(data, ComPortOutput.DEFAULT_TRANSPORT_ENCODING);
        Platform.runLater(() -> inputDataConsumer.accept(result));
    }
}
