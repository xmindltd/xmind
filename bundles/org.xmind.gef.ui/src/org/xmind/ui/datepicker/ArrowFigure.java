package org.xmind.ui.datepicker;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;

public class ArrowFigure extends BaseFigure {

	public static final int LEFT = PositionConstants.LEFT;

	public static final int RIGHT = PositionConstants.RIGHT;

	public static final int UP = PositionConstants.TOP;

	public static final int DOWN = PositionConstants.BOTTOM;

	private static final float len = 4.0f;

	private class Arrow extends Layer {

		@Override
		protected void paintFigure(Graphics graphics) {
			super.paintFigure(graphics);

			Rectangle b = getBounds();
			float cx = b.x + ((float) b.width) / 2;
			float cy = b.y + ((float) b.height) / 2;
			int[] points;
			if (orientation == LEFT) {
				points = new int[] { (int) (cx - len), (int) cy, //
						(int) (cx + len), (int) (cy - len), //
						(int) (cx + len), (int) (cy + len) };
			} else if (orientation == RIGHT) {
				points = new int[] { (int) (cx + len), (int) cy, //
						(int) (cx - len), (int) (cy - len), //
						(int) (cx - len), (int) (cy + len) };
			} else if (orientation == UP) {
				points = new int[] { (int) cx, (int) (cy - len), //
						(int) (cx + len), (int) (cy + len), //
						(int) (cx - len), (int) (cy + len) };
			} else {
				points = new int[] { (int) cx, (int) (cy + len), //
						(int) (cx + len), (int) (cy - len), //
						(int) (cx - len), (int) (cy - len) };
			}
			graphics.fillPolygon(points);
			graphics.drawPolygon(points);
		}

	}

	private int orientation = 0;

	@Override
	protected void addFeedbackLayers() {
		addPressFeedbackLayer();
	}

	@Override
	protected void addContentLayer() {
		setContentLayer(new Arrow());
	}

	public int getOrientation() {
		return orientation;
	}

	public void setOrientation(int direction) {
		this.orientation = direction;
		repaint();
	}

}
