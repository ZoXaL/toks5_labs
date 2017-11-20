package com.zoxal.labs.toks.comports;

import com.fazecast.jSerialComm.SerialPort;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComPortUtils {

    private ComPortUtils() {

    }

    public static byte getPortAddress(SerialPort port) {
        Pattern portNumberPatter = Pattern.compile("COM(\\d+)");
        Matcher portNumberMatcher = portNumberPatter.matcher(port.getSystemPortName());
        portNumberMatcher.find();
        return Byte.parseByte(portNumberMatcher.group(1));
    }
}
