package Compression;

import java.io.*;
import java.util.zip.*;

public class DeCompressor {
    private DeCompressor() {}

    public static byte[] decompress(byte[] compressedData) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPInputStream gzip = new GZIPInputStream(bis)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = gzip.read(buf)) != -1) bos.write(buf, 0, n);
        }
        return bos.toByteArray();
    }
}
