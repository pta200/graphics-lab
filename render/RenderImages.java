import java.awt.*;
import java.lang.Math;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.image.BufferedImage;

abstract class ParametricSurface {
	double[][] zbuffer;
	double convertx, converty;
	double[][][] P;

	public abstract double x(double u, double v);

	public abstract double y(double u, double v);

	public abstract double z(double u, double v);

	public int[] sort(double[] a) {
		int[] t = new int[3];
		t[0] = -1;
		t[1] = -1;
		t[2] = -1;
		if (a[0] < a[1]) {
			if (a[2] < a[1]) {
				t[0] = 1;
				if (a[2] < a[0]) {
					t[1] = 0;
					t[2] = 2;
				} else {
					t[1] = 2;
					t[2] = 0;
				}
			} else {
				t[0] = 2;
				t[1] = 1;
				t[2] = 0;
			}
		} else if (a[0] < a[2]) {
			if (a[1] < a[2]) {
				t[0] = 2;
				t[1] = 0;
				t[2] = 1;
			} else {
				t[0] = 0;
				t[1] = 1;
				t[2] = 2;
			}
		} else {
			t[0] = 0;
			if (a[1] < a[2]) {
				t[1] = 2;
				t[2] = 1;
			} else {
				t[1] = 1;
				t[2] = 2;
			}
		}
		return t;
	}

	public double Max(double a, double b, double c) {
		double max = -100000;
		double[] arr = { a, b, c };
		for (int i = 0; i < 3; i++) {
			if (max < arr[i]) {
				max = arr[i];
			}
		}
		return max;
	}

	public double Min(double a, double b, double c) {
		double min = 1000000;
		double[] arr = { a, b, c };
		for (int i = 0; i < 3; i++) {
			if (min > arr[i]) {
				min = arr[i];
			}
		}
		return min;
	}

