package com.zoxal.labs.toks.comports.io;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

public class DebugOutput {
    protected Consumer<String> debugConsumer;

    public void setDebugConsumer(Consumer<String> debugConsumer) {
        this.debugConsumer = debugConsumer;
    }

    public void debug(String ... messages) {
        if (debugConsumer == null) {
            throw new IllegalStateException("DebugOutput is configured wrong: no debugConsumer");
        }

        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("[HH:mm:ss:SSS]: ");

        StringBuilder builder = new StringBuilder(64);
        builder.append(sdf.format(new Date()));
        builder.append(String.join("", messages));
        builder.append('\n');

        debugConsumer.accept(builder.toString());
    }
}
