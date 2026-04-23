import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Zbuffer extends JPanel {
	int width, height;
	Image backbuffer;
	Graphics backg;
	double convertx, converty;
	double[][] zbuffer;

	double[][] triangle1 = { { 0.0, 1.0, -10.0 },
			{ 1.0, 0.0, -9.0 },
			{ -1.0, -1.0, -8.0 } };

	double[][] triangle2 = { { 0.5, 1.0, -7.0 },
			{ 0.0, 0.0, -7.0 },
			{ 0.5, -1.0, -7.0 } };

	double[] camera = { 0.0, 0.0, 0.0 };
	double focal = -3.0;

	public Zbuffer() {
		width = 400;
		height = 400;
		convertx = (double) (width / 2);
		converty = (double) (height / 2);
		zbuffer = new double[width][height];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				zbuffer[i][j] = 0.0;
			}
		}
		backbuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		backg = backbuffer.getGraphics();
	}

	public void destroy() {
	}

	public void DrawTriangle(double[][] triangle1, double convertfoc) {

		int l, r = 0;
		double dx1 = convertfoc * triangle1[0][0] / triangle1[0][2] + convertx;
		double dx2 = convertfoc * triangle1[1][0] / triangle1[1][2] + convertx;
		double dx3 = convertfoc * triangle1[2][0] / triangle1[2][2] + convertx;
		double dy1 = -(convertfoc * triangle1[0][1] / triangle1[0][2]) + converty;
		double dy2 = -(convertfoc * triangle1[1][1] / triangle1[1][2]) + converty;
		double dy3 = -(convertfoc * triangle1[2][1] / triangle1[2][2]) + converty;
		double dz1 = 1 / triangle1[0][2] - convertx;
		double dz2 = 1 / triangle1[1][2] - convertx;
		double dz3 = 1 / triangle1[2][2] - convertx;

		double m1 = (dx2 - dx1) / (dy2 - dy1);
		double m2 = (dx3 - dx1) / (dy3 - dy1);
		double m3 = (dx3 - dx2) / (dy3 - dy2);

		double z1 = (dz2 - dz1) / (dz2 - dx1);

		double left = dx1;
		double right = dx1;
		double perspz = dz1;

		for (int i = (int) dy1; i <= (int) dy3; i++) {
			if (left > right) {
				l = (int) right;
				r = (int) left;
			} else {
				l = (int) left;
				r = (int) right;
			}

			for (int b = l; b <= r; b++) {
				backg.drawLine(b, i, b + 1, i + 1);
				if (zbuffer[i][b] > perspz) {
					zbuffer[i][b] = perspz;
				}
			}
			if (i < dy2) {
				left += m1;
				right += m2;
				perspz += z1;
			} else {
				left += m3;
				right += m2;
				perspz += z1;
			}
		}
	}

	public void update(Graphics g) {
		backg.setColor(Color.black);
		backg.fillRect(0, 0, width, height);
		g.drawImage(backbuffer, 0, 0, this);
		double foct = focal - 3 * height;

		backg.setColor(Color.green);
		DrawTriangle(triangle1, foct);

		backg.setColor(Color.yellow);
		DrawTriangle(triangle2, foct);

		String colorval = "";
		Double dd = null;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (zbuffer[i][j] >= 0.0) {
					backg.setColor(Color.white);
				} else {
					colorval = Double.valueOf(-zbuffer[i][j]).toString();
					dd = Double.valueOf(colorval.substring(colorval.length() - 2,
							colorval.length()));
					backg.setColor(new Color(dd.intValue(), dd.intValue(),
							dd.intValue()));
				}
				backg.drawLine(j, i, j + 1, i + 1);
			}
		}
		g.drawImage(backbuffer, 0, 0, this);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		update(g);
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("ZBuffer");
		Zbuffer panel = new Zbuffer();
		frame.add(panel);
		frame.setSize(panel.width, panel.height); // Match your original applet size
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}
}
