package org.xmind.cathy.internal.dashboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.AbstractHintLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

public class RecentContainerLayout extends AbstractHintLayout {
    private Map<IFigure, Object> constraints = new HashMap<IFigure, Object>();

    public RecentContainerLayout() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.draw2d.AbstractLayout#setConstraint(org.eclipse.draw2d.
     * IFigure, java.lang.Object)
     */
    @Override
    public void setConstraint(IFigure child, Object constraint) {
        constraints.put(child, constraint);
        super.setConstraint(child, constraint);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.draw2d.AbstractLayout#getConstraint(org.eclipse.draw2d.
     * IFigure)
     */
    @Override
    public Object getConstraint(IFigure child) {
        Object constraint = constraints.get(child);
        return constraint == null ? super.getConstraint(child) : constraint;
    }

    public void layout(IFigure container) {
        Rectangle area = container.getClientArea();
        for (Object child : container.getChildren()) {
            IFigure figure = (IFigure) child;
            Dimension childSize = figure.getPreferredSize(-1, -1);
            int childWidth = Math.min(area.width, childSize.width);
            int childHeight = Math.min(area.height, childSize.height);

            int childX, childY;
            Object constraint = getConstraint(figure);
            if (constraint instanceof Integer) {
                int bit = ((Integer) constraint).intValue();
                if ((bit & PositionConstants.LEFT) != 0) {
                    childX = area.x;
                } else if ((bit & PositionConstants.RIGHT) != 0) {
                    childX = area.x + area.width - childWidth;
                } else if ((bit & PositionConstants.CENTER) != 0) {
                    childX = area.x + (area.width - childWidth) / 2;
                } else {
                    childX = area.x;
                    childWidth = area.width;
                }
                if ((bit & PositionConstants.TOP) != 0) {
                    childY = area.y;
                } else if ((bit & PositionConstants.BOTTOM) != 0) {
                    childY = area.y + area.height - childHeight;
                } else if ((bit & PositionConstants.MIDDLE) != 0) {
                    childY = area.y + (area.height - childHeight) / 2;
                } else {
                    childY = area.y;
                    childHeight = area.height;
                }
            } else {
                childX = area.x;
                childY = area.y;
                childWidth = area.width;
                childHeight = area.height;
            }

            figure.setBounds(
                    new Rectangle(childX, childY, childWidth, childHeight));
        }
    }

    @Override
    protected Dimension calculatePreferredSize(IFigure figure, int wHint,
            int hHint) {
        if (wHint > -1)
            wHint = Math.max(0, wHint - figure.getInsets().getWidth());
        if (hHint > -1)
            hHint = Math.max(0, hHint - figure.getInsets().getHeight());

        Dimension d = new Dimension();
        List children = figure.getChildren();
        IFigure child;
        for (int i = 0; i < children.size(); i++) {
            child = (IFigure) children.get(i);
            if (!isObservingVisibility() || child.isVisible())
                d.union(child.getPreferredSize(wHint, hHint));
        }

        d.expand(figure.getInsets().getWidth(), figure.getInsets().getHeight());
        d.union(getBorderPreferredSize(figure));
        return d;
    }
}
