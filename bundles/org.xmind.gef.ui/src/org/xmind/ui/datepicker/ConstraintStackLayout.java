package org.xmind.ui.datepicker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.geometry.Rectangle;

public class ConstraintStackLayout extends StackLayout {

	private Map<IFigure, Rectangle> constraints = new HashMap<IFigure, Rectangle>();

	public void layout(IFigure container) {
		Rectangle r = container.getClientArea();
		List children = container.getChildren();
		IFigure child;
		Rectangle constraint;
		for (int i = 0; i < children.size(); i++) {
			child = (IFigure) children.get(i);
			constraint = constraints.get(child);
			if (constraint == null) {
				child.setBounds(r);
			} else {
				child.setBounds(constraint.getTranslated(r.x, r.y));
			}
		}
	}

	public void setConstraint(IFigure child, Object constraint) {
		super.setConstraint(child, constraint);
		if (constraint == null || !(constraint instanceof Rectangle)) {
			constraints.remove(child);
		} else {
			constraints.put(child, (Rectangle) constraint);
		}
	}

}
