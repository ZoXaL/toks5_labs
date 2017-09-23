package com.zoxal.labs.toks.comports;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;

public class View extends Application {
    private static final String FXML_FILE = "COMPortsMessagerPane.fxml";
    private COMPortsController controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (controller != null) {
            controller.disconnect(controller.getConnectedPort());
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("COM-port messager");
        primaryStage.setScene(new Scene(createFXMLView()));
        primaryStage.setWidth(700);
        primaryStage.setMaxWidth(900);
        primaryStage.setMinWidth(600);

        primaryStage.setHeight(400);
        primaryStage.setMaxHeight(600);
        primaryStage.setMinHeight(400);
        primaryStage.show();
    }

    protected Pane createFXMLView() {
        try {
            FXMLLoader loader = new FXMLLoader(ClassLoader.getSystemResource(FXML_FILE));
            controller = loader.getController();
            return loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return createViewProgrammatically();
        }
    }

    // Implementation just for fun, not working really
    protected Pane createViewProgrammatically() {
        HBox mainPane = new HBox(10);
        mainPane.setPadding(new Insets(10));

        Pane debugPane = getSettingsDebugPane();
        debugPane.setMinWidth(250);
        debugPane.setMaxWidth(300);

        Pane sendReceivePane = getSendReceivePane();
        HBox.setHgrow(sendReceivePane, Priority.ALWAYS);
        mainPane.getChildren().addAll(
                debugPane,
                new Separator(Orientation.VERTICAL),
                sendReceivePane
        );

        return mainPane;
    }

    private Pane getConnectionPane() {
        VBox connectionBox = new VBox(10);
        connectionBox.setAlignment(Pos.CENTER);

        FlowPane selectingControlPane = new FlowPane(Orientation.HORIZONTAL);
        selectingControlPane.setAlignment(Pos.CENTER);
        selectingControlPane.setHgap(10);
        Label selectPortLabel = new Label("Select port: ");
        ComboBox<String> availablePorts = new ComboBox<>(
                FXCollections.observableArrayList("COM1", "COM2", "COM3")
        );
        selectingControlPane.getChildren().addAll(selectPortLabel, availablePorts);

        ToggleButton connectButton = new ToggleButton("Connect");

        connectionBox.getChildren().addAll(selectingControlPane, connectButton);
        return connectionBox;
    }

    private Pane getSettingsDebugPane() {
        VBox settingsDebugPane = new VBox(10);
        settingsDebugPane.setAlignment(Pos.TOP_CENTER);

        TextArea debugArea = new TextArea();
        debugArea.setPromptText("Debug info");
        debugArea.setEditable(false);

        VBox.setVgrow(debugArea, Priority.ALWAYS);

        settingsDebugPane.getChildren().addAll(
                getConnectionPane(),
                new Separator(Orientation.HORIZONTAL),
                debugArea
        );
        return settingsDebugPane;
    }

    private Pane getSendReceivePane() {
        VBox sendReceivePane = new VBox(10);
        sendReceivePane.setAlignment(Pos.CENTER);

        TextArea inputArea = new TextArea();
        inputArea.setPromptText("Start typing here");
        Button sendButton = new Button("Send");
        sendButton.setPrefWidth(150);
        TextArea outputArea = new TextArea();
        outputArea.setPromptText("Received data");

        VBox.setVgrow(inputArea, Priority.ALWAYS);
        VBox.setVgrow(outputArea, Priority.ALWAYS);

        sendReceivePane.getChildren().addAll(
                inputArea,
                sendButton,
                outputArea
        );
        return sendReceivePane;
    }
}
