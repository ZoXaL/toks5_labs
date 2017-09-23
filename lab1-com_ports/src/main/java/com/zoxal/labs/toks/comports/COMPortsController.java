package com.zoxal.labs.toks.comports;

import com.fazecast.jSerialComm.SerialPort;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.util.StringConverter;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import static com.zoxal.labs.toks.comports.InputDataListener.TRANSPORT_ENCODING;

public class COMPortsController implements Initializable{
    @FXML
    private ComboBox<SerialPort> availablePorts;
    @FXML
    private ToggleButton connectButton;
    @FXML
    private TextArea debugArea;
    @FXML
    private TextArea inputArea;
    @FXML
    private TextArea outputArea;

    public SerialPort getConnectedPort() {
        return connectedPort;
    }

    private SerialPort connectedPort;

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
    }

    @FXML
    protected void connectToPortAction(ActionEvent event) {
        if (!connectButton.isSelected()) {
            // clicked on 'Disconnect'
            if (!disconnect(connectedPort)) {
                debug("Failed to disconnect from ", connectedPort.getDescriptivePortName());
                connectButton.setSelected(true);
                return;
            }

            availablePorts.setDisable(false);
            debug("Disconnected from ", connectedPort.getDescriptivePortName());
            connectButton.setText("Connect");
        } else {
            // clicked on 'Connect'
            connectedPort = availablePorts.getValue();
            if (connectedPort == null || !setupConnection(connectedPort)) {
                if (connectedPort == null) {
                    debug("Failed to connect: no port selected");
                } else {
                    debug("Failed to connect to ", connectedPort.getDescriptivePortName());
                }
                connectButton.setSelected(false);
                return;
            }
            availablePorts.setDisable(true);
            debug("Connected to ", connectedPort.getDescriptivePortName());
            connectButton.setText("Disconnect");
        }
    }

    @FXML
    protected void sendMessageAction(ActionEvent e) {
        byte[] dataToWrite = inputArea.getText().getBytes(TRANSPORT_ENCODING);
        connectedPort.writeBytes(dataToWrite, dataToWrite.length);
    }

    protected boolean setupConnection(SerialPort connectedPort) {
        if (connectedPort == null || !connectedPort.openPort()) return false;
        connectedPort.addDataListener(new InputDataListener(outputArea::appendText));
        return true;
    }

    protected boolean disconnect(SerialPort connectedPort) {
        return (connectedPort != null) && connectedPort.closePort();
    }

    protected void debug(String ... args) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("[HH:mm:ss]: ");

        StringBuilder builder = new StringBuilder();
        builder.append(sdf.format(new Date()));
        builder.append(String.join("", args));
        builder.append('\n');
        debugArea.appendText(builder.toString());
    }
}
