package ConnectionLogger;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;

public class Logger {

    private static final String LOG_FILE = "transfer.log";
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String role;

    public Logger(String role) { this.role = role; }

    public void info(String msg)  { log("INFO",  msg); }
    public void error(String msg) { log("ERROR", msg); }

    private void log(String level, String msg) {
        String line = String.format("[%s] [%s] [%s] %s",
                LocalDateTime.now().format(FMT), level, role, msg);
        System.out.println(line);
        try (BufferedWriter w = Files.newBufferedWriter(Paths.get(LOG_FILE),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(line); w.newLine();
        } catch (IOException ignored) {}
    }
}
