package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

/**
 * SVG-style icon factory — every icon is drawn programmatically with Java2D
 * (anti-aliased, vector-quality at any size).  No external image files needed.
 *
 * Each method returns an {@link Icon} you can drop straight into a JButton/JLabel.
 */
public final class Icons {

    private Icons() {}

    // ── LAN / network ────────────────────────────────────────────────────────

    /** Three connected nodes forming a simple network diagram. */
    public static Icon lan(int w, int h, Color c) {
        return draw(w, h, g -> {
            g.setColor(c);
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // centre node
            double cx = w * 0.5, cy = h * 0.42;
            double r  = w * 0.10;
            // three satellite nodes
            double[][] nodes = {
                { w * 0.18, h * 0.72 },
                { w * 0.82, h * 0.72 },
                { w * 0.50, h * 0.12 }
            };
            for (double[] n : nodes) {
                g.draw(new Line2D.Double(cx, cy, n[0], n[1]));
                g.fill(new Ellipse2D.Double(n[0] - r, n[1] - r, 2*r, 2*r));
            }
            g.fill(new Ellipse2D.Double(cx - r*1.3, cy - r*1.3, 2*r*1.3, 2*r*1.3));
        });
    }

    /** Wi-Fi arcs (receiver icon in the peer list). */
    public static Icon wifi(int w, int h, Color c) {
        return draw(w, h, g -> {
            g.setColor(c);
            g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            double cx = w * 0.5, cy = h * 0.78, r = w * 0.09;
            g.fill(new Ellipse2D.Double(cx - r, cy - r, 2*r, 2*r));
            double[] radii = { w * 0.28, w * 0.46, w * 0.64 };
            for (double radius : radii) {
                Arc2D arc = new Arc2D.Double(cx - radius, cy - radius,
                        2*radius, 2*radius, 35, 110, Arc2D.OPEN);
                g.draw(arc);
            }
        });
    }

    /** Refresh / rescan circular arrow. */
    public static Icon refresh(int w, int h, Color c) {
        return draw(w, h, g -> {
            g.setColor(c);
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            double cx = w * 0.5, cy = h * 0.5, r = w * 0.38;
            Arc2D arc = new Arc2D.Double(cx - r, cy - r, 2*r, 2*r, 90, 270, Arc2D.OPEN);
            g.draw(arc);
            // arrowhead at end of arc (270° → at (cx+r, cy))
            double ax = cx + r, ay = cy;
            int as = (int)(w * 0.18);
            g.fillPolygon(
                new int[]{ (int)ax, (int)(ax - as), (int)(ax - as) },
                new int[]{ (int)ay, (int)(ay - as), (int)(ay + as) }, 3);
        });
    }

    /** Upload arrow (send button). */
    public static Icon upload(int w, int h, Color c) {
        return draw(w, h, g -> {
            g.setColor(c);
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            double cx = w * 0.5;
            // vertical line
            g.draw(new Line2D.Double(cx, h * 0.88, cx, h * 0.38));
            // arrowhead
            g.draw(new Line2D.Double(cx, h * 0.16, w * 0.20, h * 0.44));
            g.draw(new Line2D.Double(cx, h * 0.16, w * 0.80, h * 0.44));
            // base bar
            g.draw(new Line2D.Double(w * 0.18, h * 0.88, w * 0.82, h * 0.88));
        });
    }

    /** Play / start triangle. */
    public static Icon play(int w, int h, Color c) {
        return draw(w, h, g -> {
            g.setColor(c);
            int[] xs = { (int)(w*0.22), (int)(w*0.22), (int)(w*0.84) };
            int[] ys = { (int)(h*0.12), (int)(h*0.88), (int)(h*0.50) };
            g.fillPolygon(xs, ys, 3);
        });
    }

    /** Folder icon (directory chooser). */
    public static Icon folder(int w, int h, Color c) {
        return draw(w, h, g -> {
            g.setColor(c);
            // body
            RoundRectangle2D body = new RoundRectangle2D.Double(
                    w*0.04, h*0.32, w*0.92, h*0.58, 3, 3);
            g.fill(body);
            // tab
            g.fillRoundRect((int)(w*0.04), (int)(h*0.18), (int)(w*0.44), (int)(h*0.20), 3, 3);
            // highlight
            g.setColor(c.brighter().brighter());
            g.setStroke(new BasicStroke(0.8f));
            g.draw(body);
        });
    }

    /** Document / file icon. */
    public static Icon file(int w, int h, Color c) {
        return draw(w, h, g -> {
            g.setColor(c);
            // body
            int fold = (int)(w * 0.28);
            int[] xs = { (int)(w*0.10), (int)(w*0.10), (int)(w*0.90), (int)(w*0.90), (int)(w*0.72), (int)(w*0.72) };
            int[] ys = { (int)(h*0.04), (int)(h*0.96), (int)(h*0.96), (int)(h*0.04)+fold, (int)(h*0.04), (int)(h*0.04)+fold };
            // draw without the fold
            g.fillPolygon(
                new int[]{ (int)(w*0.10),(int)(w*0.10),(int)(w*0.90),(int)(w*0.90),(int)(w*0.72) },
                new int[]{ (int)(h*0.04),(int)(h*0.96),(int)(h*0.96),(int)(h*0.04)+fold,(int)(h*0.04) }, 5);
            // fold triangle
            g.setColor(MainWindow.C_SURFACE3);
            g.fillPolygon(
                new int[]{ (int)(w*0.72),(int)(w*0.90),(int)(w*0.72) },
                new int[]{ (int)(h*0.04),(int)(h*0.04)+fold,(int)(h*0.04)+fold }, 3);
            // lines to suggest text
            g.setColor(MainWindow.C_BG);
            g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < 3; i++) {
                double y = h * (0.42 + i * 0.14);
                g.draw(new Line2D.Double(w*0.22, y, w*0.78, y));
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  internal drawing helper
    // ─────────────────────────────────────────────────────────────────────────

    @FunctionalInterface
    interface Painter { void paint(Graphics2D g); }

    static Icon draw(int w, int h, Painter p) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        p.paint(g);
        g.dispose();
        return new ImageIcon(img);
    }
}
