package org.xmind.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * A PageStack is a special page that can stack multiple inner pages where only
 * a single page is visible at a time. It is similar to a notebook, but without
 * tabs.
 *
 * @author Frank Shaka
 * @since 3.6.50
 */
public class PageStack extends Page {

    /**
     * 
     */
    private class PageStackLayout extends Layout {

        @Override
        protected Point computeSize(Composite composite, int wHint, int hHint,
                boolean flushCache) {
            if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
                return new Point(wHint, hHint);
            }

            Point result = null;
            if (currentPage != null && currentPage.getControl() != null
                    && !currentPage.getControl().isDisposed()) {
                result = currentPage.getControl().computeSize(wHint, hHint,
                        flushCache);
            } else {
                //Rectangle rect= composite.getClientArea();
                //result= new Point(rect.width, rect.height);
                result = new Point(0, 0);
            }
            if (wHint != SWT.DEFAULT) {
                result.x = wHint;
            }
            if (hHint != SWT.DEFAULT) {
                result.y = hHint;
            }
            return result;
        }

        @Override
        protected void layout(Composite composite, boolean flushCache) {
            if (currentPage != null && currentPage.getControl() != null
                    && !currentPage.getControl().isDisposed()) {
                currentPage.getControl().setBounds(composite.getClientArea());
            }
        }
    }

    /**
     * The current page; <code>null</code> if none.
     */
    private IPage currentPage = null;

    public Control doCreateControl(Composite parent) {
        Composite stack = new Composite(parent, SWT.NONE);
        stack.setLayout(new PageStackLayout());
        return stack;
    }

    public void setFocus() {
        if (currentPage != null && setFocus(currentPage.getControl()))
            return;
        super.setFocus();
    }

    public Composite getStackComposite() {
        return (Composite) getControl();
    }

    public IPage getCurrentPage() {
        return currentPage;
    }

    /**
     * Shows the given page. This method has no effect if the given page is not
     * contained in this pagebook.
     *
     * @param page
     *            the page to show
     */
    public void showPage(IPage page) {
        if (page.getControl() == null || page.getControl().isDisposed()
                || page.getControl().getParent() != getStackComposite()) {
            return;
        }

        currentPage = page;

        // show new page
        page.getControl().setVisible(true);
        getStackComposite().layout(true);

        // hide old (and all others) *after* new page has been made visible in
        // order to avoid flashing
        Control[] children = getStackComposite().getChildren();
        for (int i = 0; i < children.length; i++) {
            Control child = children[i];
            if (child != page.getControl() && !child.isDisposed()) {
                child.setVisible(false);
            }
        }
    }

}
