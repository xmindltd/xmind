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
package org.xmind.core.internal.factory;

import org.eclipse.core.runtime.IAdapterFactory;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookComponent;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;

@SuppressWarnings("unchecked")
public class CoreAdapterFactory implements IAdapterFactory {

    private static Class[] LIST = { IWorkbook.class };

    public Object getAdapter(Object object, Class type) {
        if (type == IWorkbook.class) {
            if (object instanceof IWorkbook)
                return (IWorkbook) object;
            if (object instanceof IWorkbookComponent)
                return ((IWorkbookComponent) object).getOwnedWorkbook();
        } else if (type == IStyleSheet.class) {
            if (object instanceof IStyleSheet)
                return (IStyleSheet) object;
            if (object instanceof IStyle) {
                return ((IStyle) object).getOwnedStyleSheet();
            } else if (object instanceof IWorkbook) {
                return ((IWorkbook) object).getStyleSheet();
            } else if (object instanceof IWorkbookComponent)
                return ((IWorkbookComponent) object).getOwnedWorkbook()
                        .getStyleSheet();
        } else if (type == IMarkerSheet.class) {
            if (object instanceof IMarkerSheet)
                return (IMarkerSheet) object;
            if (object instanceof IMarker) {
                return ((IMarker) object).getOwnedSheet();
            } else if (object instanceof IWorkbook) {
                return ((IWorkbook) object).getMarkerSheet();
            } else if (object instanceof IWorkbookComponent) {
                return ((IWorkbookComponent) object).getOwnedWorkbook()
                        .getMarkerSheet();
            }
        }
        return null;
    }

    public Class[] getAdapterList() {
        return LIST;
    }

}