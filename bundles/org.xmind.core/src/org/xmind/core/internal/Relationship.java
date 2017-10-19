/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.core.internal;

import org.xmind.core.IRelationship;
import org.xmind.core.IRelationshipEnd;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.core.style.IStyle;

/**
 * @author briansun
 * 
 */
public abstract class Relationship extends AbstractWorkbookComponent
        implements IRelationship {

    public String getStyleType() {
        return IStyle.RELATIONSHIP;
    }

    /**
     * @see org.xmind.core.IRelationship#getEnd1()
     */
    public IRelationshipEnd getEnd1() {
        String end1Id = getEnd1Id();
        if (end1Id == null)
            return null;
        IWorkbook workbook = getOwnedWorkbook();
        if (workbook == null)
            return null;
        Object element = workbook.getElementById(end1Id);
        if (element instanceof IRelationshipEnd)
            return (IRelationshipEnd) element;
        return null;
    }

    /**
     * @see org.xmind.core.IRelationship#getEnd2()
     */
    public IRelationshipEnd getEnd2() {
        String end2Id = getEnd2Id();
        if (end2Id == null)
            return null;
        IWorkbook workbook = getOwnedWorkbook();
        if (workbook == null)
            return null;
        Object element = workbook.getElementById(end2Id);
        if (element instanceof IRelationshipEnd)
            return (IRelationshipEnd) element;
        return null;
    }

    /**
     * Clients may override this method.
     * 
     * @see org.xmind.core.ITitled#getTitleText()
     */
    public String getTitleText() {
        String t = getLocalTitleText();
        return t == null ? "" : t; //$NON-NLS-1$
    }

    /**
     * @see org.xmind.core.ITitled#hasTitle()
     */
    public boolean hasTitle() {
        return getLocalTitleText() != null;
    }

    public ISheet getOwnedSheet() {
        return getParent();
    }

    /**
     * @return
     */
    protected abstract String getLocalTitleText();

    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == ISheet.class)
            return adapter.cast(getOwnedSheet());

        return super.getAdapter(adapter);
    }

//    /**
//     * @return
//     */
//    protected abstract IWorkbook getOwnedWorkbook();

    public String toString() {
        return "RELATIONSHIP (" + getTitleText() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}