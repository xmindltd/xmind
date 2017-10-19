package org.xmind.gef.draw2d;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToolTipHelper;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

/**
 * This will keep the tooltip always visible during mouse hover in.
 * 
 * @author Shawn
 * 
 */
public class KeepVisibleToolTipHelper extends ToolTipHelper {

    public KeepVisibleToolTipHelper(Control control) {
        super(control);
    }

    private IFigure currentTipSource;

    private Point computeWindowLocation(IFigure tip, int eventX, int eventY) {
        org.eclipse.swt.graphics.Rectangle clientArea = control.getDisplay()
                .getClientArea();
        Point preferredLocation = new Point(eventX, eventY + 26);

        Dimension tipSize = getLightweightSystem().getRootFigure()
                .getPreferredSize().getExpanded(getShellTrimSize());

        // Adjust location if tip is going to fall outside display
        if (preferredLocation.y + tipSize.height > clientArea.height)
            preferredLocation.y = eventY - tipSize.height;

        if (preferredLocation.x + tipSize.width > clientArea.width)
            preferredLocation.x -= (preferredLocation.x + tipSize.width)
                    - clientArea.width;

        return preferredLocation;
    }

    public void displayToolTipNear(IFigure hoverSource, IFigure tip,
            int eventX, int eventY) {
        if (tip != null && hoverSource != currentTipSource) {
            getLightweightSystem().setContents(tip);
            Point displayPoint = computeWindowLocation(tip, eventX, eventY);
            Dimension shellSize = getLightweightSystem().getRootFigure()
                    .getPreferredSize().getExpanded(getShellTrimSize());
            setShellBounds(displayPoint.x, displayPoint.y, shellSize.width,
                    shellSize.height);
            show();
            currentTipSource = hoverSource;
        }
    }

    public void dispose() {
        if (isShowing()) {
            hide();
        }
        getShell().dispose();
    }

    protected void hookShellListeners() {
        // Close the tooltip window if the mouse enters the tooltip
        getShell().addMouseTrackListener(new MouseTrackAdapter() {
            public void mouseEnter(org.eclipse.swt.events.MouseEvent e) {
                hide();
                currentTipSource = null;
            }
        });
    }

    public void updateToolTip(IFigure figureUnderMouse, IFigure tip,
            int eventX, int eventY) {
        /*
         * If the cursor is not on any Figures, it has been moved off of the
         * control. Hide the tool tip.
         */
        if (figureUnderMouse == null) {
            if (isShowing()) {
                hide();
            }
        }
        // Makes tooltip appear without a hover event if a tip is currently
        // being displayed
        if (isShowing() && figureUnderMouse != currentTipSource) {
            hide();
            displayToolTipNear(figureUnderMouse, tip, eventX, eventY);
        } else if (!isShowing() && figureUnderMouse != currentTipSource)
            currentTipSource = null;
    }
}
