package Compression;

import java.io.*;
import java.util.zip.*;

/**
 * DeCompressor: uses Java's built-in GZIP decompression,
 * paired with GZIP compression.
 */
public class DeCompressor {

    private DeCompressor() {}   // utility class — no instantiation

    /**
     * Decompresses GZIP-compressed data and returns the original bytes.
    */
    public static byte[] decompress(byte[] compressedData) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPInputStream gzip = new GZIPInputStream(bis)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = gzip.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
        }
        return bos.toByteArray();
    }
}
