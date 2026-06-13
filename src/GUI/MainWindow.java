package GUI;

import Discovery.DiscoveredPeer;
import Discovery.UDPDiscovery;
import Transfer.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.List;

/**
 * MainWindow — VS Code Dark+ themed Swing GUI.
 *
 * Network calls (UDP scan, TCP send/receive) all run on SwingWorker
 * background threads so the UI never freezes, but the network code
 * itself is simple blocking code with no internal threading.
 *
 * Two tabs:
 *   Receive — wait for a sender, accept one file, loop
 *   Send    — scan for peers, pick one, pick a file, send
 */
public class MainWindow extends JFrame {

    // ── VS Code Dark+ palette ─────────────────────────────────────────────────
    static final Color C_BG       = new Color(0x1E, 0x1E, 0x1E);
    static final Color C_SURFACE  = new Color(0x25, 0x25, 0x26);
    static final Color C_SURFACE2 = new Color(0x2D, 0x2D, 0x2D);
    static final Color C_SURFACE3 = new Color(0x37, 0x37, 0x38);
    static final Color C_ACCENT   = new Color(0x00, 0x78, 0xD4);
    static final Color C_ACCENT_H = new Color(0x10, 0x8B, 0xE8);
    static final Color C_SUCCESS  = new Color(0x4E, 0xC9, 0x91);
    static final Color C_WARN     = new Color(0xDC, 0xDC, 0xAA);
    static final Color C_ERROR    = new Color(0xF4, 0x71, 0x6F);
    static final Color C_TEXT     = new Color(0xD4, 0xD4, 0xD4);
    static final Color C_MUTED    = new Color(0x85, 0x85, 0x85);
    static final Color C_BORDER   = new Color(0x45, 0x45, 0x45);
    // button text — dark enough to read against any button bg
    static final Color C_BTN_TEXT = new Color(0x1A, 0x1A, 0x1A);

    static final Font F_BODY  = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font F_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    static final Font F_BOLD  = new Font("Segoe UI", Font.BOLD,  13);
    static final Font F_HEAD  = new Font("Segoe UI", Font.BOLD,  15);
    static final Font F_MONO  = new Font("Consolas", Font.PLAIN, 12);

    // ── receive-tab state ─────────────────────────────────────────────────────
    private JTextField   tfRecvPort;
    private JTextField   tfSaveDir;
    private JButton      btnStartRecv;
    private JLabel       lblRecvStatus;
    private JProgressBar recvProgress;
    private JLabel       lblRecvFile;
    private boolean      recvLoopActive = false;

    // ── send-tab state ────────────────────────────────────────────────────────
    private DefaultListModel<DiscoveredPeer> peerModel;
    private JList<DiscoveredPeer>            peerList;
    private JButton      btnScan;
    private JTextField   tfFilePath;
    private JButton      btnSend;
    private JProgressBar sendProgress;
    private JLabel       lblSendStatus;

    // ── log ───────────────────────────────────────────────────────────────────
    private JTextArea logArea;

    // =========================================================================

