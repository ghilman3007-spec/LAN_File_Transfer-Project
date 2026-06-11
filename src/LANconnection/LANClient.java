package LANconnection;

import Compression.Compressor;
import ConnectionLogger.Logger;

import java.io.*;
import java.net.*;
import java.nio.file.*;

/**
 * Client: reads a file from disk, compresses it, and sends it
 * over TCP to the waiting Server.
 */
public class LANClient {

    private final int serverPort;
    private final String serverIp;
    private final Logger logger;

    public LANClient(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.logger = new Logger("Client (Sender)");
    }

    /**
     * Compresses and sends the file at {@code filePath} to the server.
     */
    public void sendFile(String filePath) throws IOException {

        File file = new File(filePath);

        if (!file.exists() || !file.isFile()) {
            logger.error("File not found: " + filePath);
            System.err.println("[Client] File not found: " + filePath);
            return;
        }

        // Reading raw bytes
        byte[] rawData = Files.readAllBytes(file.toPath());
        logger.info("Original size: " + rawData.length + " bytes");

        // Compressing the file
        byte[] compressedData = Compressor.compress(rawData);
        logger.info("Compressed size: " + compressedData.length + " bytes  "
                + String.format("(%.2f%% of original)", 100.0 * compressedData.length / rawData.length));

        // Creating a connection with the Receiver and sending the compressed file
        try (
            Socket socket = new Socket(serverIp, serverPort);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream())
        ) {

            logger.info("Connected to " + serverIp + ":" + serverPort);
            System.out.println("[Client] Connected. Sending file: " + file.getName());

            // File Transfer Protocol: 
            // [UTF filename] [Compressed file length] [Compressed file data]
            dos.writeUTF(file.getName());
            dos.writeLong(compressedData.length);
            dos.write(compressedData);
            dos.flush();


            /**
             * Rough Idea of the File Transfer Protocol used:
             ---------------------------------
             | -filename
             | -filesize
             |
             | -filedata
             | ....
             | ....
             | ....
             | ....
             */

            logger.info("Transfer complete: " + file.getName());
            System.out.println("[Client] Transfer complete.");
        }
    }
}
