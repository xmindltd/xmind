package org.xmind.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

public class EmptyControlContribution
        extends WorkbenchWindowControlContribution {

    public EmptyControlContribution() {
        super("org.xmind.ui.emptyControlContribution"); //$NON-NLS-1$
    }

    @Override
    protected Control createControl(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE) {
            @Override
            public Point computeSize(int wHint, int hHint, boolean flushCache) {
                return new Point(25, 0);
            }
        };
        comp.setSize(25, 0);
        return comp;
    }

}
