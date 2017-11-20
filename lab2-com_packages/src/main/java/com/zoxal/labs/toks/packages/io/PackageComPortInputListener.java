package com.zoxal.labs.toks.packages.io;

import com.fazecast.jSerialComm.SerialPortEvent;
import com.zoxal.labs.toks.comports.io.ComPortInputListener;
import com.zoxal.labs.toks.comports.io.ComPortOutput;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Waits for incoming packages
 *
 * @author Mike
 * @version 15/10/2017
 */
public class PackageComPortInputListener extends ComPortInputListener {
    private static final Logger log = LoggerFactory.getLogger(PackageComPortInputListener.class);
    private byte[] buffer = new byte[DataPackage.DATA_SIZE * 2];
    private int bufferOffset = 0;

    public PackageComPortInputListener(Consumer<String> inputDataConsumer) {
        super(inputDataConsumer);
    }

    @Override
    public int getPacketSize() {
        return DataPackage.PACKAGE_SIZE;
    }


    @Override
    public synchronized void serialEvent(SerialPortEvent serialPortEvent) {
        if (inputDataConsumer == null) {
            log.error("Error in serial event: ", new IllegalStateException("SymbolComPortInputListener is configured wrong: no inputDataConsumer"));
            return;
        }
        byte[] data = serialPortEvent.getReceivedData();
        DataPackage dataPackage = DataPackage.fromByteArray(data);
        log.debug("received row {}", dataPackage);

        DataPackage destaffedPackage = dataPackage.byteDestaff();

        if (!destaffedPackage.isFCSValid()) {   // no fcs update at all
            log.debug("fcs violation");
            debugOutput.debug("ERROR: FCS violation for package: \n\t" + destaffedPackage.toString());
        }

        String stringPayload;
        log.debug("Buffer contains: {}, buffer offset: {}", DataPackage.HexByteArray(buffer), bufferOffset);

        if (destaffedPackage.isMultipart()) {   // got first part -- copying to buffer
            log.debug("Package is first part");
            byte[] cleanPayload = dataPackage.getCleanPayload();
            System.arraycopy(
                    cleanPayload,
                    0,
                    buffer,
                    bufferOffset,
                    Integer.min(DataPackage.DATA_SIZE, cleanPayload.length)
            );
            bufferOffset = Integer.min(DataPackage.DATA_SIZE, cleanPayload.length);
            log.debug("Buffer contains: {}, buffer offset: {}", DataPackage.HexByteArray(buffer), bufferOffset);
            return;
        } else if (bufferOffset != 0) { // got second part -- copying to buffer and retrieving full package
            log.debug("Package is second part");
            byte[] cleanPayload = dataPackage.getCleanPayload();
            System.arraycopy(
                    cleanPayload,
                    0,
                    buffer,
                    bufferOffset,
                    Integer.min(buffer.length - bufferOffset - 1, cleanPayload.length)
            );
            bufferOffset = 0;
            stringPayload = new String(DataPackage.byteDestaff(buffer), ComPortOutput.DEFAULT_TRANSPORT_ENCODING);
            log.debug("Buffer contains: {}, buffer offset: {}", DataPackage.HexByteArray(buffer), bufferOffset);
            Arrays.fill(buffer, (byte)0);
        } else {                        // usual package
            byte[] cleanPayload = destaffedPackage.getCleanPayload();
            stringPayload = new String(cleanPayload, ComPortOutput.DEFAULT_TRANSPORT_ENCODING);
            log.debug("got usual package");
        }
        log.debug("Received data {}", stringPayload);
        Platform.runLater(() -> inputDataConsumer.accept(stringPayload));
    }
}
