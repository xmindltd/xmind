/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.gef.ui.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.PageBook;

public class PageBookContainerPresentation extends
        PageContainerPresentationBase {

    private PageBook container = null;

    private int activePageIndex = -1;

    public void addPage(Composite container, int index, Control pageControl) {
        setPageControl(container, index, pageControl);
    }

    public Composite createContainer(Composite parent) {
        container = new PageBook(parent, SWT.NONE);
        return container;
    }

    public void disposePage(Composite container, int pageIndex) {
        Control pageControl = getPageControl(container, pageIndex);
        if (pageControl != null) {
            pageControl.dispose();
        }
    }

    public int getActivePage(Composite container) {
        if (activePageIndex == -1 && getPageCount(container) > 0) {
            activePageIndex = 0;
        }
        return activePageIndex;
    }

    public Control getPageControl(Composite container, int pageIndex) {
        return container.getChildren()[pageIndex];
    }

    public int getPageCount(Composite container) {
        return container.getChildren().length;
    }

    public Image getPageImage(Composite container, int pageIndex) {
        return null;
    }

    public String getPageText(Composite container, int pageIndex) {
        return null;
    }

    public void setActivePage(Composite container, int pageIndex) {
        ((PageBook) container).showPage(getPageControl(container, pageIndex));
        if (pageIndex != getActivePage(container)) {
            this.activePageIndex = pageIndex;
            firePageChangedEvent();
        }
    }

    public void setPageControl(Composite container, int pageIndex,
            Control pageControl) {
        if (pageIndex < 0 || pageIndex >= getPageCount(container)) {
            pageControl.moveAbove(null);
        } else {
            Control control = getPageControl(container, pageIndex);
            pageControl.moveAbove(control);
        }
    }

    public void setPageImage(Composite container, int pageIndex, Image image) {
    }

    public void setPageText(Composite container, int pageIndex, String text) {
    }

    public int findPage(Composite container, int x, int y) {
        return -1;
    }

    public Object getSelectedPage() {
        return Integer.valueOf(getActivePage(container));
    }

}