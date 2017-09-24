package com.zoxal.labs.toks.comports.io;

import java.util.function.Consumer;

public interface IOFactory {
    ComPortOutput getComPortOutput();
    ComPortInputListener getComPortInputListener(Consumer<String> inputDataConsumer);
    DebugOutput getDebugOutput();
}
