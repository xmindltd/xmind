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
package org.xmind.ui.richtext;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;

public abstract class RichTextActionBarContributor implements
        IRichTextActionBarContributor {

    private IRichTextEditViewer viewer;

    private Map<String, IRichTextAction> richTextActions = new HashMap<String, IRichTextAction>();

    public IRichTextEditViewer getViewer() {
        return viewer;
    }

    public void dispose() {
        for (IRichTextAction action : richTextActions.values()) {
            action.dispose();
        }
        richTextActions.clear();
        viewer = null;
    }

    public void fillMenu(IMenuManager menu) {
    }

    public void fillToolBar(IToolBarManager toolbar) {
    }

    public void fillContextMenu(IMenuManager menu) {
    }

    public void init(IRichTextEditViewer viewer) {
        this.viewer = viewer;
        makeActions(viewer);
    }

    protected abstract void makeActions(IRichTextEditViewer viewer);

    protected void addRichTextAction(IRichTextAction action) {
        if (action != null && action.getId() != null)
            richTextActions.put(action.getId(), action);
    }

    public IRichTextAction getRichTextAction(String id) {
        return richTextActions.get(id);
    }

    public void selectionChanged(ISelection selection, boolean enabled) {
        for (IRichTextAction action : richTextActions.values()) {
            action.selectionChanged(viewer, selection);
            action.setEnabled(enabled);
        }
    }

}