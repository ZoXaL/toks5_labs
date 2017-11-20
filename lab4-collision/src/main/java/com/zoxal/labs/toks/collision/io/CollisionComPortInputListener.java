package com.zoxal.labs.toks.collision.io;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortPacketListener;
import com.zoxal.labs.toks.collision.CollisionUtils;
import javafx.application.Platform;

import java.util.function.Consumer;

/**
 * Listener is aimed to check JAM signal and detect the collision.
 *
 * @author Mike
 * @version 11/20/2017
 */
public class CollisionComPortInputListener implements SerialPortPacketListener {
    protected Consumer<String> inputDataConsumer;
    protected DebugOutput debugOutput;
    private CollisionComPortOutput comPortOutput;

    public CollisionComPortInputListener(Consumer<String> inputDataConsumer) {
        this.inputDataConsumer = inputDataConsumer;
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getReceivedData()[0] == CollisionUtils.JAM_SIGNAL) {  // collision signal from receiver
            CollisionUtils.notifyCollision();
        } else {
            if (CollisionUtils.checkCollision()) {  // collision detection on receiver
                comPortOutput.writeUnchecked(CollisionUtils.JAM_SIGNAL);
                Platform.runLater(() -> debugOutput.debug("X"));
            } else {
                inputDataConsumer.accept(new String(event.getReceivedData(), CollisionComPortOutput.CHARSET));
            }
        }
    }

    @Override
    public int getPacketSize() {
        return 1;
    }


    public void setDebugOutput(DebugOutput debugOutput) {
        this.debugOutput = debugOutput;
    }

    public void setComPortOutput(CollisionComPortOutput comPortOutput) {
        this.comPortOutput = comPortOutput;
    }
}
