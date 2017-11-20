package com.zoxal.labs.toks.packages.io;

import com.zoxal.labs.toks.comports.io.ComPortOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;

import static com.zoxal.labs.toks.packages.io.DataPackage.DATA_SIZE;
import static com.zoxal.labs.toks.packages.io.DataPackage.PACKAGE_SIZE;

/**
 * Sends data to connected peer.
 *
 * @author Mike
 * @version 15/10/2017
 */
public class PackageComPortOutput extends ComPortOutput {
    private static final Logger log = LoggerFactory.getLogger(PackageComPortOutput.class);

    protected byte originalAddress;
    protected byte destinationAddress;

    /**
     * Allows to send complete data package
     *
     * @param dataPackage   data package to send
     */
    public void sendPackage(DataPackage dataPackage) {
        log.debug("Sending package: {}", dataPackage);
        debugOutput.debug(dataPackage.toString());
        rawOutput.writeBytes(dataPackage.asByteArray(), PACKAGE_SIZE);
    }

    @Override
    public void writeBytes(byte[] buffer, long bufferSize) {
        // splits raw buffer data by packages, updates FCS and sends them
        for (int bufferOffset = 0; bufferOffset < bufferSize; bufferOffset += DATA_SIZE) {
            DataPackage dataPackage = new DataPackage();
            dataPackage.setFrom(originalAddress);
            dataPackage.setTo(destinationAddress);
            int bufferOffsetDelta = Integer.min(DATA_SIZE, buffer.length - bufferOffset);
            dataPackage.setPayload(buffer, bufferOffset, bufferOffsetDelta);

            dataPackage.updateFCS();        // updating FCS before byte staffing

            DataPackage[] staffedPackages = dataPackage.byteStaff();
            if (staffedPackages.length < 2
                    && DataPackage.isUTFEscape(buffer[Integer.min(buffer.length - 1, bufferOffset + bufferOffsetDelta - 1)])) {
                log.debug("Sending utf as separate package");
                // sending UTF-escape as separate package
                DataPackage dataPackage2 = new DataPackage();
                dataPackage2.setFrom(originalAddress);
                dataPackage2.setTo(destinationAddress);
                dataPackage2.setPayload(buffer, bufferOffset + bufferOffsetDelta, 1);
                sendPackage(dataPackage);
                sendPackage(dataPackage2);
                bufferOffset++;
            } else {
                for (DataPackage staffedPackage : staffedPackages) {
                    sendPackage(staffedPackage);
                }
            }

        }
    }

    public void setOriginalAddress(byte originalAddress) {
        this.originalAddress = originalAddress;
    }

    public void setDestinationAddress(byte destinationAddress) {
        this.destinationAddress = destinationAddress;
    }
}
