package org.xmind.cathy.internal.renderer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.xmind.cathy.internal.ICathyConstants;

public class XRightTrimBarLayout extends Layout {

    private static final int MARGIN_LEFT = -2;
    private static final int MARGIN_RIGHT = -2;
    private static final int MARGIN_TOP = 20;
    private static final int MARGIN_BOTTOM = 0;

    @Override
    protected Point computeSize(Composite composite, int wHint, int hHint,
            boolean flushCache) {
        int width = 0;
        Control[] children = composite.getChildren();
        for (Control child : children) {
            Point size = child.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
            width = Math.max(width, size.x);
        }
        width = width + MARGIN_LEFT + MARGIN_RIGHT;
        return new Point(width, hHint);
    }

    @Override
    protected void layout(Composite composite, boolean flushCache) {
        Rectangle bounds = composite.getBounds();
        bounds.x = MARGIN_LEFT;
        bounds.y = MARGIN_TOP;
        bounds.width -= (MARGIN_LEFT + MARGIN_RIGHT);
        bounds.height -= (MARGIN_TOP + MARGIN_BOTTOM);

        int curX = bounds.x;
        int curY = bounds.y;

        List<Control> beginingControls = new ArrayList<Control>();
        List<Control> centerControls = new ArrayList<Control>();
        List<Control> endControls = new ArrayList<Control>();

        int centerControlsHeight = 0;
        int endControlsHeight = 0;

        Control[] children = composite.getChildren();
        for (Control child : children) {
            if (isBegining(child)) {
                beginingControls.add(child);
            } else if (isCenter(child)) {
                centerControls.add(child);
                centerControlsHeight += child.computeSize(SWT.DEFAULT,
                        SWT.DEFAULT).y;
            } else if (isEnd(child)) {
                endControls.add(child);
                endControlsHeight += child.computeSize(SWT.DEFAULT,
                        SWT.DEFAULT).y;
            } else {
                centerControls.add(child);
                centerControlsHeight += child.computeSize(SWT.DEFAULT,
                        SWT.DEFAULT).y;
            }
        }

        for (Control bc : beginingControls) {
            Point size = bc.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            bc.setBounds(curX, curY, size.x, size.y);
            curY += size.y;
        }

        curY = Math.max(curY, (bounds.height - centerControlsHeight) / 2);

        for (Control cc : centerControls) {
            Point size = cc.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            cc.setBounds(curX, curY, size.x, size.y);
            curY += size.y;
        }

        curY = Math.max(curY, bounds.y + bounds.height - endControlsHeight);

        for (Control ec : endControls) {
            Point size = ec.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            ec.setBounds(curX, curY, size.x, size.y);
            curY += size.y;
        }

    }

    private boolean isBegining(Control ctrl) {
        MUIElement element = (MUIElement) ctrl
                .getData(AbstractPartRenderer.OWNING_ME);
        if (element != null && element.getTags()
                .contains(ICathyConstants.TAG_TRIMBAR_LAYOUT_BEGINING))
            return true;

        return false;
    }

    private boolean isCenter(Control ctrl) {
        MUIElement element = (MUIElement) ctrl
                .getData(AbstractPartRenderer.OWNING_ME);
        if (element != null && element.getTags()
                .contains(ICathyConstants.TAG_TRIMBAR_LAYOUT_CENTER))
            return true;

        return false;
    }

    private boolean isEnd(Control ctrl) {
        MUIElement element = (MUIElement) ctrl
                .getData(AbstractPartRenderer.OWNING_ME);
        if (element != null && element.getTags()
                .contains(ICathyConstants.TAG_TRIMBAR_LAYOUT_END))
            return true;

        return false;
    }

}
