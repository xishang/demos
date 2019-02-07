package com.demos.java.basedemo.security.base64;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;

/**
 * @author xishang
 * @version 1.0
 * @date 2018/3/6
 */
public class Base64 {

    public static String source = "Base64字符串";

    public static void main(String[] args) {
        jdkBase64();
        bcBase64();
        ccBase64();
    }

    public static void jdkBase64() {
        try {
            BASE64Encoder encoder = new BASE64Encoder();
            String result = encoder.encode(source.getBytes("utf-8"));
            System.out.println("JDK Base64 encode: " + result);
            BASE64Decoder decoder = new BASE64Decoder();
            System.out.println("JDK Base64 decode: " + new String(decoder.decodeBuffer(result), "utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void bcBase64() {
        try {
            byte[] encodeBytes = org.bouncycastle.util.encoders.Base64.encode(source.getBytes("utf-8"));
            System.out.println("BC Base64 encode: " + new String(encodeBytes, "utf-8"));
            byte[] decodeBytes = org.bouncycastle.util.encoders.Base64.decode(encodeBytes);
            System.out.println("BC Base64 decode: " + new String(decodeBytes, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void ccBase64() {
        try {
            byte[] encodeBytes = org.apache.commons.codec.binary.Base64.encodeBase64(source.getBytes("utf-8"));
            System.out.println("CC Base64 encode: " + new String(encodeBytes, "utf-8"));
            byte[] decodeBytes = org.apache.commons.codec.binary.Base64.decodeBase64(encodeBytes);
            System.out.println("CC Base64 decode: " + new String(decodeBytes, "utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
