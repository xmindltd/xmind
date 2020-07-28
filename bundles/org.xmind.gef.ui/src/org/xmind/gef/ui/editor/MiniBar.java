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

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;

public class MiniBar implements IMiniBar {

    private IToolBarManager toolBarManager;

    public IToolBarManager getToolBarManager() {
        if (toolBarManager == null) {
            toolBarManager = createToolBarManager();
        }
        return toolBarManager;
    }

    protected IToolBarManager createToolBarManager() {
        return new ToolBarManager(SWT.RIGHT | SWT.FLAT) {
            public void update(boolean force) {
                super.update(force);
                updateMiniBar();
            }
        };
    }

    protected void updateMiniBar() {
    }

    public void updateBar() {
        if (toolBarManager != null)
            toolBarManager.update(true);
    }

    protected boolean isEmpty() {
        return toolBarManager == null || toolBarManager.isEmpty();
    }

}