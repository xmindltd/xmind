package org.xmind.ui.datepicker;

import java.util.List;

import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

public class DatePanelLayout extends AbstractLayout {

	@Override
	protected Dimension calculatePreferredSize(IFigure container, int wHint,
			int hHint) {
		if (wHint >= 0 && hHint >= 0) {
			return new Dimension(wHint, hHint);
		}

		List children = container.getChildren();
		int cols = 7;
		int rows = (children.size() + 6) / cols;
		int[] widths = wHint >= 0 ? null : new int[cols];
		int[] heights = hHint >= 0 ? null : new int[rows];
		int m = 0, n = 0;
		int wh = wHint >= 0 ? wHint / cols : -1;
		int hh = hHint >= 0 ? hHint / rows : -1;
		for (int i = 0; i < children.size(); i++) {
			IFigure child = (IFigure) children.get(i);
			Dimension childSize = child.getPreferredSize(wh, hh);
			if (wHint < 0)
				widths[m] = Math.max(widths[m], childSize.width);
			if (hHint < 0)
				heights[n] = Math.max(heights[n], childSize.height);
			m++;
			if (m >= cols) {
				n++;
				m = 0;
			}
		}

		int width;
		if (wHint >= 0) {
			width = wHint;
		} else {
			width = 0;
			for (int i = 0; i < widths.length; i++) {
				width += widths[i];
			}
		}
		int height;
		if (hHint >= 0) {
			height = hHint;
		} else {
			height = 0;
			for (int i = 0; i < heights.length; i++) {
				height += heights[i];
			}
		}

		return new Dimension(width, height);
	}

	public void layout(IFigure container) {
		Rectangle box = container.getClientArea();
		List children = container.getChildren();
		int cols = 7;
		int rows = (children.size() + 6) / cols;
		float fx = box.x, fy = box.y;
		float fw = ((float) box.width + 0.99f) / cols;
		float fh = ((float) box.height + 0.99f) / rows;
		int x = box.x, y = box.y;
		int w = ((int) (fx + fw)) - x;
		int h = ((int) (fy + fh)) - y;
		int m = 0, n = 0;
		int[] lefts = new int[7];
		int[] widths = new int[7];
		for (int i = 0; i < children.size(); i++) {
			IFigure child = (IFigure) children.get(i);
			Rectangle b;
			if (n == 0) {
				b = new Rectangle(x, y, w, h);
				lefts[m] = x;
				widths[m] = w;
				x += w;
				fx += fw;
				w = ((int) (fx + fw)) - x;
			} else {
				x = lefts[m];
				w = widths[m];
				b = new Rectangle(x, y, w, h);
			}
			
			child.setBounds(b);
			
			m++;
			if (m >= cols) {
				y += h;
				fy += fh;
				h = ((int) (fy + fh)) - y;
				n++;
				m = 0;
			}
		}
	}

}