    public MainWindow() {
        super("LAN File Transfer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(700, 680));
        setMinimumSize(new Dimension(560, 560));
        getContentPane().setBackground(C_BG);
        build();
        pack();
        setLocationRelativeTo(null);
    }

    // =========================================================================
    //  UI build
    // =========================================================================

    private void build() {
        JPanel root = panel(new BorderLayout(0, 10));
        root.setBorder(new EmptyBorder(14, 14, 10, 14));
        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildTabs(),    BorderLayout.CENTER);
        root.add(buildLog(),     BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel p = panel(new BorderLayout(10, 0));
        p.setBorder(new EmptyBorder(0, 0, 6, 0));

        JPanel left = panel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.add(new JLabel(Icons.lan(28, 28, C_ACCENT)));
        JLabel title = new JLabel("LAN File Transfer");
        title.setFont(F_HEAD);
        title.setForeground(C_TEXT);
        left.add(title);
        p.add(left, BorderLayout.WEST);

        JLabel ip = new JLabel(UDPDiscovery.getLanIP());
        ip.setFont(F_MONO);
        ip.setForeground(C_MUTED);
        p.add(ip, BorderLayout.EAST);

        JSeparator sep = new JSeparator();
        sep.setForeground(C_BORDER);
        JPanel wrap = panel(new BorderLayout());
        wrap.add(p, BorderLayout.CENTER);
        wrap.add(sep, BorderLayout.SOUTH);
        return wrap;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tp = new JTabbedPane();
        tp.setBackground(C_BG);
        tp.setForeground(C_TEXT);
        tp.setFont(F_BODY);
        tp.addTab("  ⬇  Receive  ", buildReceiveTab());
        tp.addTab("  ⬆  Send  ",    buildSendTab());
        return tp;
    }

    // ── Receive tab ───────────────────────────────────────────────────────────

    private JPanel buildReceiveTab() {
        JPanel p = card();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        p.add(sectionLabel("Receiver Settings"));
        p.add(vgap(10));

        // port
        JPanel portRow = hrow();
        portRow.add(muted("TCP Port:"));
        portRow.add(hgap(8));
        tfRecvPort = field("50001", 70);
        portRow.add(tfRecvPort);
        p.add(portRow);
        p.add(vgap(8));

        // save dir
        JPanel dirRow = hrow();
        dirRow.add(muted("Save to:"));
        dirRow.add(hgap(8));
        tfSaveDir = field(System.getProperty("user.home") + File.separator + "Downloads", 360);
        dirRow.add(tfSaveDir);
        dirRow.add(hgap(6));
        JButton bDir = ghostBtn("", Icons.folder(15, 15, C_TEXT));
        bDir.addActionListener(e -> chooseSaveDir());
        dirRow.add(bDir);
        p.add(dirRow);
        p.add(vgap(14));

        // start button
        JPanel ctrlRow = hrow();
        btnStartRecv = accentBtn("Start Listening", Icons.play(13, 13, C_BTN_TEXT));
        btnStartRecv.addActionListener(e -> startReceiveLoop());
        ctrlRow.add(btnStartRecv);
        ctrlRow.add(hgap(14));
        lblRecvStatus = statusLabel("Idle  —  press Start to wait for a sender");
        ctrlRow.add(lblRecvStatus);
        p.add(ctrlRow);
        p.add(vgap(18));

        // progress
        p.add(sectionLabel("Incoming Transfer"));
        p.add(vgap(8));
        recvProgress = progressBar();
        p.add(recvProgress);
        p.add(vgap(6));
        lblRecvFile = muted("No transfer yet.");
        p.add(lblRecvFile);

        return p;
    }

    // ── Send tab ──────────────────────────────────────────────────────────────

    private JPanel buildSendTab() {
        JPanel p = card();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        p.add(sectionLabel("Discover Peers  (UDP broadcast)"));
        p.add(vgap(8));

        peerModel = new DefaultListModel<>();
        peerList  = new JList<>(peerModel);
        peerList.setBackground(C_SURFACE2);
        peerList.setForeground(C_SUCCESS);
        peerList.setFont(F_MONO);
        peerList.setFixedCellHeight(32);
        peerList.setSelectionBackground(C_ACCENT);
        peerList.setSelectionForeground(Color.WHITE);
        peerList.setCellRenderer(new PeerRenderer());
        peerList.setBorder(new EmptyBorder(4, 6, 4, 6));

        JScrollPane scroll = new JScrollPane(peerList);
        scroll.setPreferredSize(new Dimension(0, 120));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        scroll.setBorder(BorderFactory.createLineBorder(C_BORDER, 1));
        scroll.getViewport().setBackground(C_SURFACE2);
        p.add(scroll);
        p.add(vgap(8));

        JPanel scanRow = hrow();
        btnScan = ghostBtn("Scan for Peers", Icons.refresh(14, 14, C_TEXT));
        btnScan.addActionListener(e -> doScan());
        scanRow.add(btnScan);
        scanRow.add(hgap(10));
        scanRow.add(muted("Broadcasts DISCOVER — peers in Receive mode will reply."));
        p.add(scanRow);
        p.add(vgap(18));

        p.add(sectionLabel("File to Send"));
        p.add(vgap(8));

        JPanel fileRow = hrow();
        tfFilePath = field("No file selected…", 370);
        tfFilePath.setEditable(false);
        fileRow.add(tfFilePath);
        fileRow.add(hgap(6));
        JButton bFile = ghostBtn("Browse", Icons.file(13, 13, C_TEXT));
        bFile.addActionListener(e -> chooseFile());
        fileRow.add(bFile);
        p.add(fileRow);
        p.add(vgap(12));

        JPanel sendRow = hrow();
        btnSend = accentBtn("Send File", Icons.upload(13, 13, C_BTN_TEXT));
        btnSend.addActionListener(e -> doSend());
        sendRow.add(btnSend);
        p.add(sendRow);
        p.add(vgap(18));

        p.add(sectionLabel("Transfer Progress"));
        p.add(vgap(8));
        sendProgress = progressBar();
        p.add(sendProgress);
        p.add(vgap(6));
        lblSendStatus = muted("No transfer yet.");
        p.add(lblSendStatus);

        return p;
    }

    // ── Log ───────────────────────────────────────────────────────────────────

    private JPanel buildLog() {
        JPanel p = panel(new BorderLayout(0, 4));
        p.setBorder(new EmptyBorder(8, 0, 0, 0));

        JLabel lbl = new JLabel("Activity Log");
        lbl.setFont(F_BOLD);
        lbl.setForeground(C_MUTED);
        p.add(lbl, BorderLayout.NORTH);

        logArea = new JTextArea(6, 0);
        logArea.setEditable(false);
        logArea.setBackground(new Color(0x12, 0x12, 0x12));
        logArea.setForeground(new Color(0xCC, 0xCC, 0xCC));
        logArea.setFont(F_MONO);
        logArea.setBorder(new EmptyBorder(6, 8, 6, 8));

        JScrollPane sp = new JScrollPane(logArea);
        sp.setBorder(BorderFactory.createLineBorder(C_BORDER, 1));
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // =========================================================================
    //  Logic — Receive
    // =========================================================================

    /**
     * Starts the receive loop.  Each iteration:
     *   1. Reply to one DISCOVER via UDP (so the sender can find us)
     *   2. Accept one TCP file transfer
     * After each file the loop asks the user if they want to receive another.
     */
    private void startReceiveLoop() {
        if (recvLoopActive) return;

        int port;
        try { port = Integer.parseInt(tfRecvPort.getText().trim()); }
        catch (NumberFormatException ex) { err("Invalid TCP port."); return; }

        String saveDir = tfSaveDir.getText().trim();
        if (saveDir.isEmpty()) { err("Choose a save directory."); return; }

        recvLoopActive = true;
        btnStartRecv.setEnabled(false);
        setStatus(lblRecvStatus, "● Waiting for DISCOVER…", C_WARN);
        log("Receiver started — TCP:" + port + "  save → " + saveDir);

        final int fPort = port;
        final String fDir = saveDir;

        // SwingWorker keeps the UI responsive while we block on network calls
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                // Step 1: reply to UDP DISCOVER (wait up to 60 s)
                UDPDiscovery udp = new UDPDiscovery();
                String hostname  = getHostLabel();
                boolean found    = udp.waitForDiscover(hostname, fPort, 60_000);

                if (!found) {
                    publish("TIMEOUT");
                    return null;
                }

                publish("DISCOVER_OK");

                // Step 2: accept one TCP file
                TransferCallback cb = new TransferCallback() {
                    @Override public void onProgress(String name, int pct, long done, long total) {
                        SwingUtilities.invokeLater(() -> {
                            recvProgress.setValue(pct);
                            lblRecvFile.setText(name + "  —  " + human(done) + " / " + human(total));
                            lblRecvFile.setForeground(C_TEXT);
                        });
                    }
                    @Override public void onComplete(String name, String path) {
                        SwingUtilities.invokeLater(() -> {
                            recvProgress.setValue(100);
                            lblRecvFile.setText("✓  Saved: " + path);
                            lblRecvFile.setForeground(C_SUCCESS);
                        });
                    }
                    @Override public void onError(String msg) {
                        SwingUtilities.invokeLater(() -> {
                            lblRecvFile.setText("Error: " + msg);
                            lblRecvFile.setForeground(C_ERROR);
                        });
                    }
                };

                FileReceiver receiver = new FileReceiver(fPort, fDir, cb);
                receiver.acceptOne();   // blocks until file received
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                String latest = chunks.get(chunks.size() - 1);
                if ("DISCOVER_OK".equals(latest)) {
                    setStatus(lblRecvStatus, "● Sender found — receiving file…", C_SUCCESS);
                    log("DISCOVER replied — waiting for TCP connection on :" + fPort);
                } else if ("TIMEOUT".equals(latest)) {
                    setStatus(lblRecvStatus, "Timed out — no sender found.", C_WARN);
                    log("No DISCOVER received within 60 s.");
                }
            }

            @Override
            protected void done() {
                recvLoopActive = false;
                btnStartRecv.setEnabled(true);
                setStatus(lblRecvStatus, "Done  —  press Start to listen again.", C_MUTED);
                log("Receiver finished.");
            }
        };

        worker.execute();
    }

