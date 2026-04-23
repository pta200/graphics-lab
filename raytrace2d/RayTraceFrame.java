import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.Math;

public class RayTraceFrame extends JPanel {

    int width, height;
    double countx, county;
    Thread t = null;
    boolean threadSuspended;
    Image backbuffer;
    Graphics backg;

    double convertx, converty;
    double scale;
    // camera eye point
    double[] camera = { 0.0, 0.0, 0.0 };
    // "film" that is the a square parallel to the xy plane
    // 1 unit wide and 1 unit high, centered at aim point [0,0,-3].
    double[] film = { 0.0, 0.0, -3.0 };
    /*
     * collection of spheres as your model:
     * centerx centery centerz radius color
     * -1 0 -10 1 red
     * 0 1 -10 1.25 green
     * 1 0 -10 1 blue
     * 0 -1 -10 1.25 white
     */
    double[] circle1 = { -1.0, 0.0, -10.0 };
    double[] circle2 = { 0.0, 1.0, -10.0 };
    double[] circle3 = { 1.0, 0.0, -10.0 };
    double[] circle4 = { 0.0, -1.0, -10.0 };
    double r1 = 1.0;
    double r2 = 1.25;
    double r3 = 1.0;
    double r4 = 1.25;

    // Executed when the applet is first created.
    public RayTraceFrame() {
        width = 400;
        height = 400;
        convertx = (double) (width / 2);
        converty = (double) (height / 2);
        scale = (double) width;
        backbuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        backg = backbuffer.getGraphics();
    }

    public void destroy() {
    }

    public double SolveT(double a, double b, double c) {
        double x1, x2;
        x1 = (double) (-b - Math.sqrt((b * b) - 4 * a * c)) / (2 * a);
        x2 = (double) (-b + Math.sqrt((b * b) - 4 * a * c)) / (2 * a);

        if (x1 >= 0.0) {
            return x1;
        } else if (x2 >= 0.0) {
            return x2;
        } else {
            return x1;
        }
    }

    public double GetABC(double[] p, double r, double x, double y, double z) {
        double xt = camera[0] + convertx;
        double yt = camera[1] + converty;
        double zt = camera[2] + scale;
        double d1 = x - xt;
        double d2 = y - yt;
        double d3 = z - zt;
        double a = d1 * d1 + d2 * d2 + d3 * d3;
        double b = 2 * ((d1 * (xt - (p[0] + convertx))) +
                (d2 * (yt - (p[1] + converty))) +
                (d3 * (zt - (p[2] + scale))));
        double c = Math.pow(xt - (p[0] + convertx), 2.0) +
                Math.pow(yt - (p[1] + converty), 2.0) +
                Math.pow(zt - (p[2] + scale), 2.0) -
                r * r;
        return SolveT(a, b, c);
    }

    public int FindSmallest(double[] w) {
        double win = 100000.0;
        int i = -1;
        for (int j = 0; j < 4; j++) {
            if ((w[j] < win)) {
                win = w[j];
                i = j;
            }
        }
        if ((win >= 0.0) && (win <= 1.0)) {
            return i;
        } else {
            return -1;
        }
    }

    public void update(Graphics g) {
        double[] win = { 0.0, 0.0, 0.0, 0.0 };
        int winner = -1;

        for (int i = 0; i < 400; i++) {
            for (int j = 0; j < 400; j++) {
                win[0] = GetABC(circle1, r1, (double) i, (double) j, -3.0);
                win[1] = GetABC(circle2, r2, (double) i, (double) j, -3.0);
                win[2] = GetABC(circle3, r3, (double) i, (double) j, -3.0);
                win[3] = GetABC(circle4, r4, (double) i, (double) j, -3.0);

                winner = FindSmallest(win);
                switch (winner) {
                    case 0: {
                        backg.setColor(Color.red);
                        break;
                    }
                    case 1: {
                        backg.setColor(Color.green);
                        break;
                    }
                    case 2: {
                        backg.setColor(Color.blue);
                        break;
                    }
                    case 3: {
                        backg.setColor(Color.white);
                        break;
                    }
                    case -1: {
                        backg.setColor(Color.black);
                        break;
                    }
                } // end case

                backg.drawLine(i, j, i + 1, j + 1);
            } // end j
        } // end i
        g.drawImage(backbuffer, 0, 0, this);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        update(g);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Ray Trace 2D");
        RayTraceFrame panel = new RayTraceFrame();
        frame.add(panel);
        frame.setSize(panel.width, panel.height); // Match your original applet size
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
