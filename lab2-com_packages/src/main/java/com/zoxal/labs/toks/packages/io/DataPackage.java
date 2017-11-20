package com.zoxal.labs.toks.packages.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Wraps raw byte array and provides data package abstraction.
 *
 * @author Mike
 * @version 10/15/2017
 */
public class DataPackage {
    private static final Logger log = LoggerFactory.getLogger(DataPackage.class);
    public static final int PACKAGE_SIZE = 20;
    public static final int DATA_SIZE = 16;
    public static final int BYTE_SIZE = 8;
    public static final byte FLAG_BYTE = Byte.parseByte("01010101", 2);
    public static final byte ESCAPE_BYTE = 0x4A;
    public static final byte EMPTY_BYTE = 0x0;
    public static final byte ESCAPE_CONFIRMATION = 0x4B;
    public static final byte FLAG_CONFIRMATION = 0x4C;

    public static final byte FLAG_OFFSET = 0;
    public static final byte FROM_OFFSET = 1;
    public static final byte TO_OFFSET = 2;
    public static final byte DATA_OFFSET = 3;
    public static final byte FCS_OFFSET = 19;
    protected byte[] data = new byte[PACKAGE_SIZE];
    protected boolean isMultipart = false;

    public DataPackage() {
        Arrays.fill(data, EMPTY_BYTE);
        data[FLAG_OFFSET] = FLAG_BYTE;
    }

    /**
     * Copy constructor
     *
     * @param copyObject    object to get clone from
     */
    public DataPackage(DataPackage copyObject) {
        System.arraycopy(copyObject.data, 0, data, 0, PACKAGE_SIZE);
    }

    /**
     * Sets raw bytes on DATA_OFFSET position.
     * Clears array before operation.
     * @param dataToCopy        raw data array to copy from
     * @param dataToCopyOffset  raw data position to copy from
     * @param dataLength        raw data length
     * @throws IllegalArgumentException if dataLenght is more than <code>DATA_SIZE</code>
     */
    public void setPayload(byte[] dataToCopy, int dataToCopyOffset, int dataLength) {
        if (dataLength > DATA_SIZE) {
            throw new IllegalArgumentException("Raw data length can not be larger then data length within package");
        }
        Arrays.fill(data, DATA_OFFSET, FCS_OFFSET, EMPTY_BYTE);
        System.arraycopy(dataToCopy, dataToCopyOffset, this.data, DATA_OFFSET, dataLength);
    }

    /**
     * Sets raw bytes of inner byte array.
     * Clears array before operation.
     * @param dataToCopy           raw data array to copy from
     * @param dataToCopyOffset  raw data position to copy from
     * @param dataLength        raw data length
     * @throws IllegalArgumentException if dataLenght is more than <code>DATA_SIZE</code>
     */
    public void setRawData(byte[] dataToCopy, int dataToCopyOffset, int dataLength) {
        if (dataLength > PACKAGE_SIZE) {
            throw new IllegalArgumentException("Raw data length can not be larger then data legth within package");
        }
        Arrays.fill(data, EMPTY_BYTE);
        System.arraycopy(dataToCopy, dataToCopyOffset, this.data, 0, dataLength);
    }

    /**
     * Recalculates data package FCS
     */
    public void updateFCS() {
        this.data[FCS_OFFSET] = calculateFCS();
    }

    /**
     * Calculates data package FCS without setting
     *
     * @return FCS      package FCS
     */
    public byte calculateFCS() {
        byte fcs = data[DATA_OFFSET];
        for (int i = DATA_OFFSET + 1; i < FCS_OFFSET; i++) {
            fcs ^= data[i];
        }
        return fcs;
    }

    /**
     * Recalculates FCS and checks if it is valid.
     *
     * @return true     if data package FCS if valid.
     */
    public boolean isFCSValid() {
        return this.data[FCS_OFFSET] == calculateFCS();
    }

