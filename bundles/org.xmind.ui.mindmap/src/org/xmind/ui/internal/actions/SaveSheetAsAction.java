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

import java.util.Arrays;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.xmind.core.Core;
import org.xmind.core.ICloneData;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventSource2;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.MindMapUI;

public class SaveSheetAsAction extends Action implements IWorkbenchAction {

    private IGraphicalEditorPage page;

    public SaveSheetAsAction() {
        setId(ActionConstants.SAVE_SHEET_AS_ID);
        setText(MindMapMessages.SaveSheetAs_text);
        setToolTipText(MindMapMessages.SaveSheetAs_toolTip);
    }

    public void setActivePage(IGraphicalEditorPage page) {
        this.page = page;
        setEnabled(page != null && page.getAdapter(IMindMap.class) != null);
    }

    public void run() {
        if (page == null)
            return;

        IMindMap mindmap = (IMindMap) page.getAdapter(IMindMap.class);
        if (mindmap == null)
            return;

        ISheet sheet = mindmap.getSheet();
        final IWorkbook newWorkbook = Core.getWorkbookBuilder()
                .createWorkbook(MME.createTempStorage());
        try {
            newWorkbook.saveTemp();
        } catch (Exception ignore) {
        }
        ICloneData clone = newWorkbook.clone(Arrays.asList(sheet));
        ISheet newSheet = (ISheet) clone.get(sheet);
        initSheet(newSheet);
        ITopic newCentralTopic = (ITopic) clone.get(mindmap.getCentralTopic());
        if (newCentralTopic == null)
            //TODO should we log this?
            return;

        newSheet.replaceRootTopic(newCentralTopic);
        newWorkbook.addSheet(newSheet);
        newWorkbook.removeSheet(newWorkbook.getPrimarySheet());

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                final IEditorPart newEditor = page.getParentEditor().getSite()
                        .getPage().openEditor(
                                MindMapUI.getEditorInputFactory()
                                        .createEditorInputForPreLoadedWorkbook(
                                                newWorkbook, null),
                                //new WorkbookEditorInput(newWorkbook, null, true),
                                MindMapUI.MINDMAP_EDITOR_ID, true);
                // Forcely make editor saveable:
                if (newWorkbook instanceof ICoreEventSource2) {
                    ((ICoreEventSource2) newWorkbook)
                            .registerOnceCoreEventListener(
                                    Core.WorkbookPreSaveOnce,
                                    ICoreEventListener.NULL);
                }
                if (newEditor != null && newEditor instanceof ISaveablePart) {
                    Display.getCurrent().timerExec(500, new Runnable() {
                        public void run() {
                            ((ISaveablePart) newEditor).doSaveAs();
                        }
                    });
                }
            }
        });

    }

    private void initSheet(ISheet sheet) {
        initTopic(sheet.getRootTopic());
    }

    private void initTopic(ITopic topic) {
        for (ITopic child : topic.getAllChildren()) {
            initTopic(child);
        }
    }

    @Override
    public void dispose() {
        page = null;
    }
}
