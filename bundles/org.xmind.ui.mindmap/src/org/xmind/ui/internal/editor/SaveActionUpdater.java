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

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.xmind.core.Core;
import org.xmind.core.IMeta;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.ui.internal.MindMapMessages;

public class SaveActionUpdater implements IPartListener, IPropertyListener,
        ICoreEventListener {

    private IWorkbenchWindow window;

    private IWorkbenchAction action;

    private IEditorPart editor = null;

    private IWorkbook workbook = null;

    private ICoreEventRegistration eventReg = null;

    public SaveActionUpdater(IWorkbenchWindow window, IWorkbenchAction action) {
        this.window = window;
        this.action = action;
        window.getPartService().addPartListener(this);
    }

    public void dispose() {
        handleEditorChange(null);
        if (window != null) {
            window.getPartService().removePartListener(this);
            window = null;
        }
    }

    private void handleEditorChange(IEditorPart newEditor) {
        IEditorPart oldEditor = this.editor;
        this.editor = newEditor;
        if (oldEditor != null) {
            oldEditor.removePropertyListener(this);
        }
        if (newEditor != null) {
            newEditor.addPropertyListener(this);
        }
        handleWorkbookChange(newEditor == null ? null : (IWorkbook) newEditor
                .getAdapter(IWorkbook.class));
    }

    private void handleWorkbookChange(IWorkbook newWorkbook) {
        this.workbook = newWorkbook;
        if (eventReg != null) {
            eventReg.unregister();
            eventReg = null;
        }
        if (newWorkbook != null) {
            IMeta meta = newWorkbook.getMeta();
            if (meta instanceof ICoreEventSource) {
                eventReg = ((ICoreEventSource) meta).registerCoreEventListener(
                        Core.Metadata, this);
            }
        }
        updateText();
    }

    private void updateText() {
        if (workbook != null && isAutoSave(workbook)) {
            action.setText(MindMapMessages.SaveNewRevision_text);
        } else {
            action.setText(WorkbenchMessages.SaveAction_text);
        }
    }

    private static final boolean isAutoSave(IWorkbook workbook) {
        String value = workbook.getMeta().getValue(
                IMeta.CONFIG_AUTO_REVISION_GENERATION);
        return value == null || IMeta.V_YES.equalsIgnoreCase(value);
    }

    public void partActivated(IWorkbenchPart part) {
        if (!(part instanceof IEditorPart))
            return;

        handleEditorChange((IEditorPart) part);
    }

    public void partBroughtToTop(IWorkbenchPart part) {
    }

    public void partClosed(IWorkbenchPart part) {
    }

    public void partDeactivated(IWorkbenchPart part) {
    }

    public void partOpened(IWorkbenchPart part) {
    }

    public void propertyChanged(Object source, int propId) {
        if (propId == IEditorPart.PROP_INPUT) {
            handleWorkbookChange(editor == null ? null : (IWorkbook) editor
                    .getAdapter(IWorkbook.class));
        }
    }

    public void handleCoreEvent(final CoreEvent event) {
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            public void run() {
                if (IMeta.CONFIG_AUTO_REVISION_GENERATION.equals(event
                        .getTarget())) {
                    updateText();
                }
            }
        });
    }

}
