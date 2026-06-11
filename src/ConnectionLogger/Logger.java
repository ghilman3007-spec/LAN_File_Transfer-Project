package ConnectionLogger;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;

/**
 * Logger: lightweight, dependency-free logger that writes timestamped
 * messages to an append-mode log file (transfer.log).
 */
public class Logger {

    private static final String LOG_FILE = "transfer.log";
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String connectionUser;    // e.g. "Server/Receiver" or "Client/Sender"

    public Logger(String connectionUser) {
        this.connectionUser = connectionUser;
    }

    public void info(String message) {
        log("INFO", message);
    }

    public void error(String message) {
        log("ERROR", message);
    }

    private void log(String level, String message) {
        String messageLine = String.format("[%s] [%s] [%s] %s",
                            LocalDateTime.now().format(FMT), 
                            level, 
                            connectionUser, 
                            message);

        if ("ERROR".equals(level)) {
            System.err.println(messageLine);
            
            // print the error message and terminate the program here when the error occurs
        }

        // File (append, create if absent)
        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(LOG_FILE),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(messageLine);
            writer.newLine();
        } catch (IOException e) {
            // Don't let logging failures crash the application
            System.err.println("[Logger] Could not write to log file: " + e.getMessage());
        }
    }
}
