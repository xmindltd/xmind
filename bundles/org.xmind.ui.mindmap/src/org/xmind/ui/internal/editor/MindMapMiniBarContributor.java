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
package org.xmind.ui.internal.editor;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.xmind.gef.ui.editor.IMiniBar;
import org.xmind.gef.ui.editor.MiniBarContributor;
import org.xmind.ui.internal.actions.ActionConstants;

public class MindMapMiniBarContributor extends MiniBarContributor {

    private MiniZoomContribution zoomContribution;

    private OverviewCheckContribution overviewCheckContribution;

    protected void init(IMiniBar bar) {
        zoomContribution = new MiniZoomContribution(getEditor());
        overviewCheckContribution = new OverviewCheckContribution();
        super.init(bar);
    }

    public void contributeToToolBar(IToolBarManager toolBar) {
        toolBar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        toolBar.add(new Separator(ActionConstants.GROUP_FILTER));
        toolBar.add(new Separator(ActionConstants.GROUP_ZOOM));
        if (overviewCheckContribution != null)
            toolBar.add(overviewCheckContribution);
        toolBar.add(new Separator(ActionConstants.GROUP_ZOOM));
        if (zoomContribution != null)
            toolBar.add(zoomContribution);
    }

    public void dispose() {
        if (zoomContribution != null) {
            zoomContribution.dispose();
            zoomContribution = null;
        }

        if (overviewCheckContribution != null) {
            overviewCheckContribution.dispose();
            overviewCheckContribution = null;
        }
        super.dispose();
    }

}