    // =========================================================================
    //  Logic — Scan
    // =========================================================================

    private void doScan() {
        peerModel.clear();
        btnScan.setEnabled(false);
        log("Scanning… (broadcasting DISCOVER, waiting 2 s for replies)");

        new SwingWorker<List<DiscoveredPeer>, Void>() {
            @Override
            protected List<DiscoveredPeer> doInBackground() {
                return new UDPDiscovery().scanOnce();
            }
            @Override
            protected void done() {
                btnScan.setEnabled(true);
                try {
                    List<DiscoveredPeer> peers = get();
                    for (DiscoveredPeer p : peers) peerModel.addElement(p);
                    if (peers.isEmpty()) log("No peers found — make sure the other device is in Receive mode.");
                    else                 log("Found " + peers.size() + " peer(s).");
                } catch (Exception ex) {
                    log("Scan error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    // =========================================================================
    //  Logic — Send
    // =========================================================================

    private void doSend() {
        DiscoveredPeer target = peerList.getSelectedValue();
        if (target == null)                               { err("Select a peer from the list."); return; }
        String filePath = tfFilePath.getText().trim();
        if (filePath.isEmpty() || filePath.startsWith("No file")) { err("Choose a file to send."); return; }

        btnSend.setEnabled(false);
        sendProgress.setValue(0);
        setStatus(lblSendStatus, "Connecting to " + target.ip + ":" + target.tcpPort + "…", C_WARN);
        log("Sending " + new File(filePath).getName() + " → " + target);

        final String fPath  = filePath;
        final String fIp    = target.ip;
        final int    fPort  = target.tcpPort;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                TransferCallback cb = new TransferCallback() {
                    @Override public void onProgress(String name, int pct, long done, long total) {
                        SwingUtilities.invokeLater(() -> {
                            sendProgress.setValue(pct);
                            lblSendStatus.setText(name + "  —  " + human(done) + " / " + human(total));
                            lblSendStatus.setForeground(C_TEXT);
                        });
                    }
                    @Override public void onComplete(String name, String path) {
                        SwingUtilities.invokeLater(() -> {
                            sendProgress.setValue(100);
                            lblSendStatus.setText("✓  Sent: " + name);
                            lblSendStatus.setForeground(C_SUCCESS);
                            log("Transfer complete: " + name);
                        });
                    }
                    @Override public void onError(String msg) {
                        SwingUtilities.invokeLater(() -> {
                            lblSendStatus.setText("Error: " + msg);
                            lblSendStatus.setForeground(C_ERROR);
                            log("Send error: " + msg);
                        });
                    }
                };
                new FileSender(cb).send(fPath, fIp, fPort);
                return null;
            }
            @Override
            protected void done() {
                btnSend.setEnabled(true);
            }
        }.execute();
    }

    // =========================================================================
    //  File choosers
    // =========================================================================

    private void chooseSaveDir() {
        JFileChooser fc = new JFileChooser(tfSaveDir.getText());
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            tfSaveDir.setText(fc.getSelectedFile().getAbsolutePath());
    }

    private void chooseFile() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            tfFilePath.setText(fc.getSelectedFile().getAbsolutePath());
    }

    // =========================================================================
    //  Helpers
    // =========================================================================

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            String ts = java.time.LocalTime.now().toString().substring(0, 8);
            logArea.append(ts + "  " + msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private static void err(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static void setStatus(JLabel l, String text, Color c) {
        l.setText(text); l.setForeground(c);
    }

    private static String getHostLabel() {
        try { return InetAddress.getLocalHost().getHostName(); }
        catch (Exception e) { return "Peer"; }
    }

    private static String human(long bytes) {
        if (bytes < 1024) return bytes + " B";
        DecimalFormat df = new DecimalFormat("0.0");
        if (bytes < 1024 * 1024) return df.format(bytes / 1024.0) + " KB";
        return df.format(bytes / (1024.0 * 1024)) + " MB";
    }

    // =========================================================================
    //  Widget factories
    // =========================================================================

    static JPanel panel(LayoutManager lm) {
        JPanel p = new JPanel(lm); p.setBackground(C_BG); return p;
    }

    static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(C_SURFACE);
        p.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(C_BORDER, 1),
                new EmptyBorder(16, 16, 16, 16)));
        return p;
    }

    static JPanel hrow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setBackground(C_SURFACE);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        return p;
    }

