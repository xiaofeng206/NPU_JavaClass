import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

class App {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			CameraDemoFrame frame = new CameraDemoFrame();
			frame.setVisible(true);
		});
	}

	static class CameraDemoFrame extends JFrame {
		private final ImageCanvas canvas;
		private final JTextField focalField;
		private final JTextField depthField;
		private final JLabel statusLabel;
		private final JLabel resultLabel;
		private final DecimalFormat df = new DecimalFormat("0.000");

		CameraDemoFrame() {
			setTitle("Pinhole Camera Model - Road Demo");
			setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			setSize(1200, 800);
			setLocationRelativeTo(null);

			canvas = new ImageCanvas(this);

			JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JButton loadBtn = new JButton("載入 road.jpg");
			JButton distModeBtn = new JButton("量測兩點距離");
			JButton vpModeBtn = new JButton("設定消失點(4點)");
			JButton clearBtn = new JButton("清除標記");

			focalField = new JTextField("1000", 6); // px
			depthField = new JTextField("20", 6);   // m

			top.add(loadBtn);
			top.add(distModeBtn);
			top.add(vpModeBtn);
			top.add(clearBtn);
			top.add(new JLabel("焦距 f(px):"));
			top.add(focalField);
			top.add(new JLabel("場景深度 Z(m):"));
			top.add(depthField);

			statusLabel = new JLabel("模式: 請先載入圖片");
			resultLabel = new JLabel("結果: -");

			JPanel bottom = new JPanel(new GridLayout(2, 1));
			bottom.add(statusLabel);
			bottom.add(resultLabel);

			add(top, BorderLayout.NORTH);
			add(new JScrollPane(canvas), BorderLayout.CENTER);
			add(bottom, BorderLayout.SOUTH);

			loadBtn.addActionListener(e -> loadDefaultImage());
			distModeBtn.addActionListener(e -> {
				canvas.setMode(ImageCanvas.Mode.DISTANCE);
				setStatus("模式: 量測距離 (請點兩個點)");
			});
			vpModeBtn.addActionListener(e -> {
				canvas.setMode(ImageCanvas.Mode.VANISHING_POINT);
				setStatus("模式: 消失點 (依序點 4 個點：線1兩點 + 線2兩點)");
			});
			clearBtn.addActionListener(e -> {
				canvas.clearAnnotations();
				setResult("結果: -");
			});

			loadDefaultImage();
		}

		void loadDefaultImage() {
			File road = new File("road.jpg");
			if (!road.exists()) {
				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("找不到 road.jpg，請選擇圖片");
				int ret = chooser.showOpenDialog(this);
				if (ret == JFileChooser.APPROVE_OPTION) {
					road = chooser.getSelectedFile();
				} else {
					setStatus("模式: 尚未載入圖片");
					return;
				}
			}

			try {
				BufferedImage img = ImageIO.read(road);
				if (img == null) {
					JOptionPane.showMessageDialog(this, "無法讀取圖片: " + road.getAbsolutePath());
					return;
				}
				canvas.setImage(img);
				setStatus("已載入: " + road.getName() + " | 請選擇模式");
				setResult("結果: -");
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(this, "讀取圖片失敗: " + ex.getMessage());
			}
		}

		double getFocalPx() {
			return parsePositive(focalField.getText(), 1000.0);
		}

		double getDepthMeters() {
			return parsePositive(depthField.getText(), 20.0);
		}

		private double parsePositive(String s, double fallback) {
			try {
				double v = Double.parseDouble(s.trim());
				return v > 0 ? v : fallback;
			} catch (Exception ex) {
				return fallback;
			}
		}

		void onDistanceMeasured(Point2D p1, Point2D p2, double pixelDist) {
			double f = getFocalPx();
			double z = getDepthMeters();
			double estimatedMeters = (pixelDist * z) / f;

			setResult("結果: 像素距離 = " + df.format(pixelDist)
					+ " px, 估計實際距離 = " + df.format(estimatedMeters)
					+ " m (Pinhole: d = d_px * Z / f)");
		}

		void onVanishingPointComputed(Point2D vp, Point2D c) {
			double f = getFocalPx();
			double dx = vp.getX() - c.getX();
			double dy = vp.getY() - c.getY();
			double norm = Math.sqrt(dx * dx + dy * dy + f * f);
			double vx = dx / norm;
			double vy = dy / norm;
			double vz = f / norm;

			setResult("結果: 消失點 VP = (" + df.format(vp.getX()) + ", " + df.format(vp.getY())
					+ "), 方向向量 ≈ (" + df.format(vx) + ", " + df.format(vy) + ", " + df.format(vz) + ")");
		}

		void onVanishingPointFailed() {
			setResult("結果: 兩條線近乎平行，無法穩定計算消失點");
		}

		void setStatus(String text) {
			statusLabel.setText(text);
		}

		void setResult(String text) {
			resultLabel.setText(text);
		}
	}

	static class ImageCanvas extends JPanel {
		enum Mode {DISTANCE, VANISHING_POINT}

		private BufferedImage image;
		private Mode mode = Mode.DISTANCE;
		private final CameraDemoFrame owner;

		private final List<Point2D> distancePoints = new ArrayList<>();
		private final List<Point2D> vpPoints = new ArrayList<>();
		private Point2D vanishingPoint;

		ImageCanvas(CameraDemoFrame owner) {
			this.owner = owner;
			setBackground(Color.DARK_GRAY);

			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (image == null) {
						return;
					}
					Point2D p = new Point2D.Double(e.getX(), e.getY());
					if (mode == Mode.DISTANCE) {
						handleDistanceClick(p);
					} else {
						handleVanishingPointClick(p);
					}
					repaint();
				}
			});
		}

		void setImage(BufferedImage img) {
			this.image = img;
			clearAnnotations();
			setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
			revalidate();
			repaint();
		}

		void setMode(Mode mode) {
			this.mode = mode;
			clearAnnotations();
			owner.setResult("結果: -");
			repaint();
		}

		void clearAnnotations() {
			distancePoints.clear();
			vpPoints.clear();
			vanishingPoint = null;
			repaint();
		}

		private void handleDistanceClick(Point2D p) {
			if (distancePoints.size() == 2) {
				distancePoints.clear();
			}
			distancePoints.add(p);

			if (distancePoints.size() == 2) {
				Point2D p1 = distancePoints.get(0);
				Point2D p2 = distancePoints.get(1);
				double d = p1.distance(p2);
				owner.onDistanceMeasured(p1, p2, d);
			} else {
				owner.setStatus("模式: 量測距離 (已點 1 點，請再點 1 點)");
			}
		}

		private void handleVanishingPointClick(Point2D p) {
			if (vpPoints.size() == 4) {
				vpPoints.clear();
				vanishingPoint = null;
			}
			vpPoints.add(p);

			if (vpPoints.size() == 4) {
				Point2D p1 = vpPoints.get(0);
				Point2D p2 = vpPoints.get(1);
				Point2D p3 = vpPoints.get(2);
				Point2D p4 = vpPoints.get(3);

				Point2D vp = computeIntersection(p1, p2, p3, p4);
				if (vp == null) {
					owner.onVanishingPointFailed();
				} else {
					vanishingPoint = vp;
					Point2D center = new Point2D.Double(image.getWidth() / 2.0, image.getHeight() / 2.0);
					owner.onVanishingPointComputed(vp, center);
				}
			} else {
				owner.setStatus("模式: 消失點 (目前已點 " + vpPoints.size() + "/4)");
			}
		}

		private Point2D computeIntersection(Point2D a1, Point2D a2, Point2D b1, Point2D b2) {
			double x1 = a1.getX(), y1 = a1.getY();
			double x2 = a2.getX(), y2 = a2.getY();
			double x3 = b1.getX(), y3 = b1.getY();
			double x4 = b2.getX(), y4 = b2.getY();

			double den = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
			if (Math.abs(den) < 1e-9) {
				return null;
			}

			double px = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / den;
			double py = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / den;
			return new Point2D.Double(px, py);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			if (image != null) {
				g2.drawImage(image, 0, 0, null);
			}

			drawDistanceAnnotations(g2);
			drawVanishingAnnotations(g2);

			g2.dispose();
		}

		private void drawDistanceAnnotations(Graphics2D g2) {
			if (distancePoints.isEmpty()) return;

			g2.setStroke(new BasicStroke(2f));
			g2.setColor(new Color(0, 255, 128));

			for (Point2D p : distancePoints) {
				drawPoint(g2, p, 5);
			}

			if (distancePoints.size() == 2) {
				Point2D a = distancePoints.get(0);
				Point2D b = distancePoints.get(1);
				g2.draw(new Line2D.Double(a, b));
			}
		}

		private void drawVanishingAnnotations(Graphics2D g2) {
			if (vpPoints.isEmpty()) return;

			g2.setStroke(new BasicStroke(2f));
			g2.setColor(new Color(255, 120, 0));

			for (Point2D p : vpPoints) {
				drawPoint(g2, p, 5);
			}

			if (vpPoints.size() >= 2) {
				g2.draw(new Line2D.Double(vpPoints.get(0), vpPoints.get(1)));
			}
			if (vpPoints.size() >= 4) {
				g2.draw(new Line2D.Double(vpPoints.get(2), vpPoints.get(3)));
			}

			if (vanishingPoint != null) {
				g2.setColor(Color.RED);
				drawCross(g2, vanishingPoint, 8);
				g2.drawString("VP", (float) vanishingPoint.getX() + 10, (float) vanishingPoint.getY() - 10);
			}
		}

		private void drawPoint(Graphics2D g2, Point2D p, int r) {
			int x = (int) Math.round(p.getX());
			int y = (int) Math.round(p.getY());
			g2.fillOval(x - r, y - r, 2 * r, 2 * r);
		}

		private void drawCross(Graphics2D g2, Point2D p, int size) {
			int x = (int) Math.round(p.getX());
			int y = (int) Math.round(p.getY());
			g2.drawLine(x - size, y, x + size, y);
			g2.drawLine(x, y - size, x, y + size);
		}
	}
}
