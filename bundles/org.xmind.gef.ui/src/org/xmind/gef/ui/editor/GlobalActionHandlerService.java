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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.ui.IActionBars;

/**
 * @author Frank Shaka
 * 
 */
public class GlobalActionHandlerService implements IGlobalActionHandlerService,
        IPageChangedListener {

    private IGraphicalEditor editor;

    private List<IActionBars> actionBarsList = null;

    /**
     * 
     */
    public GlobalActionHandlerService(IGraphicalEditor editor) {
        this.editor = editor;
        editor.addPageChangedListener(this);
    }

    public void addActionBars(IActionBars actionBars) {
        if (actionBarsList != null && actionBarsList.contains(actionBars))
            return;

        if (actionBarsList == null)
            actionBarsList = new ArrayList<IActionBars>();
        actionBarsList.add(actionBars);
        update(actionBars, getUpdater());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.ui.editor.IGlobalActionHandlerService#removeActionBars(
     * org.eclipse.ui.IActionBars)
     */
    public void removeActionBars(IActionBars actionBars) {
        if (actionBarsList != null) {
            actionBarsList.remove(actionBars);
        }
        if (actionBars != null) {
            actionBars.clearGlobalActionHandlers();
            actionBars.updateActionBars();
        }
    }

    private IGlobalActionHandlerUpdater getUpdater() {
        return (IGlobalActionHandlerUpdater) editor
                .getAdapter(IGlobalActionHandlerUpdater.class);
    }

    private void update(IActionBars actionBars,
            IGlobalActionHandlerUpdater updater) {
        if (updater == null)
            return;

        updater.updateGlobalActionHandlers(actionBars);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.dialogs.IPageChangedListener#pageChanged(org.eclipse
     * .jface.dialogs.PageChangedEvent)
     */
    public void pageChanged(PageChangedEvent event) {
        if (actionBarsList != null) {
            IGlobalActionHandlerUpdater updater = getUpdater();
            if (updater != null) {
                for (IActionBars actionBars : actionBarsList) {
                    update(actionBars, updater);
                }
            }
        }
    }

}
