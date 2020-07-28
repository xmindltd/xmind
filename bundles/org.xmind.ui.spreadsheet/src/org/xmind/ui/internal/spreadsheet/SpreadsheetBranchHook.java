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
package org.xmind.ui.internal.spreadsheet;

import org.eclipse.swt.widgets.Control;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.ui.branch.IBranchHook;
import org.xmind.ui.internal.mindmap.BranchPart;
import org.xmind.ui.mindmap.IBranchPart;

public class SpreadsheetBranchHook implements IBranchHook, ICoreEventListener {

    private IBranchPart branch;

    private ICoreEventRegister register;

    public void hook(IBranchPart branch) {
        this.branch = branch;
        ITopic topic = branch.getTopic();
        register = new CoreEventRegister(topic, this);
        register.register(Spreadsheet.EVENT_MODIFY_COLUMN_ORDER);
        register.register(Spreadsheet.EVENT_MODIFY_ROW_ORDER);

        ((BranchPart) branch).getRequestHandler().installEditPolicy(
                Spreadsheet.ROLE_SHEET_HEAD_MODIFIABLE,
                Spreadsheet.POLICY_SHEET_HEAD_MODIFIABLE);
    }

    public void unhook(IBranchPart branch) {
        ((BranchPart) branch).getRequestHandler()
                .uninstallEditPolicy(Spreadsheet.ROLE_SHEET_HEAD_MODIFIABLE);

        if (register != null) {
            register.unregisterAll();
            register = null;
        }
        this.branch = null;
    }

    public void handleCoreEvent(CoreEvent event) {
        if (branch != null && (Spreadsheet.EVENT_MODIFY_COLUMN_ORDER
                .equals(event.getType())
                || Spreadsheet.EVENT_MODIFY_ROW_ORDER
                        .equals(event.getType()))) {
            Control c = branch.getSite().getViewerControl();
            if (c != null && !c.isDisposed()) {
                c.getDisplay().syncExec(new Runnable() {
                    public void run() {
                        branch.getFigure().revalidate();
                        branch.getFigure().repaint();
                    }
                });
            }
        }
    }

}