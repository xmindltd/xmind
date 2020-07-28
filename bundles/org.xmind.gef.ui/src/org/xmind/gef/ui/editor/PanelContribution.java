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

public abstract class PanelContribution implements IPanelContribution {

    private IPanel panel = null;

    private boolean visible = true;

    public IPanel getPanel() {
        return panel;
    }

    public void setPanel(IPanel panel) {
        this.panel = panel;
    }

    protected void updatePanel() {
        if (panel != null) {
            panel.update();
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        if (visible == this.visible)
            return;
        this.visible = visible;
        updatePanel();
    }

}