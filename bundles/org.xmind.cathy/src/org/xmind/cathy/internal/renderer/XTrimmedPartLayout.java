package org.xmind.cathy.internal.renderer;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.e4.ui.workbench.renderers.swt.TrimmedPartLayout;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class XTrimmedPartLayout extends TrimmedPartLayout {

    public Control headerSeparator;
    public Control footerSeparator;

    public Map<Object, Composite> containers = new HashMap<Object, Composite>();

    public XTrimmedPartLayout(Composite parent) {
        super(parent);
        clientArea.setVisible(false);
    }

    public void createHeaderSeparator(Composite parent) {
        if (headerSeparator != null)
            return;

        headerSeparator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        headerSeparator.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                headerSeparator = null;
            }
        });
    }

    public void createFooterSeparator(Composite parent) {
        if (footerSeparator != null)
            return;

        footerSeparator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        footerSeparator.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                footerSeparator = null;
            }
        });
    }

    @Override
    protected void layout(Composite composite, boolean flushCache) {
        Rectangle ca = composite.getClientArea();
        Rectangle caRect = new Rectangle(ca.x, ca.y, ca.width, ca.height);

        // 'Top' spans the entire area
        if (top != null && top.isVisible()) {
            Point topSize = top.computeSize(
                    caRect.width - (Util.isMac() ? 12 : 0), SWT.DEFAULT, true); /// -12 for mac margin
            caRect.y += topSize.y;
            caRect.height -= topSize.y;

            // Don't layout unless we've changed
            Rectangle newBounds = new Rectangle(ca.x, ca.y, caRect.width,
                    topSize.y);
            if (!newBounds.equals(top.getBounds())) {
                top.setBounds(newBounds);
            }
        }

        // 'Header Separator' spans the entire area
        if (headerSeparator != null && headerSeparator.isVisible()) {
            Point underTopSize = headerSeparator.computeSize(caRect.width,
                    SWT.DEFAULT, true);
            caRect.y += underTopSize.y;
            caRect.height -= underTopSize.y;

            // Don't layout unless we've changed
            Rectangle newBounds = new Rectangle(caRect.x,
                    caRect.y - underTopSize.y, caRect.width, underTopSize.y);
            if (!newBounds.equals(headerSeparator.getBounds())) {
                headerSeparator.setBounds(newBounds);
            }
        }

        // Include the gutter whether there is a top area or not.
        caRect.y += gutterTop;
        caRect.height -= gutterTop;

        // 'Bottom' spans the entire area
        if (bottom != null && bottom.isVisible()) {
            Point bottomSize = bottom.computeSize(caRect.width, SWT.DEFAULT,
                    true);
            caRect.height -= bottomSize.y;

            // Don't layout unless we've changed
            Rectangle newBounds = new Rectangle(caRect.x,
                    caRect.y + caRect.height, caRect.width, bottomSize.y);
            if (!newBounds.equals(bottom.getBounds())) {
                bottom.setBounds(newBounds);
            }
        }

        // 'Footer Separator' spans the entire area
        if (footerSeparator != null && footerSeparator.isVisible()) {
            Point aboveBottomSize = footerSeparator.computeSize(caRect.width,
                    SWT.DEFAULT, true);
            caRect.height -= aboveBottomSize.y;

            // Don't layout unless we've changed
            Rectangle newBounds = new Rectangle(caRect.x,
                    caRect.y + caRect.height, caRect.width, aboveBottomSize.y);
            if (!newBounds.equals(footerSeparator.getBounds())) {
                footerSeparator.setBounds(newBounds);
            }
        }

        caRect.height -= gutterBottom;

        // 'Left' spans between 'top' and 'bottom'
        if (left != null && left.isVisible()) {
            Point leftSize = left.computeSize(SWT.DEFAULT, caRect.height, true);
            caRect.x += leftSize.x;
            caRect.width -= leftSize.x;

            // Don't layout unless we've changed
            Rectangle newBounds = new Rectangle(caRect.x - leftSize.x, caRect.y,
                    leftSize.x, caRect.height);
            if (!newBounds.equals(left.getBounds())) {
                left.setBounds(newBounds);
            }
        }
        caRect.x += gutterLeft;
        caRect.width -= gutterLeft;

        // 'Right' spans between 'top' and 'bottom'
        if (right != null && right.isVisible()) {
            Point rightSize = right.computeSize(SWT.DEFAULT, caRect.height,
                    true);
            caRect.width -= rightSize.x;

            // Don't layout unless we've changed
            Rectangle newBounds = new Rectangle(caRect.x + caRect.width,
                    caRect.y, rightSize.x, caRect.height);
            if (!newBounds.equals(right.getBounds())) {
                right.setBounds(newBounds);
            }
        }
        caRect.width -= gutterRight;

        // Don't layout unless we've changed
        if (clientArea.isVisible()) {
            if (!caRect.equals(clientArea.getBounds())) {
                clientArea.setBounds(caRect);
            }
        }

        for (Composite container : containers.values()) {
            if (container != null && !container.isDisposed()
                    && container.isVisible()) {
                if (!ca.equals(container.getBounds())) {
                    container.setBounds(ca);
                }
            }
        }

    }

    public Composite getContainer(Composite parent, final Object child) {
        Composite container = containers.get(child);
        if (container == null || container.isDisposed()) {
            container = new Composite(parent, SWT.NONE);
            container.setLayout(new FillLayout());
            container.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e) {
                    Composite c = containers.get(child);
                    if (c != e.widget) {
                        containers.remove(child);
                    }
                }
            });
            containers.put(child, container);
        }
        return container;
    }

    public void removeContainer(Object child) {
        Composite container = containers.remove(child);
        if (container != null) {
            container.dispose();
        }
    }

    @Override
    public Composite getTrimComposite(Composite parent, int side) {
        if (side == SWT.RIGHT) {
            if (right == null) {
                right = new Composite(parent, SWT.NONE);
                right.setLayout(new XRightTrimBarLayout());
                right.addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e) {
                        right = null;
                    }
                });
            }
            return right;
        }
        Composite trim = super.getTrimComposite(parent, side);
        if (trim != null && clientArea != null && !clientArea.isDisposed()) {
            trim.setVisible(clientArea.isVisible());
        }
        return trim;
    }

}
