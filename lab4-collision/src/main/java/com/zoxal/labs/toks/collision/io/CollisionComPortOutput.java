package com.zoxal.labs.toks.collision.io;

import com.fazecast.jSerialComm.SerialPort;
import com.zoxal.labs.toks.collision.CollisionUtils;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Sending algorithm:
 * <ol>
 *     <li>Check that channel is free. If no,
 *         <ol>
 *             <li>Generate random suspend</li>
 *             <li>Check channel availability again</li>
 *         </ol>
 *     </li>
 *     <li>Send data</li>
 *     <li>Check for collision after collision frame</li>
 * </ol>
 *
 * @author Mike
 * @version 11/20/2017
 */
public class CollisionComPortOutput {
    private static final Logger log = LoggerFactory.getLogger(CollisionComPortOutput.class);


    private static final long SLOT_TIME = 100L;
    private static final long RETRIES_LIMIT = 2;
    public static final Charset CHARSET = StandardCharsets.UTF_8;

    private SerialPort comPort;
    private DebugOutput debugOutput;

    public CollisionComPortOutput(DebugOutput debugOutput) {
        this.debugOutput = debugOutput;
    }

    public void writeUnchecked(byte data) {
        comPort.writeBytes(new byte[]{data}, 1);
    }

    public void writeData(byte data) throws CollisionException, InterruptedException {
        Platform.runLater(() ->
            debugOutput.debug("-----------------")
        );
        int retryNumber = 0;
        boolean collisionDetected;
        do {
            final int retryNumberFinal = retryNumber;
            Platform.runLater(() ->
                debugOutput.debug("Collision retry count: ", String.valueOf(retryNumberFinal))
            );

            // Check if channel is free
            waitForFreeChannel();

            // writing data
            comPort.writeBytes(new byte[]{data}, 1);

            // wait for collision frame
            collisionDetected = CollisionUtils.waitCollisionFrame();
            log.debug("collision: {}", collisionDetected);
            retryNumber++;
        } while(retryNumber < RETRIES_LIMIT && collisionDetected);

        if (collisionDetected) {
            throw new CollisionException();
        }
    }

    private void waitForFreeChannel() throws InterruptedException {
        int numberOfRetries = 0;
        do {
            final int numberOfRetiesFinal = numberOfRetries;
            Platform.runLater(() ->
                    debugOutput.debug("Attempting to capture the channel: attempt ", String.valueOf(numberOfRetiesFinal))
            );
            long slotsCount = CollisionUtils.generateSlotsCount(numberOfRetries);
            Platform.runLater(() -> debugOutput.debug("Waiting for ", String.valueOf(slotsCount), " slots..."));
            Thread.sleep(slotsCount * SLOT_TIME);
            numberOfRetries++;
        } while(!CollisionUtils.isChannelFree());
    }

    public void setComPort(SerialPort comPort) {
        this.comPort = comPort;
    }
}
