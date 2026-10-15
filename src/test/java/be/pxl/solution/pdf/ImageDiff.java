package be.pxl.solution.pdf;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

/** Minimal visual comparison: share of pixels that differ, with regions that are allowed to change masked out. */
public final class ImageDiff {

	private ImageDiff() {
	}

	/** Paints the regions black (in a copy), like the "mask" option of Playwright's toHaveScreenshot. */
	public static BufferedImage mask(BufferedImage image, List<Rectangle> regions) {
		BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = copy.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.setColor(Color.BLACK);
		regions.forEach(r -> g.fillRect(r.x, r.y, r.width, r.height));
		g.dispose();
		return copy;
	}

	/** @return the share (0..1) of pixels where any colour channel differs by more than channelTolerance */
	public static double differingPixelRatio(BufferedImage a, BufferedImage b, int channelTolerance) {
		if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
			return 1.0;
		}
		long different = 0;
		for (int y = 0; y < a.getHeight(); y++) {
			for (int x = 0; x < a.getWidth(); x++) {
				if (differs(a.getRGB(x, y), b.getRGB(x, y), channelTolerance)) {
					different++;
				}
			}
		}
		return (double) different / ((long) a.getWidth() * a.getHeight());
	}

	/** A copy of {@code actual} with the pixels that differ from {@code expected} painted red (same size required). */
	public static BufferedImage highlight(BufferedImage actual, BufferedImage expected, int channelTolerance) {
		BufferedImage out = new BufferedImage(actual.getWidth(), actual.getHeight(), BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < actual.getHeight(); y++) {
			for (int x = 0; x < actual.getWidth(); x++) {
				boolean different = x >= expected.getWidth() || y >= expected.getHeight()
						|| differs(actual.getRGB(x, y), expected.getRGB(x, y), channelTolerance);
				out.setRGB(x, y, different ? Color.RED.getRGB() : actual.getRGB(x, y));
			}
		}
		return out;
	}

	private static boolean differs(int p, int q, int tolerance) {
		return Math.abs(((p >> 16) & 255) - ((q >> 16) & 255)) > tolerance
				|| Math.abs(((p >> 8) & 255) - ((q >> 8) & 255)) > tolerance
				|| Math.abs((p & 255) - (q & 255)) > tolerance;
	}
}
