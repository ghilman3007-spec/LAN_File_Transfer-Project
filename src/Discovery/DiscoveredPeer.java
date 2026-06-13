package Discovery;

/**
 * Immutable value object representing a peer found via UDP broadcast.
 * Wire format the server sends back:
 *   HERE|<displayName>|<ip>|<tcpPort>
 */
public class DiscoveredPeer {

    public final String displayName;
    public final String ip;
    public final int    tcpPort;

    public DiscoveredPeer(String displayName, String ip, int tcpPort) {
        this.displayName = displayName;
        this.ip          = ip;
        this.tcpPort     = tcpPort;
    }

    /** Parse "HERE|name|ip|port" — returns null on any format error. */
    public static DiscoveredPeer parse(String raw) {
        if (raw == null || !raw.startsWith("HERE|")) return null;
        String[] p = raw.split("\\|", 4);
        if (p.length < 4) return null;
        try {
            return new DiscoveredPeer(p[1].trim(), p[2].trim(), Integer.parseInt(p[3].trim()));
        } catch (NumberFormatException e) { return null; }
    }

    @Override
    public String toString() {
        return displayName + "  ·  " + ip + ":" + tcpPort;
    }
}
