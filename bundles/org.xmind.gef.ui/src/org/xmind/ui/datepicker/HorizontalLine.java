package org.xmind.ui.datepicker;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;

public class HorizontalLine extends Figure {

	private int margin = 0;

	public int getMargin() {
		return margin;
	}

	public void setMargin(int margin) {
		this.margin = margin;
		repaint();
	}

	@Override
	protected void paintFigure(Graphics graphics) {
		super.paintFigure(graphics);
		Rectangle b = getBounds();
		int left = b.x + margin;
		int right = b.x + b.width - margin - 1;
		if (left > right)
			return;
		graphics.drawLine(left, b.y + b.height / 2, right, b.y + b.height / 2);
	}

}
