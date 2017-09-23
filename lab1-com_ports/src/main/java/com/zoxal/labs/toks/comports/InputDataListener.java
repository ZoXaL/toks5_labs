package com.zoxal.labs.toks.comports;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortPacketListener;
import javafx.application.Platform;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class InputDataListener implements SerialPortPacketListener {
    // using utf-16 to guarantee 2-byte symbol size
    public static final Charset TRANSPORT_ENCODING = StandardCharsets.UTF_16;
    private Consumer<String> dataConsumer;

    public InputDataListener(Consumer<String> dataConsumer) {
        this.dataConsumer = dataConsumer;
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        byte[] data = serialPortEvent.getReceivedData();
        String result = new String(data, TRANSPORT_ENCODING);
        Platform.runLater(() -> {dataConsumer.accept(result);});
    }

    @Override
    public int getPacketSize() {
        return 2;
    }
}
