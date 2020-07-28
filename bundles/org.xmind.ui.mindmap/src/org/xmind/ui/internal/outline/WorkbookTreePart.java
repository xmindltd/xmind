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
package org.xmind.ui.internal.outline;

import org.xmind.core.Core;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;

public class WorkbookTreePart extends MindMapTreePartBase {

    public WorkbookTreePart(IWorkbook model) {
        super(model);
    }

    public IWorkbook getWorkbook() {
        return (IWorkbook) super.getModel();
    }

    protected Object[] getModelChildren(Object model) {
        return getWorkbook().getSheets().toArray();
    }

    protected void registerCoreEvents(Object source, ICoreEventRegister register) {
        super.registerCoreEvents(source, register);
        register.register(Core.SheetAdd);
        register.register(Core.SheetMove);
        register.register(Core.SheetRemove);
    }

    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.SheetAdd.equals(type) || Core.SheetRemove.equals(type)
                || Core.SheetMove.equals(type)) {
            runInUI(new Runnable() {
                public void run() {
                    refresh();
                }
            }, false);
        } else {
            super.handleCoreEvent(event);
        }
    }

}