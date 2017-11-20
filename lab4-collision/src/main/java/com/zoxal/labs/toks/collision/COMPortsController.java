package com.zoxal.labs.toks.collision;

import com.fazecast.jSerialComm.SerialPort;
import com.zoxal.labs.toks.collision.io.CollisionComPortInputListener;
import com.zoxal.labs.toks.collision.io.CollisionComPortOutput;
import com.zoxal.labs.toks.collision.io.CollisionException;
import com.zoxal.labs.toks.collision.io.DebugOutput;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;


public class COMPortsController implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(COMPortsController.class);
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

    private SerialPort connectedPort;
    private DebugOutput debugOutput = new DebugOutput();
    private CollisionComPortOutput comPortOutput = new CollisionComPortOutput(debugOutput);

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
        inputArea.setDisable(true);
        debugOutput.setDebugConsumer(debugArea::appendText);

        // key press configuration
        inputArea.setOnKeyTyped(event -> Platform.runLater(() -> {
            inputArea.setDisable(true);
            new Thread(() -> {
                try {
                    comPortOutput.writeData(
                        event.getCharacter().getBytes(CollisionComPortOutput.CHARSET)[0]
                    );
                } catch (CollisionException ce) {
                    debugOutput.debug("X");
                } catch (InterruptedException ie) {
                    log.warn("Unexpected exception during waiting slot time", ie);
                }
                inputArea.setDisable(false);
            }).start();
        }));
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
            inputArea.setDisable(true);
            debugOutput.debug("Disconnected from ", currentPortName);
            connectButton.setText("Connect");
        } else {
            // clicked on 'Connect'
            SerialPort portToConnect = availablePorts.getValue();
            if (portToConnect != null) {
                connectedPort = portToConnect;
                debugOutput.debug("Connecting to " + connectedPort.getSystemPortName() + "...");
                if (connectedPort.openPort()) {
                    setupConnectedPort();
                    inputArea.setDisable(false);
                    availablePorts.setDisable(true);
                    debugOutput.debug("Connected to ", connectedPort.getSystemPortName());
                    connectButton.setText("Disconnect");
                } else {
                    debugOutput.debug("Failed to connect to ", connectedPort.getSystemPortName());
                    log.error("Failed to connect to {}", connectedPort.getSystemPortName());
                    connectedPort = null;
                    connectButton.setSelected(false);
                }
            } else {
                debugOutput.debug("Failed to connect: no port selected");
                connectButton.setSelected(false);
            }
        }
    }

    private void setupConnectedPort() {
        comPortOutput.setComPort(connectedPort);
        connectedPort.removeDataListener();
        CollisionComPortInputListener listener = new CollisionComPortInputListener(outputArea::appendText);
        listener.setDebugOutput(debugOutput);
        listener.setComPortOutput(comPortOutput);
        connectedPort.addDataListener(listener);
    }

    boolean disconnectFromCurrentPort() {
        if (connectedPort == null) {
            return false;
        }
        if (connectedPort.closePort()) {
            connectedPort = null;
            return true;
        }
        log.error("Failed to disconnectFromCurrentPort from {}", connectedPort.getSystemPortName());
        return false;
    }
}