	public void zbufferTriangle(double[] t1, double[] t2, double[] t3) {
		double convertfoc = -(convertx * 3);
		double[] d1;
		double[] d2;
		double[] d3;

		// sort all the y values ordering them in decreasing size
		double[] ps1 = { t1[1], t2[1], t3[1] };
		int[] pos = sort(ps1);
		if (pos[0] == 0) {
			d1 = t1;
		} else if (pos[0] == 1) {
			d1 = t2;
		} else {
			d1 = t3;
		}
		if (pos[1] == 0) {
			d2 = t1;
		} else if (pos[1] == 1) {
			d2 = t2;
		} else {
			d2 = t3;
		}
		if (pos[2] == 0) {
			d3 = t1;
		} else if (pos[2] == 1) {
			d3 = t2;
		} else {
			d3 = t3;
		}
		// calculate pixel points
		double dx1 = convertfoc * d1[0] / d1[2] + convertx;
		double dx2 = convertfoc * d2[0] / d2[2] + convertx;
		double dx3 = convertfoc * d3[0] / d3[2] + convertx;
		double dy1 = -(convertfoc * d1[1] / d1[2]) + converty;
		double dy2 = -(convertfoc * d2[1] / d2[2]) + converty;
		double dy3 = -(convertfoc * d3[1] / d3[2]) + converty;
		double dz1 = 1 / d1[2] - convertx;
		double dz2 = 1 / d2[2] - convertx;
		double dz3 = 1 / d3[2] - convertx;

		// get slopes
		double m1 = (dx2 - dx1) / (dy2 - dy1);
		double m2 = (dx3 - dx1) / (dy3 - dy1);
		double m3 = (dx3 - dx2) / (dy3 - dy2);
		double z1 = (dz1 - dz2) / (dx1 - dx2);

		double right = 0.0;
		double left = 0.0;
		double top = 0.0;
		double bot = 0.0;

		// determine which y and x vertices to interpolate over
		if (((int) dy1 == (int) dy2) && ((int) dy3 > (int) dy1)) {
			top = dy1;
			bot = dy3;
			left = Min(dx1, dx2, dx3);
			right = Max(dx1, dx2, dx3);
		} else if (((int) dy1 == (int) dy2) && ((int) dy3 < (int) dy1)) {
			top = dy3;
			bot = dy1;
			left = dx3;
			right = dx3;
		} else if (((int) dy1 == (int) dy3) && ((int) dy2 > (int) dy1)) {
			top = dy1;
			bot = dy2;
			left = Min(dx1, dx2, dx3);
			right = Max(dx1, dx2, dx3);
		} else if (((int) dy1 == (int) dy3) && ((int) dy2 < (int) dy1)) {
			top = dy2;
			bot = dy1;
			left = dx2;
			right = dx2;
		} else if (((int) dy2 == (int) dy3) && ((int) dy1 > (int) dy2)) {
			top = dy2;
			bot = dy1;
			left = Min(dx1, dx2, dx3);
			right = Max(dx1, dx2, dx3);
			right = dx2;
		} else if (((int) dy2 == (int) dy3) && ((int) dy1 < (int) dy2)) {
			top = dy1;
			bot = dy2;
			left = dx1;
			right = dx1;
		} else {
			top = dy1;
			bot = dy2;
			left = Min(dx1, dx2, dx3);
			right = Max(dx1, dx2, dx3);
		}
		double perspz = dz1;

		// fill zbuffer
		for (int i = (int) top; i < (int) bot; i++) {
			for (int b = (int) left; b <= (int) right; b++) {
				if (zbuffer[i][b] > perspz) {
					zbuffer[i][b] = perspz;
				}
			}

			// determine which slopes to add in order to get next x values
			// to interpolate over.
			if (((int) dy1 == (int) dy2) && ((int) dy3 > (int) dy1)) {
				if ((int) dx2 > (int) dx1) {
					left += m2;
					right += m3;
				} else {
					left += m3;
					right = m2;
				}
			} else if (((int) dy1 == (int) dy2) && ((int) dy3 < (int) dy1)) {
				top = dy3;
				bot = dy1;
				if ((int) dx2 > (int) dx1) {
					left += m2;
					right += m3;
				} else {
					left += m3;
					right = m2;
				}
			} else if (((int) dy1 == (int) dy3) && ((int) dy2 > (int) dy1)) {
				if ((int) dx1 > (int) dx3) {
					left += m3;
					right += m1;
				} else {
					left += m1;
					right += m3;
				}
			} else if (((int) dy1 == (int) dy3) && ((int) dy2 < (int) dy1)) {
				if ((int) dx1 > (int) dx3) {
					left += m3;
					right += m1;
				} else {
					left += m1;
					right += m3;
				}
			} else if (((int) dy2 == (int) dy3) && ((int) dy1 > (int) dy2)) {
				if ((int) dx3 > (int) dx2) {
					left += m1;
					;
					right += m2;
				} else {
					left += m2;
					right += m1;
				}
			} else if (((int) dy2 == (int) dy3) && ((int) dy1 < (int) dy2)) {
				if ((int) dx3 > (int) dx2) {
					left += m1;
					;
					right += m2;
				} else {
					left += m2;
					right += m1;
				}
			}
			perspz += z1;
		}

	} // end zbuffer triangle

	public boolean samePoint(double[] a, double[] b) {
		return a[0] == b[0] && a[1] == b[1] && a[2] == b[2];
	}

	public void computeNormal(double[] p1, double[] p2, double[] p3, double[] p4,
			double[] p5) {
		double[] A = { p3[0] - p2[0], p3[1] - p2[1], p3[2] - p2[2] };
		double[] B = { p5[0] - p4[0], p5[1] - p4[1], p5[2] - p4[2] };
		double[] cross = { A[1] * B[2] - B[1] * A[2], A[2] * B[0] - B[2] * A[0],
				A[0] * B[1] - B[0] * A[1] };
		double length = Math.sqrt(cross[0] * cross[0] + cross[1] * cross[1]
				+ cross[2] * cross[0]);
		p1[3] = cross[0] / length;
		p1[4] = cross[1] / length;
		p1[5] = cross[2] / length;
	}

