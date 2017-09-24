package com.zoxal.labs.toks.comports;

import com.fazecast.jSerialComm.SerialPort;
import com.zoxal.labs.toks.comports.io.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static com.zoxal.labs.toks.comports.io.RawDataOutput.DEFAULT_TRANSPORT_ENCODING;

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
    private ComPortInputListener comPortInputListener;
    private IOFactory ioFactory;

    public void setIOFactory(IOFactory ioFactory) {
        this.ioFactory = ioFactory;

        debugOutput = ioFactory.getDebugOutput();
        debugOutput.setDebugConsumer(debugArea::appendText);

        comPortInputListener = ioFactory.getComPortInputListener(outputArea::appendText);
        comPortInputListener.setDebugOutput(debugOutput);

        comPortOutput = ioFactory.getComPortOutput();
        comPortOutput.setDebugOutput(debugOutput);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // retrieve and set list of available ports
        if (availablePorts != null) {
            availablePorts.getItems().addAll(SerialPort.getCommPorts());
            availablePorts.setConverter(new StringConverter<SerialPort>() {
                @Override
                public String toString(SerialPort object) {
                    return object.getDescriptivePortName();
                }

                @Override
                public SerialPort fromString(String string) {
                    return availablePorts.getItems().stream()
                            .filter((SerialPort sp) -> sp.getDescriptivePortName().equals(string))
                            .findFirst()
                            .get();
                }
            });
        }
        sendButton.setDisable(true);
    }

    @FXML
    protected void connectToPortAction(ActionEvent event) {
        if (!connectButton.isSelected()) {
            // clicked on 'Disconnect'
            if (!disconnectFromCurrentPort()) {
                debugOutput.debug("Failed to disconnect from ", connectedPort.getDescriptivePortName());
                connectButton.setSelected(true);
                return;
            }

            availablePorts.setDisable(false);
            sendButton.setDisable(true);
            debugOutput.debug("Disconnected from ", connectedPort.getDescriptivePortName());
            connectButton.setText("Connect");
        } else {
            // clicked on 'Connect'
            SerialPort connectedPort = availablePorts.getValue();
            if (connectedPort == null || !setupConnection(connectedPort)) {
                if (connectedPort == null) {
                    debugOutput.debug("Failed to connect: no port selected");
                } else {
                    debugOutput.debug("Failed to connect to ", connectedPort.getDescriptivePortName());
                }
                connectButton.setSelected(false);
                return;
            }
            sendButton.setDisable(false);
            availablePorts.setDisable(true);
            debugOutput.debug("Connected to ", connectedPort.getDescriptivePortName());
            connectButton.setText("Disconnect");
        }
    }

    @FXML
    protected void sendMessageAction(ActionEvent e) {
        byte[] dataToWrite = inputArea.getText().getBytes(DEFAULT_TRANSPORT_ENCODING);
        comPortOutput.write(dataToWrite, dataToWrite.length);
    }

    protected boolean setupConnection(SerialPort connectedPort) {
        if (!connectedPort.openPort()) {
            log.error("Failed to connect to {}", connectedPort.getDescriptivePortName());
            return false;
        }
        this.connectedPort = connectedPort;
        connectedPort.addDataListener(comPortInputListener);
        comPortOutput.setComPortOutput(connectedPort::writeBytes);
        return true;
    }

    protected boolean disconnectFromCurrentPort() {
        if (connectedPort == null) {
            log.error("Can not disconnect from com-port: no com-port");
            return false;
        }
        if (connectedPort.closePort()) {
            connectedPort = null;
            comPortOutput.setComPortOutput(null);
            return true;
        }
        log.error("Failed to disconnectFromCurrentPort from {}", connectedPort.getDescriptivePortName());
        return false;
    }
}
