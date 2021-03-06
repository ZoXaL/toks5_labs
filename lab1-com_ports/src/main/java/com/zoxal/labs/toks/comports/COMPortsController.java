package com.zoxal.labs.toks.comports;

import com.fazecast.jSerialComm.SerialPort;
import com.zoxal.labs.toks.comports.io.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import static com.zoxal.labs.toks.comports.io.ComPortOutput.DEFAULT_TRANSPORT_ENCODING;


public class COMPortsController implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(COMPortsController.class);
    @FXML
    private ComboBox<SerialPort> availablePorts;
    @FXML
    private ToggleButton connectButton;
    @FXML
    private Button sendButton;
    @FXML
    private TextArea debugArea;
    @FXML
    private TextArea inputArea;
    @FXML
    private TextArea outputArea;

    private SerialPort connectedPort;
    private ComPortOutput comPortOutput;
    private DebugOutput debugOutput;
    private IOFactory ioFactory;

    public void setIOFactory(IOFactory ioFactory) {
        this.ioFactory = ioFactory;

        debugOutput = ioFactory.getDebugOutput();
        debugOutput.setDebugConsumer(debugArea::appendText);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // retrieve and set list of available ports
        if (availablePorts != null) {
            availablePorts.getItems().addAll(SerialPort.getCommPorts());
            availablePorts.setConverter(new StringConverter<SerialPort>() {
                @Override
                public String toString(SerialPort object) {
                    return object.getSystemPortName();
                }

                @Override
                public SerialPort fromString(String string) {
                    return availablePorts.getItems().stream()
                            .filter((SerialPort sp) -> sp.getSystemPortName().equals(string))
                            .findFirst()
                            .get();
                }
            });
        }
        sendButton.setDisable(true);
//        inputArea.setOnKeyPressed(event -> {
//            if(event.getCode() == KeyCode.ENTER && inputArea.isFocused()){
//                sendMessageAction(null);
//            }
//        });
    }

    @FXML
    protected void connectToPortAction(ActionEvent event) {
        if (!connectButton.isSelected()) {
            // clicked on 'Disconnect'
            String currentPortName = connectedPort.getSystemPortName();
            if (!disconnectFromCurrentPort()) {
                debugOutput.debug("Failed to disconnect from ", currentPortName);
                connectButton.setSelected(true);
                return;
            }
            availablePorts.setDisable(false);
            sendButton.setDisable(true);
            debugOutput.debug("Disconnected from ", currentPortName);
            connectButton.setText("Connect");
        } else {
            // clicked on 'Connect'
            SerialPort portToConnect = availablePorts.getValue();
            if (portToConnect != null) {
                connectedPort = portToConnect;
                debugOutput.debug("Connecting to " + connectedPort.getSystemPortName() + "...");
                setupConnection(
                        connectedPort,
                        debugOutput,
                        (ComPortOutput output) -> {
                            comPortOutput = output;
                            connectedPort.removeDataListener();
                            ComPortInputListener listener = ioFactory.getComPortInputListener(outputArea::appendText);
                            listener.setDebugOutput(debugOutput);
                            connectedPort.addDataListener(listener);

                            sendButton.setDisable(false);
                            availablePorts.setDisable(true);
                            debugOutput.debug("Connected to ", connectedPort.getSystemPortName());
                            connectButton.setText("Disconnect");
                        },
                        () -> {
                            debugOutput.debug("Failed to connect to ", connectedPort.getSystemPortName());
                            log.error("Failed to connect to {}", connectedPort.getSystemPortName());
                            connectedPort = null;
                            comPortOutput = null;
                            connectButton.setSelected(false);
                        }
                    );
            } else {
                debugOutput.debug("Failed to connect: no port selected");
                connectButton.setSelected(false);
            }
        }
    }

    @FXML
    protected void sendMessageAction(ActionEvent e) {
        byte[] dataToWrite = inputArea.getText().getBytes(DEFAULT_TRANSPORT_ENCODING);
        comPortOutput.writeBytes(dataToWrite, dataToWrite.length);
    }

    protected void setupConnection(SerialPort port, DebugOutput debugOutput,
                                   Consumer<ComPortOutput> onSuccess, Runnable onError) {
        ioFactory.getComPortConnectionAlgorithm(port, debugOutput, onSuccess, onError).connect();
    }

    protected boolean disconnectFromCurrentPort() {
        if (connectedPort == null) {
            return false;
        }
        if (connectedPort.closePort()) {
            connectedPort = null;
            comPortOutput = null;
            return true;
        }
        log.error("Failed to disconnectFromCurrentPort from {}", connectedPort.getSystemPortName());
        return false;
    }
}
