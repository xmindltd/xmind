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

import org.xmind.gef.ui.actions.RequestAction;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.mindmap.IDrillDownTraceListener;
import org.xmind.ui.mindmap.IDrillDownTraceService;
import org.xmind.ui.mindmap.MindMapUI;

public class DrillUpAction extends RequestAction implements
        IDrillDownTraceListener {

    private IDrillDownTraceService traceService;

    public DrillUpAction(IGraphicalEditorPage page) {
        super(MindMapActionFactory.DRILL_UP.getId(), page,
                MindMapUI.REQ_DRILLUP);
    }

    public void setTraceService(IDrillDownTraceService traceService) {
        if (this.traceService != null) {
            this.traceService.removeTraceListener(this);
        }
        this.traceService = traceService;
        if (traceService != null) {
            traceService.addTraceListener(this);
        }
    }

    public void traceChanged(IDrillDownTraceService traceService) {
        setEnabled(traceService.canDrillUp());
    }

    public void dispose() {
        if (traceService != null) {
            traceService.removeTraceListener(this);
            traceService = null;
        }
        super.dispose();
    }

}