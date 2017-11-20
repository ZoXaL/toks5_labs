package com.zoxal.labs.toks.packages.io;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortPacketListener;
import com.zoxal.labs.toks.comports.ComPortUtils;
import com.zoxal.labs.toks.comports.io.ComPortConnectionAlgorithm;
import com.zoxal.labs.toks.comports.io.ComPortOutput;
import com.zoxal.labs.toks.comports.io.DebugOutput;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Algorithm of package connection process.
 * Performs handshake to exchange peers addresses.
 *
 * @author Mike
 * @version 15/10/2017
 */
public class PackageComPortConnectionAlgorithm extends ComPortConnectionAlgorithm implements SerialPortPacketListener {
    private static final Logger log = LoggerFactory.getLogger(PackageComPortConnectionAlgorithm.class);
    protected Alert connectingAlert = new Alert(Alert.AlertType.NONE);
    protected ButtonType buttonTypeCancel;
    protected byte portAddress;
    protected PackageComPortOutput output;
    protected volatile boolean connectionEstablished = false;

    public PackageComPortConnectionAlgorithm(
            SerialPort port, DebugOutput debugOutput, Consumer<ComPortOutput> onSuccess, Runnable onError) {
        super(port, debugOutput, onSuccess, onError);

        portAddress = ComPortUtils.getPortAddress(port);

        connectingAlert.setTitle("Connecting to port");
        connectingAlert.setContentText("Connecting to " + port.getSystemPortName() + "...");
        buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.BACK_PREVIOUS);
        connectingAlert.getButtonTypes().setAll(buttonTypeCancel);
        connectingAlert.setOnCloseRequest((event -> {
            if (!connectionEstablished) {
                cancelConnection();
            }
        }));
    }

    /**
     * Tries to connect to peer.
     * Initializes output. Shows information alert during
     * connection process.
     */
    @Override
    public void connect() {
        if (!port.openPort()) {
            cancelConnection();
            return;
        }
        Platform.runLater(() ->
            connectingAlert.showAndWait()
        );
        output = new PackageComPortOutput();
        output.setDebugOutput(debugOutput);
        output.setOriginalAddress(portAddress);
        output.setComPortOutput(port::writeBytes);

        DataPackage connectionPackage = new DataPackage();
        connectionPackage.setFrom(portAddress);
        connectionPackage.calculateFCS();
        port.removeDataListener();
        port.addDataListener(this);

        output.sendPackage(connectionPackage);      // first handshaking message
    }

    @Override
    public int getPacketSize() {
        return DataPackage.PACKAGE_SIZE;
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
    }

    /**
     * Catches peer's handshake message and sends handshake response.
     * @param event     event with received data
     */
    @Override
    public void serialEvent(SerialPortEvent event) {
        if (!connectionEstablished) {
            byte[] handshakePackageBytes = event.getReceivedData();
            DataPackage handshakePackage = DataPackage.fromByteArray(handshakePackageBytes);
            output.setDestinationAddress(handshakePackage.getFrom());       // setting peer address
            log.debug("Got handshake from {}", handshakePackage.getFrom());

            DataPackage connectionAcceptPackage = new DataPackage();
            connectionAcceptPackage.setTo(handshakePackage.getFrom());
            connectionAcceptPackage.setFrom(portAddress);
            output.sendPackage(connectionAcceptPackage);                    // second handshaking package

            Platform.runLater(() -> {
                connectingAlert.hide();
                onSuccess.accept(output);
                log.debug("Connection established from {} to {}", portAddress, handshakePackage.getFrom());
            });
        }
        connectionEstablished = true;
    }

    /**
     * Cancel connection process and hides connection alert.
     */
    protected void cancelConnection() {
        log.debug("Connection request has been cancelled");
        connectingAlert.hide();
        if (port.isOpen()) port.closePort();
        onError.run();
    }
}
