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
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class DeleteAction extends org.xmind.gef.ui.actions.DeleteAction {

    public DeleteAction(IGraphicalEditorPage page) {
        super(page);
    }

    protected boolean canDelete(ISelection selection) {
        return !MindMapUtils.hasSuchElements(selection,
                MindMapUI.CATEGORY_SHEET);
    }
}