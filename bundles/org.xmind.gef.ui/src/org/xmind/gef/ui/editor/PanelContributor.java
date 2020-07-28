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

import org.xmind.gef.IGraphicalViewer;

public class PanelContributor implements IPanelContributor {

    private IPanel panel;

    private IGraphicalEditorPage page;

    public void dispose() {
    }

    public void init(IPanel panel, IGraphicalEditorPage page) {
        this.page = page;
        init(panel);
    }

    protected void init(IPanel panel) {
        this.panel = panel;
    }

    public IGraphicalEditorPage getPage() {
        return page;
    }

    public IPanel getPanel() {
        return panel;
    }

    public void contributeToPanel() {
    }

    public void setViewer(IGraphicalViewer viewer) {
    }

}