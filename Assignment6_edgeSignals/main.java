import java.awt.image.BufferedImage;
import java.awt.GridLayout;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class main {

	// Convert RGB pixel to grayscale intensity in [0,255]
	private static int toGray(int rgb) {
		int r = (rgb >> 16) & 0xFF;
		int g = (rgb >> 8) & 0xFF;
		int b = rgb & 0xFF;
		return (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);
	}

	private static int clamp(int v, int lo, int hi) {
		return Math.max(lo, Math.min(hi, v));
	}

	private static int grayToRgb(int g) {
		int v = clamp(g, 0, 255);
		return (v << 16) | (v << 8) | v;
	}

	private static JPanel buildPanel(String title, BufferedImage img) {
		JPanel p = new JPanel(new GridLayout(2, 1));
		JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
		JLabel imageLabel = new JLabel(new ImageIcon(img));
		imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		p.add(titleLabel);
		p.add(imageLabel);
		return p;
	}

	private static void showImages(BufferedImage original, BufferedImage ixImage, BufferedImage iyImage, BufferedImage magImage) {
		JFrame frame = new JFrame("Gradient Edge Signals");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new GridLayout(2, 2, 8, 8));

		frame.add(buildPanel("Original", original));
		frame.add(buildPanel("Ix (dI/dx)", ixImage));
		frame.add(buildPanel("Iy (dI/dy)", iyImage));
		frame.add(buildPanel("|∇I| Edge Signal", magImage));

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		String inputPath = (args.length >= 1) ? args[0] : "fox.jpg";
		double threshold = (args.length >= 2) ? Double.parseDouble(args[1]) : -1.0;

		try {
			BufferedImage input = ImageIO.read(new File(inputPath));
			if (input == null) {
				System.out.println("Cannot read image: " + inputPath);
				return;
			}

			int w = input.getWidth();
			int h = input.getHeight();

			int[][] gray = new int[h][w];
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					gray[y][x] = toGray(input.getRGB(x, y));
				}
			}

			double[][] ixArr = new double[h][w];
			double[][] iyArr = new double[h][w];
			double[][] magArr = new double[h][w];
			double maxAbsIx = 1.0;
			double maxAbsIy = 1.0;
			double maxMag = 1.0;

			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					// Central differences from your formula:
					// Ix(x,y) ≈ (f(x+1,y) - f(x-1,y)) / 2
					// Iy(x,y) ≈ (f(x,y+1) - f(x,y-1)) / 2
					int xm1 = clamp(x - 1, 0, w - 1);
					int xp1 = clamp(x + 1, 0, w - 1);
					int ym1 = clamp(y - 1, 0, h - 1);
					int yp1 = clamp(y + 1, 0, h - 1);

					double ix = (gray[y][xp1] - gray[y][xm1]) / 2.0;
					double iy = (gray[yp1][x] - gray[ym1][x]) / 2.0;

					// Edge signal (gradient magnitude)
					double mag = Math.sqrt(ix * ix + iy * iy);
					ixArr[y][x] = ix;
					iyArr[y][x] = iy;
					magArr[y][x] = mag;

					maxAbsIx = Math.max(maxAbsIx, Math.abs(ix));
					maxAbsIy = Math.max(maxAbsIy, Math.abs(iy));
					maxMag = Math.max(maxMag, mag);
				}
			}

			BufferedImage ixImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
			BufferedImage iyImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
			BufferedImage magImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);

			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					// Signed derivative visualization: mid-gray=0, bright/dark=+/-
					int ixVis = clamp((int) Math.round(128.0 + (ixArr[y][x] / maxAbsIx) * 127.0), 0, 255);
					int iyVis = clamp((int) Math.round(128.0 + (iyArr[y][x] / maxAbsIy) * 127.0), 0, 255);

					int magVis;
					if (threshold >= 0) {
						magVis = (magArr[y][x] >= threshold) ? 255 : 0;
					} else {
						// Log normalization keeps weak edges visible and avoids "all black" output
						double numer = Math.log1p(magArr[y][x]);
						double denom = Math.log1p(maxMag);
						magVis = (denom > 0) ? clamp((int) Math.round((numer / denom) * 255.0), 0, 255) : 0;
					}

					ixImage.setRGB(x, y, grayToRgb(ixVis));
					iyImage.setRGB(x, y, grayToRgb(iyVis));
					magImage.setRGB(x, y, grayToRgb(magVis));
				}
			}

			ImageIO.write(ixImage, "png", new File("edge_ix.png"));
			ImageIO.write(iyImage, "png", new File("edge_iy.png"));
			ImageIO.write(magImage, "png", new File("edge_magnitude.png"));
			System.out.println("Saved: edge_ix.png, edge_iy.png, edge_magnitude.png");
			if (threshold >= 0) {
				System.out.println("Threshold used: " + threshold);
			}

			showImages(input, ixImage, iyImage, magImage);

		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