	public void render(int m, int n) {
		convertx = (double) (m / 2);
		converty = (double) (n / 2);

		// allocate an m by n parametric grid for vertex location and normals
		zbuffer = new double[m][n];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				zbuffer[i][j] = 0.0;
				zbuffer[i][j] = 0.0;
			}
		}
		P = new double[m][n][6];

		// compute the location of each vertex

		for (int i = 0; i < m; i++) {
			double u = (double) i / (m - 1);
			for (int j = 0; j < m; j++) {
				double v = (double) j / (n - 1);
				P[i][j][0] = x(u, v);
				P[i][j][1] = y(u, v);
				P[i][j][2] = z(u, v);
			}
		}

		// compute the normals at each point

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {

				// find the next lower neighbor, and check for wrap-around

				int i0 = i > 0 ? i - 1 : samePoint(P[i][j], P[m - 1][j]) ? m - 2 : i;
				int j0 = j > 0 ? j - 1 : samePoint(P[i][j], P[i][n - 1]) ? n - 2 : j;

				// find the next higher neighbor, and check for wrap-around

				int i1 = i < m - 1 ? i + 1 : samePoint(P[i][j], P[0][j]) ? 1 : i;
				int j1 = j < n - 1 ? j + 1 : samePoint(P[i][j], P[i][0]) ? 1 : j;

				// Fill in P[i][j][3..5] based on cross product of vectors
				// connecting neighboring vertices in parametric grid.
				// (remember to normalize the normal vector inside computeNormal).

				computeNormal(P[i][j], P[i0][j], P[i1][j], P[i][j0], P[i][j1]);
			}
		}

		// z-buffer each polygon
		for (int i = 1; i < m; i++) {
			for (int j = 1; j < n; j++) {
				zbufferTriangle(P[i - 1][j - 1], P[i][j - 1], P[i][j]);
				zbufferTriangle(P[i - 1][j - 1], P[i][j], P[i - 1][j]);
			}
		}

	}
} // end abstract class ParametricSurface

class ParametricSphere extends ParametricSurface {
	double r;
	double cx;
	double cy;
	double cz;

	public ParametricSphere(double radius, double x, double y, double z) {
		r = radius;
		cx = x;
		cy = y;
		cz = z;
	}

	public double theta(double u) {
		return 2 * Math.PI * u;
	}

	public double phi(double v) {
		return Math.PI * v - Math.PI / 2;
	}

	public double x(double u, double v) {
		return r * Math.cos(theta(u)) * Math.cos(phi(v)) + cx;
	}

	public double y(double u, double v) {
		return r * Math.sin(phi(v)) + cy;
	}

	public double z(double u, double v) {
		return -(r * Math.sin(theta(u)) * Math.cos(phi(v)) + cz);
	}
} // end ParametricSphere

public class RenderImages extends JPanel {
	int width, height;
	double conx, cony;
	Image backbuffer;
	Graphics backg;
	ParametricSphere ps;

	public RenderImages() {
		width = 400;
		height = 400;
		conx = (double) width / 2;
		cony = (double) height / 2;
		ps = new ParametricSphere(1.0, 0.0, 0.0, -8.0);

		backbuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		backg = backbuffer.getGraphics();
	}

	public void update(Graphics g) {
		backg.setColor(Color.black);
		backg.fillRect(0, 0, width, height);
		g.drawImage(backbuffer, 0, 0, this);

		ps.render(width, height);
		Color sblue = Color.blue;
		double blue = 0.0;

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (ps.zbuffer[i][j] >= 0.0) {
					backg.setColor(Color.black);
				} else {
					// do shading
					double source1 = (conx + (1.0)) - (ps.cx + conx);
					double source2 = (cony + (1.0)) - (ps.cy + cony);
					double source3 = (conx + (1.0)) - (ps.cz + conx);
					double llength = Math.sqrt(source1 * source1 + source2 * source2
							+ source3 * source3);
					double[] lnormal = { source1 / llength, source2 / llength, source3 / llength };
					double dot = lnormal[0] * ps.P[i][j][3] + lnormal[1] * ps.P[i][j][4]
							+ lnormal[2] * ps.P[i][j][5];

					if (dot < 0.0) {
						dot = 0.0;
					}
					// Clamp blue value as color takes int in 0–255 range
					blue = (double) sblue.getBlue() * (0.2 + (0.8 * dot));
					int b = Math.max(0, Math.min(255, (int) blue));
					backg.setColor(new Color(0, 0, b));

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
		JFrame frame = new JFrame("Render Sphere");
        RenderImages panel = new RenderImages();
        frame.add(panel);
        frame.setSize(panel.width, panel.height); // Match your original applet size
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
	}
}
