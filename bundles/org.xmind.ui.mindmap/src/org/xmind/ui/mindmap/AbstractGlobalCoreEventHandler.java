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
package org.xmind.ui.mindmap;

import org.eclipse.core.runtime.IAdaptable;
import org.xmind.core.ISheet;
import org.xmind.core.ISheetComponent;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.part.IPart;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;

public abstract class AbstractGlobalCoreEventHandler implements
        IGlobalCoreEventHandler {

    protected IPart findPart(Object source, IGraphicalEditor editor) {
        Object pageInput = findPageInput(source);
        if (pageInput == null)
            return null;

        IGraphicalEditorPage page = editor.findPage(pageInput);
        if (page == null)
            return null;

        IGraphicalViewer viewer = page.getViewer();
        if (viewer == null)
            return null;

        return viewer.findPart(source);
    }

    protected Object findPageInput(Object o) {
        if (o instanceof ISheet)
            return o;
        if (o instanceof ISheetComponent)
            return ((ISheetComponent) o).getOwnedSheet();
        if (o instanceof IAdaptable)
            return ((IAdaptable) o).getAdapter(ISheet.class);
        return null;
    }
}