    static JLabel sectionLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(F_BOLD); l.setForeground(C_TEXT);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    static JLabel muted(String t) {
        JLabel l = new JLabel(t); l.setFont(F_SMALL); l.setForeground(C_MUTED); return l;
    }

    static JLabel statusLabel(String t) {
        JLabel l = new JLabel(t); l.setFont(F_SMALL); l.setForeground(C_MUTED); return l;
    }

    static JTextField field(String placeholder, int width) {
        JTextField tf = new JTextField(placeholder) {
            @Override public Dimension getPreferredSize() { return new Dimension(width, 26); }
            @Override public Dimension getMaximumSize()   { return new Dimension(width, 26); }
        };
        tf.setBackground(C_SURFACE2); tf.setForeground(C_TEXT); tf.setCaretColor(C_TEXT);
        tf.setSelectionColor(C_ACCENT); tf.setSelectedTextColor(Color.WHITE);
        tf.setFont(F_BODY);
        tf.setBorder(new CompoundBorder(BorderFactory.createLineBorder(C_BORDER),
                new EmptyBorder(2, 8, 2, 8)));
        return tf;
    }

    static JProgressBar progressBar() {
        JProgressBar pb = new JProgressBar(0, 100);
        pb.setForeground(C_ACCENT);
        pb.setBackground(C_SURFACE3);
        pb.setBorderPainted(false);
        pb.setStringPainted(false);
        pb.setPreferredSize(new Dimension(Integer.MAX_VALUE, 10));
        pb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        pb.setAlignmentX(LEFT_ALIGNMENT);
        return pb;
    }

