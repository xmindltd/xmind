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
package org.xmind.gef;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IInputSelectionProvider;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;
import org.xmind.gef.acc.AccessibleRegistry;
import org.xmind.gef.dnd.IDndSupport;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IPartFactory;
import org.xmind.gef.part.IRootPart;
import org.xmind.gef.part.PartRegistry;
import org.xmind.gef.service.IViewerService;
import org.xmind.gef.util.Properties;

/**
 * @author Brian Sun
 */
public interface IViewer
        extends IAdaptable, IInputSelectionProvider, IPostSelectionProvider {

    public static interface IPartSearchCondition {

        boolean evaluate(IPart part);

    }

    Control getControl();

    EditDomain getEditDomain();

    void setEditDomain(EditDomain domain);

    void setInput(Object input);

    void setSelection(ISelection selection, boolean reveal);

    ISelectionSupport getSelectionSupport();

    void reveal(Object[] elements);

    void updateToolTip();

    void setCursor(Cursor cursor);

    void addFilter(ViewerFilter filter);

    void removeFilter(ViewerFilter filter);

    ViewerFilter[] getFilters();

    void setFilters(ViewerFilter[] filters);

    ViewerSorter getSorter();

    void setSorter(ViewerSorter sorter);

    ViewerComparator getComparator();

    void setComparator(ViewerComparator comparator);

    IRootPart getRootPart();

    void setRootPart(IRootPart rootPart);

    IPartFactory getPartFactory();

    void setPartFactory(IPartFactory partFactory);

    PartRegistry getPartRegistry();

    IPart findPart(Object element);

    /**
     * Coordinates represents position relative to the viewer's control.
     * 
     * @param x
     * @param y
     * @return
     */
    IPart findPart(int x, int y);

    void setPartSearchCondition(IPartSearchCondition condition);

    IPartSearchCondition getPartSearchCondition();

    Properties getProperties();

    IDndSupport getDndSupport();

    AccessibleRegistry getAccessibleRegistry();

    Object getPreselected();

    IPart getPreselectedPart();

    void setPreselected(Object element);

    Object getFocused();

    IPart getFocusedPart();

    void setFocused(Object element);

    void addPreSelectionChangedListener(ISelectionChangedListener listener);

    void removePreSelectionChangedListener(ISelectionChangedListener listener);

    void addInputChangedListener(IInputChangedListener listener);

    void removeInputChangedListener(IInputChangedListener listener);

    void addFocusedPartChangedListener(ISelectionChangedListener listener);

    void removeFocusedPartChangedListener(ISelectionChangedListener listener);

    IViewerService getService(Class<? extends IViewerService> serviceType);

    boolean hasService(Class<? extends IViewerService> serviceType);

}
