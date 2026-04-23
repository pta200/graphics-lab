import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;
import java.lang.Math;
import java.awt.image.BufferedImage;

public class RayTrace3DFrame extends JPanel
        implements MouseListener, MouseMotionListener {

    JLabel statusLabel;
    int width, height;
    double countx, county;
    Image backbuffer;
    Graphics backg;

    double convertx, converty;
    double scale;
    double[] camera = { 0.0, 0.0, 10.0 };
    double[] film = { 0.0, 0.0, 0.0 };
    double[] circle1 = { -1.0, 0.0, 0.0 };
    double[] circle2 = { 0.0, 1.0, 0.0 };
    double[] circle3 = { 1.0, 0.0, 0.0 };
    double[] circle4 = { 0.0, -1.0, 0.0 };
    double[] normal = { 1.0, 1.0, 1.0 };
    double r1 = 1.0;
    double r2 = 1.25;
    double r3 = 1.0;
    double r4 = 1.25;
    double theta = 0.0;
    double phi = 0.0;
    int mx, my;

    public RayTrace3DFrame() {
        width = 400;
        height = 400;
        convertx = (double) (width / 2);
        converty = (double) (height / 2);
        scale = (double) width * 2;

        backbuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        backg = backbuffer.getGraphics();
        addMouseListener(this);
        addMouseMotionListener(this);

        statusLabel = new JLabel(" Ready");
        statusLabel.setBorder(BorderFactory.createEtchedBorder());
    }

    public void destroy() {
    }

    public Matrix3D GetInverse(Matrix3D m) {
        Matrix3D temp = new Matrix3D();
        int i, j;
        double[] xyz = { m.get(1, 0), m.get(1, 1), m.get(1, 2) };
        double t = m.get(1, 3);
        for (j = 0; j < 4; j++) {
            for (i = 0; i < 4; i++) {
                temp.set(i, j, m.get(j, i));
            }
        }
        temp.set(0, 3, -xyz[0] * t);
        temp.set(1, 3, -xyz[1] * t);
        temp.set(2, 3, -xyz[2] * t);
        temp.set(3, 3, 1.0);
        return temp;
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
        mx = e.getX();
        my = e.getY();
        // get new angle
        // theta = Math.PI * mx / 180.0;
        // phi = Math.PI * my / 180.0;
        theta = mx - convertx;
        phi = -my + converty;
        this.showStatus("Mouse at (" + theta + "," + phi + ")");

        // set matrix
        Matrix3D mat = new Matrix3D();
        mat.identity();
        mat.set(3, 2, 10.0);
        mat.rotateX(theta);
        mat.rotateY(phi);
        Matrix3D mat2 = GetInverse(mat);

        // transform centers of each circle
        Vector3D vec = new Vector3D();
        vec.set(circle1[0], circle1[1], circle1[2], 1.0);
        vec.transform(mat2);
        circle1[0] = vec.get(0);
        circle1[1] = vec.get(1);
        circle1[2] = vec.get(2);

        vec.set(circle2[0], circle2[1], circle2[2], 1.0);
        vec.transform(mat2);
        circle2[0] = vec.get(0);
        circle2[1] = vec.get(1);
        circle2[2] = vec.get(2);

        vec.set(circle3[0], circle3[1], circle3[2], 1.0);
        vec.transform(mat2);
        circle3[0] = vec.get(0);
        circle3[1] = vec.get(1);
        circle3[2] = vec.get(2);

        vec.set(circle4[0], circle4[1], circle4[2], 1.0);
        vec.transform(mat2);
        circle4[0] = vec.get(0);
        circle4[1] = vec.get(1);
        circle4[2] = vec.get(2);

        repaint();
        e.consume();
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
        double[] result = new double[3];
        double xt = camera[0] + convertx;
        double yt = camera[1] + converty;
        double zt = camera[2] + scale;
        double d1 = x - xt;
        double d2 = y - yt;
        double d3 = z - zt;
        result[0] = d1 * d1 + d2 * d2 + d3 * d3;
        result[1] = 2 * ((d1 * (xt - (p[0] + convertx))) +
                (d2 * (yt - (p[1] + converty))) +
                (d3 * (zt - (p[2] + scale))));
        result[2] = Math.pow(xt - (p[0] + convertx), 2.0) +
                Math.pow(yt - (p[1] + converty), 2.0) +
                Math.pow(zt - (p[2] + scale), 2.0) -
                r * r;
        return SolveT(result[0], result[1], result[2]);
    }

    public int FindSmallest(double[] w) {
        double win = 100000.0;
        double solvet;
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

    public double ShadeSphere(double[] p, double t, double x, double y, double z) {
        double xt = camera[0] + convertx;
        double yt = camera[1] + converty;
        double zt = camera[2] + scale;
        double source1 = (convertx + 1) - (p[0] + convertx);
        double source2 = (converty + 1) - (p[1] + converty);
        double source3 = (scale + 1) - (p[2] + scale);
        double d1 = x - xt;
        double d2 = y - yt;
        double d3 = z - zt;
        double[] spoint = { (xt + t * d1) - (p[0] + convertx), (yt + t * d2) - (p[1] + converty),
                (zt + t * d3) - (p[2] + scale) };
        double surface = Math.sqrt(spoint[0] * spoint[0] + spoint[1] * spoint[1] +
                spoint[2] * spoint[2]);
        double[] snormal = { spoint[0] / surface, spoint[1] / surface, spoint[2] / surface };
        double llength = Math.sqrt(source1 * source1 + source2 * source2 + source3 * source3);
        double[] lnormal = { source1 / llength, source2 / llength, source3 / llength };
        double dot = snormal[0] * lnormal[0] + snormal[1] * lnormal[1] + snormal[2] * lnormal[2];

        if (dot < 0.0) {
            dot = 0.0;
        }
        return 0.2 + (0.8 * dot);
    }

    public void update(Graphics g) {
        double[] win = { 0.0, 0.0, 0.0, 0.0 };
        Color sred = Color.red;
        Color sblue = Color.blue;
        Color sgreen = Color.green;
        double shade = 0.0;
        double red, green, blue, white = 0.0;
        int winner = -1;

        backg.setColor(Color.white);
        backg.fillRect(0, 0, width, height);
        g.drawImage(backbuffer, 0, 0, this);

        for (int i = 0; i < 400; i++) {
            for (int j = 0; j < 400; j++) {
                win[0] = GetABC(circle1, r1, (double) i, (double) j, 0.0);
                win[1] = GetABC(circle2, r2, (double) i, (double) j, 0.0);
                win[2] = GetABC(circle3, r3, (double) i, (double) j, 0.0);
                win[3] = GetABC(circle4, r4, (double) i, (double) j, 0.0);

                winner = FindSmallest(win);
                switch (winner) {
                    case 0: {
                        shade = ShadeSphere(circle1, win[0], (double) i, (double) j, 0.0);
                        red = (double) sred.getRed() * shade;
                        backg.setColor(new Color((int) red, 0, 0));
                        break;
                    }
                    case 1: {
                        shade = ShadeSphere(circle2, win[1], (double) i, (double) j, 0.0);
                        green = (double) sgreen.getGreen() * shade;
                        backg.setColor(new Color(0, (int) green, 0));
                        break;
                    }
                    case 2: {
                        shade = ShadeSphere(circle3, win[2], (double) i, (double) j, 0.0);
                        blue = (double) sblue.getBlue() * shade;
                        backg.setColor(new Color(0, 0, (int) blue));
                        break;
                    }
                    case 3: {
                        shade = ShadeSphere(circle4, win[3], (double) i, (double) j, 0.0);
                        red = (double) sred.getRed() * shade;
                        blue = (double) sblue.getBlue() * shade;
                        green = (double) sgreen.getGreen() * shade;
                        backg.setColor(new Color((int) red, (int) green, (int) blue));
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

    public void showStatus(String message) {
        this.statusLabel.setText(" " + message);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Ray Trace");
        RayTrace3DFrame panel = new RayTrace3DFrame();
        frame.add(panel);
        frame.setSize(panel.width, panel.height); // Match your original applet size
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.add(panel.statusLabel, BorderLayout.SOUTH);
    }
} // RayTrace
