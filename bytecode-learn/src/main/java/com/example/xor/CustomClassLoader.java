package com.example.xor;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class CustomClassLoader extends ClassLoader {

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        CustomClassLoader classLoader = new CustomClassLoader();
        Class<?> clazz = classLoader.loadClass("com.example.xor.MyService");
        clazz.newInstance();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = getClassFileByByteInDir(name);
        byte[] decodedBytes = decodeClassBytes(bytes);
        return defineClass(name, decodedBytes, 0, bytes.length);
    }

    private byte[] decodeClassBytes(byte[] bytes) {
        byte[] decodedBytes = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            decodedBytes[i] = (byte) (bytes[i] ^ 0xFF);
        }
        return decodedBytes;
    }

    private byte[] getClassFileByByteInDir(String name) {
        try {
            return FileUtils.readFileToByteArray(new File(name));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static native byte[] decryptJni(byte[] bytes);
}
