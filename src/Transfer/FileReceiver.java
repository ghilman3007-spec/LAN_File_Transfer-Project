package Transfer;

import Compression.DeCompressor;
import ConnectionLogger.Logger;

import java.io.*;
import java.net.*;
import java.nio.file.*;

/**
 * Simple blocking TCP receiver — no threads, no executor pools.
 *
 * Usage (on the GUI's background SwingWorker):
 *   FileReceiver r = new FileReceiver(port, saveDir, callback);
 *   r.acceptOne();   // blocks until one file arrives, then returns
 *
 * The GUI calls acceptOne() in a loop (one per expected transfer)
 * so it stays simple and single-threaded on the network side.
 */
public class FileReceiver {

    private static final int CHUNK   = 65_536;
    private static final int BACKLOG = 1;       // only one sender at a time

    private final int              tcpPort;
    private final String           saveDir;
    private final Logger           logger;
    private final TransferCallback callback;

    public FileReceiver(int tcpPort, String saveDir, TransferCallback callback) {
        this.tcpPort   = tcpPort;
        this.saveDir   = saveDir;
        this.callback  = callback;
        this.logger    = new Logger("FileReceiver");
    }

    /**
     * Opens a ServerSocket, waits for exactly ONE sender to connect,
     * receives the file, saves it, then closes everything.
     * Returns the path where the file was saved, or null on error.
     *
     * Call this method from a background thread (e.g. SwingWorker.doInBackground).
     */
    public String acceptOne() {
        try {
            Files.createDirectories(Paths.get(saveDir));
        } catch (IOException e) {
            if (callback != null) callback.onError("Cannot create save directory: " + e.getMessage());
            return null;
        }

        try (ServerSocket server = new ServerSocket(tcpPort, BACKLOG)) {
            server.setReuseAddress(true);
            logger.info("Waiting for sender on TCP:" + tcpPort);

            try (Socket sock = server.accept()) {
                logger.info("Sender connected: " + sock.getInetAddress().getHostAddress());
                return receiveFile(sock);
            }

        } catch (IOException e) {
            logger.error("acceptOne error: " + e.getMessage());
            if (callback != null) callback.onError(e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------------------------

    private String receiveFile(Socket sock) throws IOException {
        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(sock.getInputStream()))) {

            // header: filename + compressed length
            String fileName         = dis.readUTF();
            long   compressedLength = dis.readLong();
            logger.info("Receiving: " + fileName + " (" + compressedLength + " bytes compressed)");

            // read body in chunks so the progress bar updates
            byte[] compressed = new byte[(int) compressedLength];
            long   received   = 0;
            int    offset     = 0;

            while (received < compressedLength) {
                int toRead = (int) Math.min(CHUNK, compressedLength - received);
                int n      = dis.read(compressed, offset, toRead);
                if (n < 0) throw new EOFException("Unexpected EOF during receive");
                offset   += n;
                received += n;

                int pct = (int) (received * 100 / compressedLength);
                if (callback != null) callback.onProgress(fileName, pct, received, compressedLength);
            }

            // decompress + save
            byte[] original = DeCompressor.decompress(compressed);
            Path   outPath  = resolveUnique(saveDir, fileName);
            Files.write(outPath, original);

            String saved = outPath.toAbsolutePath().toString();
            logger.info("Saved: " + saved);
            if (callback != null) callback.onComplete(fileName, saved);
            return saved;
        }
    }

    /** Appends (1), (2), … to avoid overwriting existing files. */
    private static Path resolveUnique(String dir, String name) {
        Path p = Paths.get(dir, name);
        if (!Files.exists(p)) return p;
        String base = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;
        String ext  = name.contains(".") ? name.substring(name.lastIndexOf('.'))    : "";
        for (int i = 1; i < 9_999; i++) {
            p = Paths.get(dir, base + "(" + i + ")" + ext);
            if (!Files.exists(p)) return p;
        }
        return Paths.get(dir, System.currentTimeMillis() + "_" + name);
    }
}