    /**
     * Performs byte staff operation of data package payload.
     * During staffing operation payload size can change. If it
     * overruns allowed fixed package size, second package will be
     * created and <code>isMultipart</code> flag on first package will
     * be raised. FCS of packages will not change during operation.
     *
     * @return  staffedPackages[]   array of staffed packages
     */
    public DataPackage[] byteStaff() {
        DataPackage firstPackage = new DataPackage(this);

        byte[] staffedData = new byte[DATA_SIZE * 2];
        Arrays.fill(staffedData, EMPTY_BYTE);

        int staffedDataOffset = 0;
        for (byte i = 0; i < DATA_SIZE; i++, staffedDataOffset++) {
            if (data[DATA_OFFSET + i] == EMPTY_BYTE) break;         // end of payload
            if (data[DATA_OFFSET + i] == FLAG_BYTE) {               // escaping flag byte
                staffedData[staffedDataOffset] = ESCAPE_BYTE;
                staffedDataOffset++;
                staffedData[staffedDataOffset] = FLAG_CONFIRMATION;
            } else if (data[DATA_OFFSET + i] == ESCAPE_BYTE) {      // escaping escape byte
                staffedData[staffedDataOffset] = ESCAPE_BYTE;
                staffedDataOffset++;
                staffedData[staffedDataOffset] = ESCAPE_CONFIRMATION;
            } else {
                staffedData[staffedDataOffset] = data[DATA_OFFSET + i];
            }
        }
        firstPackage.setPayload(staffedData, 0, DATA_SIZE);

        if (staffedDataOffset > DATA_SIZE) {
            DataPackage[] staffedPackages = new DataPackage[2];
            staffedPackages[0] = firstPackage;
            staffedPackages[1] = headCopy();
            staffedPackages[1].setPayload(staffedData, DATA_SIZE, staffedDataOffset - DATA_SIZE);

            isMultipart = true;
            staffedPackages[0].isMultipart = true;
//            staffedPackages[1].calculateFCS();
            return staffedPackages;
        } else {
            DataPackage[] staffedPackages = new DataPackage[1];
            staffedPackages[0] = firstPackage;
            return staffedPackages;
        }
    }

    /**
     * Performs destaffing operation on payload.
     * Also updates package <code>isMultipart</code> property. FCS will
     * not be updated during operation.
     *
     * @return destaffedPackage     new package with destaffed data
     */
    public DataPackage byteDestaff() {
        byte[] destaffedData = new byte[DATA_SIZE];
        Arrays.fill(destaffedData, EMPTY_BYTE);
        int destaffedDataOffset = 0;
        boolean destaffPrecedent = false;
        int i;
        for (i = 0; i < DATA_SIZE; i++, destaffedDataOffset++) {
            if (data[DATA_OFFSET + i] == EMPTY_BYTE) break;
            log.trace("got byte {}, is escape: {}", Integer.toHexString(data[DATA_OFFSET + i]), data[DATA_OFFSET + i] == ESCAPE_BYTE);
            if (data[DATA_OFFSET + i] == ESCAPE_BYTE) {
                if ((i + 1) == DATA_SIZE) {
                    destaffPrecedent = true;
                    destaffedData[destaffedDataOffset] = ESCAPE_BYTE;
                } else if (i < (DATA_SIZE - 1)
                        && data[DATA_OFFSET + i + 1] == ESCAPE_CONFIRMATION) {
                    destaffPrecedent = true;
                    destaffedData[destaffedDataOffset] = ESCAPE_BYTE;
                    i++;
                    log.trace("got escape confirmation at {}", i);
                } else if (i < (DATA_SIZE - 1)
                        && data[DATA_OFFSET + i + 1] == FLAG_CONFIRMATION) {
                    destaffPrecedent = true;
                    destaffedData[destaffedDataOffset] = FLAG_BYTE;
                    i++;
                    log.trace("got flag confirmation at {}", i);
                } else {
                    log.error("Unexpected sequence near {}: {}", i, this);
                }
            } else {
                destaffedData[destaffedDataOffset] = data[DATA_OFFSET + i];
            }
        }
        if (isUTFEscape(data[FCS_OFFSET - 1]) || (destaffPrecedent && i > DATA_SIZE)) {
            this.isMultipart = true;
        }
        DataPackage dataPackage = headCopy();
        dataPackage.setPayload(destaffedData, 0, destaffedDataOffset);
        dataPackage.isMultipart = this.isMultipart;
        log.trace("byteDestaff result: {}, is multipart: {}", dataPackage, dataPackage.isMultipart);
        return dataPackage;
    }

    /**
     * Performs destaffing operation on raw data.
     *
     * @return destaffedData     array of destaffed data
     */
    public static byte[] byteDestaff(byte[] dataArray) {
        log.debug("destaffing: {}", DataPackage.HexByteArray(dataArray));
        byte[] destaffedData = new byte[dataArray.length];
        Arrays.fill(destaffedData, EMPTY_BYTE);
        int destaffedDataOffset = 0;
        for (int i = 0; i < dataArray.length; i++, destaffedDataOffset++) {
            if (dataArray[i] == EMPTY_BYTE) break;      // end of payload
            if (dataArray[i] == ESCAPE_BYTE) {
                if (dataArray[i + 1] == ESCAPE_CONFIRMATION) {       // escape byte
                    destaffedData[destaffedDataOffset] = ESCAPE_BYTE;
                    i++;
                } else if (dataArray[i + 1] == FLAG_CONFIRMATION) {   // escape byte
                    destaffedData[destaffedDataOffset] = FLAG_BYTE;
                    i++;
                }
            } else {
                destaffedData[destaffedDataOffset] = dataArray[i];
            }
        }
        log.debug("static byteDestaff result: {}", HexByteArray(destaffedData));
        return destaffedData;
    }

    public byte[] asByteArray() {
        return data;
    }

    public static DataPackage fromByteArray(byte[] data) {
        DataPackage dataPackage = new DataPackage();
        dataPackage.setRawData(data, 0, PACKAGE_SIZE);
        return dataPackage;
    }

