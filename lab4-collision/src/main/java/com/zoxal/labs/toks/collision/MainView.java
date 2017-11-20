package com.zoxal.labs.toks.collision;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public class MainView extends Application {
    public static final Logger log = LoggerFactory.getLogger(MainView.class);
    private static final String FXML_FILE = "COMPortsMessagerPane.fxml";
    private COMPortsController controller;

    public void run(String[] args) {
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
            controller.disconnectFromCurrentPort();
        }
        System.exit(0);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("COM-port messager: collision edition");
        primaryStage.setScene(new Scene(createFXMLView()));
        primaryStage.show();
    }

    protected Pane createFXMLView() {
        try {
            FXMLLoader loader = new FXMLLoader(getFXMLView());
            return loader.load();
        } catch (IOException e) {
            log.error("Can not load main view", e);
            return null;
        }
    }

    protected URL getFXMLView() {
        return this.getClass().getClassLoader().getResource(FXML_FILE);
    }
}
