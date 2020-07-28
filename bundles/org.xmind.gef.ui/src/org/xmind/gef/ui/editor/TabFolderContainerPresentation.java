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

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class TabFolderContainerPresentation extends
        PageContainerPresentationBase implements SelectionListener {

    private CTabFolder container = null;

    public void addPage(Composite container, int index, Control pageControl) {
        CTabFolder tabFolder = (CTabFolder) container;
        CTabItem item = new CTabItem(tabFolder, SWT.NONE, index);
        item.setControl(pageControl);
    }

    public Composite createContainer(Composite parent) {
        if (container != null && !container.isDisposed())
            return container;
        container = new CTabFolder(parent, SWT.BOTTOM | SWT.FLAT);
        container.setBackground(container.getDisplay().getSystemColor(
                SWT.COLOR_WHITE));
        if (hasListener()) {
            container.addSelectionListener(this);
        }
        return container;
    }

    public void disposePage(Composite container, int pageIndex) {
        CTabItem item = getItem(container, pageIndex);
        Control pageControl = item.getControl();
        item.dispose();
        if (pageControl != null) {
            pageControl.dispose();
        }
    }

    private CTabItem getItem(Composite container, int pageIndex) {
        return ((CTabFolder) container).getItem(pageIndex);
    }

    public int getActivePage(Composite container) {
        CTabFolder tabFolder = (CTabFolder) container;
        if (tabFolder != null && !tabFolder.isDisposed()) {
            return tabFolder.getSelectionIndex();
        }
        return -1;
    }

    public Control getPageControl(Composite container, int pageIndex) {
        return getItem(container, pageIndex).getControl();
    }

    public int getPageCount(Composite container) {
        CTabFolder tabFolder = (CTabFolder) container;
        return tabFolder.getItemCount();
    }

    public Image getPageImage(Composite container, int pageIndex) {
        return getItem(container, pageIndex).getImage();
    }

    public String getPageText(Composite container, int pageIndex) {
        return getItem(container, pageIndex).getText();
    }

    public void setActivePage(Composite container, int pageIndex) {
        ((CTabFolder) container).setSelection(pageIndex);
    }

    public void setPageControl(Composite container, int pageIndex,
            Control pageControl) {
        getItem(container, pageIndex).setControl(pageControl);
    }

    public void setPageImage(Composite container, int pageIndex, Image image) {
        getItem(container, pageIndex).setImage(image);
    }

    public void setPageText(Composite container, int pageIndex, String text) {
        getItem(container, pageIndex).setText(text);
    }

    public Object getSelectedPage() {
        return Integer.valueOf(getActivePage(container));
    }

    public void addPageChangedListener(IPageChangedListener listener) {
        boolean hadListener = hasListener();
        super.addPageChangedListener(listener);
        boolean hasListener = hasListener();
        if (!hadListener && hasListener) {
            if (container != null && !container.isDisposed()) {
                container.addSelectionListener(this);
            }
        }
    }

    public void removePageChangedListener(IPageChangedListener listener) {
        boolean hadListener = hasListener();
        super.removePageChangedListener(listener);
        boolean hasListener = hasListener();
        if (hadListener && !hasListener) {
            if (container != null && !container.isDisposed()) {
                container.removeSelectionListener(this);
            }
        }
    }

    public int findPage(Composite container, int x, int y) {
        CTabItem item = ((CTabFolder) container).getItem(new Point(x, y));
        if (item != null) {
            return ((CTabFolder) container).indexOf(item);
        }
        return -1;
    }

    public void widgetDefaultSelected(SelectionEvent e) {
    }

    public void widgetSelected(SelectionEvent e) {
        firePageChangedEvent();
    }

}