    public byte[] getCleanPayload() {
        int i;
        for (i = DATA_OFFSET; i < FCS_OFFSET; i++) {
            if (data[i] == EMPTY_BYTE) break;
        }
        return Arrays.copyOfRange(this.data, DATA_OFFSET, i);
    }

    /**
     * Creates copy of package with same header and FCS
     *
     * @return  copy    copy of package with same header and FCS
     */
    public DataPackage headCopy() {
        DataPackage dataPackage = new DataPackage();
        dataPackage.setFrom(this.getFrom());
        dataPackage.setTo(this.getTo());
        dataPackage.data[FCS_OFFSET] = data[FCS_OFFSET];
        return dataPackage;
    }

    /**
     * Pretty prints raw package data in hex radix;
     *
     * @return  string      string representation of package row data
     */
    public String toString() {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[data.length * 3 + 2];
        int j = 0;
        for (int i = 0; i < data.length; i++ , j += 3) {
            int v = data[i] & 0xFF;
            if (i == DATA_OFFSET) {
                hexChars[j] = '[';
                j++;
            }
            hexChars[j] = hexArray[v >>> 4];
            hexChars[j + 1] = hexArray[v & 0x0F];
            if (i == FCS_OFFSET - 1) {
                hexChars[j + 2] = ']';
                j++;
            }
            hexChars[j + 2] = ' ';

        }
        return new String(hexChars);
    }


    public static String HexByteArray(byte[] data) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[data.length * 3];
        int j = 0;
        for (int i = 0; i < data.length; i++ , j += 3) {
            int v = data[i] & 0xFF;
            hexChars[j] = hexArray[v >>> 4];
            hexChars[j + 1] = hexArray[v & 0x0F];
            hexChars[j + 2] = ' ';

        }
        return new String(hexChars);
    }

    public void setTo(byte address) {
        data[TO_OFFSET] = address;
    }
    public void setFrom(byte address) {
        data[FROM_OFFSET] = address;
    }

    public byte getFrom() {
        return data[FROM_OFFSET];
    }
    public byte getTo() {
        return data[TO_OFFSET];
    }

    public boolean isMultipart() {
        return isMultipart;
    }

    public static boolean isUTFEscape(byte byteToTest) {
        // UTF uses 110xxxxx mask for first byte in two-byte symbols
        return ((byteToTest & 128) > 0)
                && ((byteToTest & 64) > 0)
                && ((byteToTest & 32) == 0);
    }
}

//    public DataPackage[] byteStaff2() {
//        log.debug("byte staffing");
//        // real positions to staff
//        List<Integer> staffPositions = new ArrayList<>();
//        // current data
//        byte[] payload = Arrays.copyOfRange(data, DATA_OFFSET, DATA_SIZE);
//
//        BigInteger payloadBits = new BigInteger(payload);
//        BigInteger flagSearcher = BigInteger.valueOf((long)FLAG_BYTE);
//        int stopSearchOffset = (DATA_SIZE - 1) * BYTE_SIZE;
//        // compare from left
//        flagSearcher = flagSearcher.shiftLeft(stopSearchOffset);
//
//        log.debug("payload bits: {}", payloadBits.toString(2));
//        log.debug("flagSearcher bits: {}", flagSearcher.toString(2));
//        // others will shift right
//        int staffInsertedCount = 0;
//        for (int searchOffset = 0; searchOffset < stopSearchOffset; searchOffset++, flagSearcher.shiftRight(1)) {
//            if (payloadBits.and(flagSearcher).equals(flagSearcher)) {
//                staffPositions.add(searchOffset / BYTE_SIZE + 1 + staffInsertedCount);
//                staffInsertedCount++;
//                searchOffset += (BYTE_SIZE - 1) - searchOffset % BYTE_SIZE;
//            }
//        }
//
//        if (staffPositions.isEmpty()) {
//            log.debug("no staffing");
//            return new DataPackage[]{this};
//        } else {
//            byte[] returnPayloadArray = new byte[DATA_SIZE * 2];
//            Arrays.fill(returnPayloadArray, ESCAPE_BYTE);
//            int insertedPayloadBytes = 0;
//            for (int i = 0; insertedPayloadBytes < DATA_SIZE; i++) {
//                log.debug("inserting {} at {}", payload[insertedPayloadBytes], i);
//                if (!staffPositions.contains(i)) {
//                    returnPayloadArray[i] = payload[insertedPayloadBytes];
//                    insertedPayloadBytes++;
//                }
//            }
//            DataPackage[] returnArray = new DataPackage[2];
//            this.setRawData(returnPayloadArray, 0, DATA_SIZE);
//            returnArray[0] = this;
//            returnArray[1] = new DataPackage();
//            returnArray[1].setRawData(returnPayloadArray, DATA_SIZE, DATA_SIZE);
//
//            return returnArray;
//        }
//    }
