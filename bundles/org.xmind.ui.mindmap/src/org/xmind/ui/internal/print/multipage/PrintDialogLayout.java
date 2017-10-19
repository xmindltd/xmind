package org.xmind.ui.internal.print.multipage;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.xmind.ui.internal.print.PrintConstants;

/**
 * Used to layout previewControl and settingsPart in MultipageSetupDialog.
 * 
 * @author Shawn
 *
 */
class PrintDialogLayout extends Layout {

    private IDialogSettings settings;

    public int marginWidth = 0;

    public int marginHeight = 0;

    public int marginLeft = 0;

    public int marginTop = 0;

    public int marginRight = 0;

    public int marginBottom = 0;

    public int horizontalSpacing = 0;

    public int verticalSpacing = 0;

    public PrintDialogLayout(IDialogSettings settings) {
        this.settings = settings;
    }

    @Override
    protected Point computeSize(Composite composite, int wHint, int hHint,
            boolean flushCache) {
        Control[] children = composite.getChildren();
        Control settingsControl = children[0];
        Point size1 = settingsControl.computeSize(SWT.DEFAULT, SWT.DEFAULT,
                flushCache);
        Control previewControl = children[1];
        Point size2 = previewControl.computeSize(SWT.DEFAULT, SWT.DEFAULT,
                flushCache);

        boolean hideDetails = settings.getBoolean(PrintConstants.HIDE_DETAILS);
        if (!hideDetails) {
            int width = size1.x + size2.x + horizontalSpacing;
            int height = size1.y > size2.y ? size1.y : size2.y;
            return new Point(width + marginWidth * 2 + marginLeft + marginRight,
                    height + marginHeight * 2 + marginTop + marginBottom);
        } else {
            int width = size1.x > size2.x ? size1.x : size2.x;
            int height = size1.y + size2.y + verticalSpacing;
            return new Point(width + marginWidth * 2 + marginLeft + marginRight,
                    height + marginHeight * 2 + marginTop + marginBottom);
        }
    }

    @Override
    protected void layout(Composite composite, boolean flushCache) {
        Rectangle clientArea = composite.getBounds();

        Control[] children = composite.getChildren();
        Control settingsControl = children[0];
        Point settingsSize = settingsControl.computeSize(SWT.DEFAULT,
                SWT.DEFAULT, flushCache);
        Control previewControl = children[1];
        Point previewSize = previewControl.computeSize(SWT.DEFAULT, SWT.DEFAULT,
                flushCache);

        boolean hideDetails = settings.getBoolean(PrintConstants.HIDE_DETAILS);
        if (!hideDetails) {
            //locate center
            previewControl.setBounds(
                    new Rectangle(clientArea.x + marginWidth + marginLeft,
                            clientArea.y + marginHeight + marginTop,
                            previewSize.x, previewSize.y));
            settingsControl.setBounds(new Rectangle(
                    clientArea.x + horizontalSpacing + previewSize.x
                            + marginWidth + marginLeft,
                    clientArea.y + marginHeight + marginTop, settingsSize.x,
                    settingsSize.y));
        } else {
            //locate center
            settingsControl
                    .setBounds(
                            new Rectangle(
                                    clientArea.x + (clientArea.width
                                            - settingsSize.x) / 2,
                            clientArea.y + marginHeight + marginTop,
                            settingsSize.x, settingsSize.y));
            previewControl.setBounds(new Rectangle(
                    clientArea.x + (clientArea.width - previewSize.x) / 2,
                    clientArea.y + verticalSpacing + settingsSize.y
                            + marginHeight + marginTop,
                    previewSize.x, previewSize.y));
        }
    }

}