    /**
     * Accent button — blue background, DARK text so it's clearly readable.
     * Using C_BTN_TEXT (#1A1A1A) instead of white gives high contrast
     * against the blue (#0078D4) and matches the VS Code style.
     */
    static JButton accentBtn(String text, Icon icon) {
        JButton b = new JButton(text, icon);
        b.setFont(F_BOLD);
        b.setForeground(C_BTN_TEXT);      // dark text on blue bg
        b.setBackground(C_ACCENT);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(C_ACCENT.darker()),
                new EmptyBorder(6, 16, 6, 16)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setIconTextGap(6);
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(C_ACCENT_H); }
            @Override public void mouseExited (MouseEvent e) { b.setBackground(C_ACCENT);   }
        });
        return b;
    }

    /**
     * Ghost / secondary button — dark surface background, light-grey text.
     */
    static JButton ghostBtn(String text, Icon icon) {
        JButton b = new JButton(text, icon);
        b.setFont(F_SMALL);
        b.setForeground(new Color(0xCC, 0xCC, 0xCC));  // readable on dark surface
        b.setBackground(C_SURFACE3);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(C_BORDER),
                new EmptyBorder(4, 10, 4, 10)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setIconTextGap(text.isEmpty() ? 0 : 5);
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(new Color(0x44,0x44,0x44)); }
            @Override public void mouseExited (MouseEvent e) { b.setBackground(C_SURFACE3); }
        });
        return b;
    }

    static Component vgap(int h) { return Box.createVerticalStrut(h); }
    static Component hgap(int w) { return Box.createHorizontalStrut(w); }

    // =========================================================================
    //  Peer list cell renderer
    // =========================================================================

    static class PeerRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int idx, boolean sel, boolean focus) {
            JLabel l = (JLabel) super.getListCellRendererComponent(list, value, idx, sel, focus);
            if (value instanceof DiscoveredPeer p) {
                l.setIcon(Icons.wifi(14, 14, sel ? Color.WHITE : C_SUCCESS));
                l.setText("  " + p.displayName + "   ·   " + p.ip + "   :   " + p.tcpPort);
                l.setFont(F_MONO);
                l.setIconTextGap(6);
            }
            l.setBackground(sel ? C_ACCENT : C_SURFACE2);
            l.setForeground(sel ? Color.WHITE : C_SUCCESS);
            l.setBorder(new EmptyBorder(5, 6, 5, 6));
            return l;
        }
    }
}
