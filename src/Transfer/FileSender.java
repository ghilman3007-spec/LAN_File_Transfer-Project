package Transfer;

import Compression.Compressor;
import ConnectionLogger.Logger;

import java.io.*;
import java.net.*;
import java.nio.file.*;

/**
 * Simple blocking TCP sender — no threads.
 *
 * Usage (on the GUI's background SwingWorker):
 *   FileSender s = new FileSender(callback);
 *   s.send(filePath, ip, port);   // blocks until done, then returns
 */
public class FileSender {

    private static final int CHUNK           = 65_536;
    private static final int CONNECT_TIMEOUT = 6_000;

    private final Logger           logger;
    private final TransferCallback callback;

    public FileSender(TransferCallback callback) {
        this.callback = callback;
        this.logger   = new Logger("FileSender");
    }

    /**
     * Compresses {@code filePath} and streams it to {@code ip:port}.
     * Blocks until the transfer completes or fails.
     * Call from a background thread (e.g. SwingWorker.doInBackground).
     */
    public void send(String filePath, String ip, int port) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            if (callback != null) callback.onError("File not found: " + filePath);
            return;
        }

        try {
            // compress whole file first
            byte[] raw        = Files.readAllBytes(file.toPath());
            byte[] compressed = Compressor.compress(raw);
            logger.info(String.format("Compressed %s: %d → %d bytes",
                    file.getName(), raw.length, compressed.length));

            // connect + send
            try (Socket sock = new Socket()) {
                sock.connect(new InetSocketAddress(ip, port), CONNECT_TIMEOUT);
                logger.info("Connected to " + ip + ":" + port);

                DataOutputStream dos = new DataOutputStream(
                        new BufferedOutputStream(sock.getOutputStream()));

                // header
                dos.writeUTF(file.getName());
                dos.writeLong(compressed.length);

                // body with progress
                long sent   = 0;
                int  offset = 0;
                while (sent < compressed.length) {
                    int toWrite = (int) Math.min(CHUNK, compressed.length - sent);
                    dos.write(compressed, offset, toWrite);
                    offset += toWrite;
                    sent   += toWrite;

                    int pct = (int) (sent * 100 / compressed.length);
                    if (callback != null) callback.onProgress(file.getName(), pct, sent, compressed.length);
                }
                dos.flush();
                logger.info("Transfer complete: " + file.getName());
                if (callback != null) callback.onComplete(file.getName(), null);
            }

        } catch (IOException e) {
            logger.error("Send error: " + e.getMessage());
            if (callback != null) callback.onError(e.getMessage());
        }
    }
}
