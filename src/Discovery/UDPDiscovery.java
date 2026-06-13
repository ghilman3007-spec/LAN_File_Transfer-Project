package Discovery;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Simple single-threaded UDP discovery — no thread pools, no daemon threads.
 *
 * SENDER  → call {@link #scanOnce()} to broadcast DISCOVER and collect all
 *           replies that arrive within 2 seconds. Returns the list.
 *
 * RECEIVER → call {@link #waitForDiscover(String, int)} to block until one
 *            DISCOVER arrives, reply with HERE|name|ip|port, then return.
 *            The GUI calls this once per accepted TCP connection in a simple loop.
 *
 * Wire format (identical to the demo files supplied):
 *   Sender  sends:  "DISCOVER"
 *   Receiver sends: "HERE|<displayName>|<ip>|<tcpPort>"
 */
public class UDPDiscovery {

    public static final int DISCOVERY_PORT = 50000;

    private static final String DISCOVER_MSG  = "DISCOVER";
    private static final String HERE_PREFIX   = "HERE";
    private static final int    SCAN_WAIT_MS  = 2000;   // how long to collect replies
    private static final int    RECV_TO_MS    = 500;    // socket read timeout
    private static final int    BUF           = 2048;

    // -------------------------------------------------------------------------
    //  SENDER side — one blocking call, returns what it found
    // -------------------------------------------------------------------------

    /**
     * Broadcasts DISCOVER once, waits {@value #SCAN_WAIT_MS} ms for replies,
     * and returns every unique peer that replied.
     * Filters out replies that came from this machine's own IP addresses.
     */
    public List<DiscoveredPeer> scanOnce() {
        List<DiscoveredPeer> found = new ArrayList<>();
        Set<String> seen    = new HashSet<>();
        Set<String> selfIPs = getSelfIPs();

        try (DatagramSocket sock = new DatagramSocket()) {
            sock.setBroadcast(true);
            sock.setSoTimeout(RECV_TO_MS);

            // send the broadcast
            byte[]         discBytes = DISCOVER_MSG.getBytes(StandardCharsets.UTF_8);
            InetAddress    broadcast = InetAddress.getByName("255.255.255.255");
            DatagramPacket sendPkt   = new DatagramPacket(discBytes, discBytes.length,
                                                          broadcast, DISCOVERY_PORT);
            sock.send(sendPkt);
            System.out.println("[UDPDiscovery] DISCOVER broadcast sent");

            // collect replies until our time window closes
            long deadline = System.currentTimeMillis() + SCAN_WAIT_MS;
            while (System.currentTimeMillis() < deadline) {
                try {
                    byte[]         buf = new byte[BUF];
                    DatagramPacket rx  = new DatagramPacket(buf, buf.length);
                    sock.receive(rx);

                    String senderIp = rx.getAddress().getHostAddress();
                    if (selfIPs.contains(senderIp)) continue;   // skip self

                    String        msg = new String(rx.getData(), 0, rx.getLength(), StandardCharsets.UTF_8);
                    DiscoveredPeer dp = DiscoveredPeer.parse(msg);
                    if (dp != null && seen.add(dp.ip)) {
                        found.add(dp);
                        System.out.println("[UDPDiscovery] Found peer: " + dp);
                    }
                } catch (SocketTimeoutException ignored) { /* keep polling until deadline */ }
            }

        } catch (Exception e) {
            System.err.println("[UDPDiscovery] scanOnce error: " + e.getMessage());
        }

        return found;
    }

    // -------------------------------------------------------------------------
    //  RECEIVER side — blocks until one DISCOVER arrives, replies, returns
    // -------------------------------------------------------------------------

    /**
     * Binds to {@link #DISCOVERY_PORT}, blocks until a DISCOVER packet arrives,
     * sends back "HERE|displayName|ip|tcpPort", and returns.
     *
     * The GUI calls this once right before starting to listen for TCP connections
     * so the sender can find us.  If no DISCOVER arrives within
     * {@code timeoutMs} milliseconds it returns false (timed out / no scanner nearby).
     */
    public boolean waitForDiscover(String displayName, int tcpPort, int timeoutMs) {
        String myIp   = getLanIP();
        String reply  = HERE_PREFIX + "|" + displayName + "|" + myIp + "|" + tcpPort;
        byte[] replyB = reply.getBytes(StandardCharsets.UTF_8);

        try (DatagramSocket sock = new DatagramSocket(null)) {
            sock.setReuseAddress(true);
            sock.bind(new InetSocketAddress(DISCOVERY_PORT));
            sock.setBroadcast(true);
            sock.setSoTimeout(timeoutMs);

            System.out.println("[UDPDiscovery] Waiting for DISCOVER on UDP:" + DISCOVERY_PORT);

            byte[]         buf = new byte[BUF];
            DatagramPacket rx  = new DatagramPacket(buf, buf.length);
            sock.receive(rx);                                     // blocks until DISCOVER or timeout

            String msg = new String(rx.getData(), 0, rx.getLength(), StandardCharsets.UTF_8);
            if (DISCOVER_MSG.equals(msg.trim())) {
                DatagramPacket resp = new DatagramPacket(replyB, replyB.length,
                                                         rx.getAddress(), rx.getPort());
                sock.send(resp);
                System.out.println("[UDPDiscovery] Replied HERE to " + rx.getAddress().getHostAddress());
                return true;
            }
        } catch (SocketTimeoutException e) {
            System.out.println("[UDPDiscovery] No DISCOVER received within " + timeoutMs + " ms");
        } catch (Exception e) {
            System.err.println("[UDPDiscovery] waitForDiscover error: " + e.getMessage());
        }
        return false;
    }

    // -------------------------------------------------------------------------
    //  helpers
    // -------------------------------------------------------------------------

    /** Returns the first non-loopback IPv4 LAN address. */
    public static String getLanIP() {
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface ni = ifaces.nextElement();
                if (!ni.isUp() || ni.isLoopback()) continue;
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress a = addrs.nextElement();
                    if (a instanceof Inet4Address && !a.isLoopbackAddress())
                        return a.getHostAddress();
                }
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) { return "127.0.0.1"; }
    }

    private static Set<String> getSelfIPs() {
        Set<String> ips = new HashSet<>();
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                Enumeration<InetAddress> addrs = ifaces.nextElement().getInetAddresses();
                while (addrs.hasMoreElements()) ips.add(addrs.nextElement().getHostAddress());
            }
        } catch (Exception ignored) {}
        return ips;
    }
}
