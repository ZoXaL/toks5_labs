package com.zoxal.labs.toks.packages;

import com.zoxal.labs.toks.comports.io.IOFactory;
import com.zoxal.labs.toks.packages.io.PackageIOFactory;

import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
import java.net.URL;

/**
 * Main application class. Extends loading logic from parent.
 * Parent class was used in first lab.
 *
 * @author Mike
 * @version 10/15/2017
 */
public class MainView extends com.zoxal.labs.toks.comports.MainView {
    @Override
    public void run(String[] args) {
        launch(args);
    }

    @Override
    protected IOFactory getIOFactory() {
        return new PackageIOFactory();
    }

    @Override
    protected URL getFXMLView() {
        return this.getClass().getClassLoader().getResource("COMPortsMessagerPane.fxml");
    }
}
