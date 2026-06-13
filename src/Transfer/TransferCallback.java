package Transfer;

/** Callback interface so the TCP layer can push events to the GUI. */
public interface TransferCallback {
    /** Called periodically while bytes are being transferred. 0 ≤ pct ≤ 100 */
    void onProgress(String fileName, int pct, long bytesDone, long totalBytes);
    /** Called once when the transfer completes successfully. */
    void onComplete(String fileName, String savedPath);
    /** Called if an error occurs. */
    void onError(String message);
}
