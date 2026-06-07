package jfr_sample;

import java.io.*;
import java.util.*;

/**
 * Minimal JFR event generator for GraalVM native-image builds.
 * Kept small so native-image compilation finishes quickly on CI.
 */
public class NativeMain {

    static volatile Object sink;

    public static void main(String[] args) throws Exception {
        long end = System.currentTimeMillis() + 20_000;

        while (System.currentTimeMillis() < end) {
            allocateAndGC();
            fileIO();
            throwAndCatch();
            Thread.sleep(5);
            System.gc();
        }
    }

    static void allocateAndGC() {
        List<byte[]> list = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) list.add(new byte[4096]);
        sink = list;
        sink = null;
    }

    static File tmpFile;
    static {
        try { tmpFile = File.createTempFile("jfr_native_sample", ".tmp"); tmpFile.deleteOnExit(); }
        catch (IOException e) { tmpFile = null; }
    }

    static void fileIO() throws Exception {
        if (tmpFile == null) return;
        try (FileOutputStream fos = new FileOutputStream(tmpFile, true)) { fos.write(new byte[256]); }
        try (FileInputStream fis = new FileInputStream(tmpFile)) { fis.read(new byte[256]); }
    }

    static void throwAndCatch() {
        try { throw new RuntimeException("jfr_native_sample"); } catch (RuntimeException e) { /* ok */ }
    }
}
