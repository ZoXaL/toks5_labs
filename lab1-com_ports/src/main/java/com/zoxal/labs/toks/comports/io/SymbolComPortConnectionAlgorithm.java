package com.zoxal.labs.toks.comports.io;

import com.fazecast.jSerialComm.SerialPort;
import java.util.function.Consumer;

public class SymbolComPortConnectionAlgorithm extends ComPortConnectionAlgorithm {
    public SymbolComPortConnectionAlgorithm(SerialPort port, DebugOutput debugOutput,
                                            Consumer<ComPortOutput> onSuccess, Runnable onError) {
        super(port, debugOutput, onSuccess, onError);
    }

    @Override
    public void connect() {
        if (!port.openPort()) {
            onError.run();
            return;
        }
        ComPortOutput output = new SymbolComPortOutput();
        output.setComPortOutput(port::writeBytes);
        onSuccess.accept(output);
    }
}
