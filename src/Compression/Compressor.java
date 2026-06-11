package Compression;

import java.io.*;
import java.util.zip.*;

/**
 * Compressor: uses Java's built-in GZIP compression class.
 * Uses "GZIPOutputStream" to compress raw bytes in-memory.
 * No third-party dependencies required.
 */
public class Compressor {

    private Compressor() {}   // utility class — no instantiation

    /**
     * Compresses data using GZIP and returns the compressed bytes.
     */
    public static byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            gzip.write(data);
        }
        return bos.toByteArray();
    }
}
