package LANconnection;

import Compression.DeCompressor;
import ConnectionLogger.Logger;

import java.io.*;
import java.net.*;

/**
 * Server: listens on a TCP port, receives compressed file data,
 * decompresses it, and saves it to the current directory.
 */
public class LANServer {

    private final int port;
    private final Logger logger;

    public LANServer(int port) {
        this.port = port;
        this.logger = new Logger("Server (Receiver)");
    }

    /** Starts the server and blocks, accepting one client at a time. */
    public void start() throws IOException {

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            String serverIP = InetAddress.getLocalHost().getHostAddress();
            String socketAddress = serverIP +":"+ port;

            System.out.println("Starting the Server...");
            System.out.println("Server IP: " + serverIP);
            logger.info("Server listening on IP & port: " + socketAddress);
            System.out.println("[Server] Listening on the Socket: " + socketAddress + ".");
            System.out.println("Waiting for clients...");

            while (true) {

                Socket clientSocket = serverSocket.accept();
                String clientAddr = clientSocket.getInetAddress().getHostAddress();
                logger.info("Client connected: " + clientAddr);
                System.out.println("[Server] Client connected: " + clientAddr);

                handleClient(clientSocket);
            }
        }
    }

    private void handleClient(Socket socket) {
        try ( DataInputStream dis = new DataInputStream(socket.getInputStream()) ) {

            //File Transfer Protocol used: 
            // [UTF filename] [Compressed file length] [Compressed file data]

            // Reading the original filename
            String fileName = dis.readUTF();
            logger.info("Receiving file: " + fileName);
            System.out.println("[Server] Receiving file: " + fileName);

            // Reading the length of the compressed file data
            long compressedLength = dis.readLong();
            logger.info("Compressed size: " + compressedLength + " bytes");

            // Reading the compressed file data
            byte[] compressedData = new byte[(int) compressedLength];
            dis.readFully(compressedData);

            // Decompressing the file data
            byte[] originalData = DeCompressor.decompress(compressedData);
            logger.info("Decompressed size: " + originalData.length + " bytes");

            // Writing the data to the output file (prefixed a custom word to avoid overwrite conflicts)
            // TODO: take a proper path from user to save the file into
            String outPath = "received_" + fileName;
            try (FileOutputStream fos = new FileOutputStream(outPath)) {
                fos.write(originalData);
            }

            logger.info("File saved as: " + outPath);
            System.out.println("[Server] File saved as: " + outPath);

        } catch (IOException e) {
            logger.error("Error handling client: " + e.getMessage());
            System.err.println("[Server] Error: " + e.getMessage());
        } finally {
            try { 
                socket.close(); 
            } 
            catch (IOException ignored) {}
        }
    }
}
