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
package org.xmind.ui.internal.outline;

import org.xmind.core.IAdaptable;
import org.xmind.core.ISheet;
import org.xmind.core.ISheetComponent;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookComponent;
import org.xmind.gef.tree.TreeViewer;

public class MindMapTreeViewer extends TreeViewer {

    public <T> T getAdapter(Class<T> adapter) {
        if (ISheet.class.equals(adapter)) {
            return adapter.cast(getSheet());
        } else if (ITopic.class.equals(adapter)) {
            return adapter.cast(getTopTopic());
        } else if (IWorkbook.class.equals(adapter)) {
            return adapter.cast(getWorkbook());
        }
        return super.getAdapter(adapter);
    }

    public ISheet getSheet() {
        Object input = getInput();
        if (input instanceof ISheet)
            return (ISheet) input;
        if (input instanceof ISheetComponent)
            return ((ISheetComponent) input).getOwnedSheet();
        if (input instanceof IWorkbook)
            return ((IWorkbook) input).getPrimarySheet();
        if (input instanceof IAdaptable) {
            ISheet adapter = (ISheet) ((IAdaptable) input)
                    .getAdapter(ISheet.class);
            if (adapter != null)
                return adapter;
        }
        if (input instanceof org.eclipse.core.runtime.IAdaptable) {
            return (ISheet) ((org.eclipse.core.runtime.IAdaptable) input)
                    .getAdapter(ISheet.class);
        }
        return null;
    }

    public IWorkbook getWorkbook() {
        Object input = getInput();
        if (input instanceof IWorkbook)
            return (IWorkbook) input;
        if (input instanceof IWorkbookComponent)
            return ((IWorkbookComponent) input).getOwnedWorkbook();
        if (input instanceof IAdaptable) {
            IWorkbook adapter = (IWorkbook) ((IAdaptable) input)
                    .getAdapter(IWorkbook.class);
            if (adapter != null)
                return adapter;
        }
        if (input instanceof org.eclipse.core.runtime.IAdaptable) {
            return (IWorkbook) ((org.eclipse.core.runtime.IAdaptable) input)
                    .getAdapter(IWorkbook.class);
        }
        return null;
    }

    public ITopic getTopTopic() {
        Object input = getInput();
        if (input instanceof ITopic)
            return (ITopic) input;
        if (input instanceof ISheet)
            return ((ISheet) input).getRootTopic();
        if (input instanceof IWorkbook) {
            return ((IWorkbook) input).getPrimarySheet().getRootTopic();
        }
        if (input instanceof IAdaptable) {
            ITopic adapter = (ITopic) ((IAdaptable) input)
                    .getAdapter(ITopic.class);
            if (adapter != null)
                return adapter;
        }
        if (input instanceof org.eclipse.core.runtime.IAdaptable) {
            return (ITopic) ((org.eclipse.core.runtime.IAdaptable) input)
                    .getAdapter(ITopic.class);
        }
        return null;
    }
}