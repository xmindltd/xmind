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
package org.xmind.ui.internal.actions;

import org.eclipse.jface.viewers.ISelection;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.part.IPart;
import org.xmind.gef.ui.actions.ISelectionAction;
import org.xmind.gef.ui.actions.RequestAction;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;

public class TraverseAction extends RequestAction implements ISelectionAction {

    public TraverseAction(IGraphicalEditorPage page) {
        this(MindMapActionFactory.TRAVERSE.getId(), page);
    }

    public TraverseAction(String id, IGraphicalEditorPage page) {
        super(id, page, GEF.REQ_TRAVERSE);
    }

    public void setSelection(ISelection selection) {
        setEnabled(isSelectionTraversable(selection));
    }

    private boolean isSelectionTraversable(ISelection selection) {
        return getTraversablePart(selection) != null;
    }

    private IPart getTraversablePart(ISelection selection) {
        IGraphicalViewer viewer = getViewer();
        if (viewer != null) {
            IPart part = viewer.getFocusedPart(); //findPart(element);
            if (part != null && part.getStatus().isActive()
                    && part.hasRole(GEF.ROLE_TRAVERSABLE))
                return part;
        }
        return null;
    }

}