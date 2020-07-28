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

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.xmind.ui.internal.MindMapMessages;

/**
 * @deprecated
 */
@Deprecated
public class SaveAsTemplateAction extends Action implements IWorkbenchAction {

//    private IWorkbenchWindow window;

    /**
     * @deprecated
     */
    @Deprecated
    public SaveAsTemplateAction(String id, IWorkbenchWindow window) {
        super();
        setId(id);
//        this.window = window;
        setText(MindMapMessages.SaveAsTemplate_text);
        setToolTipText(MindMapMessages.SaveAsTemplate_toolTip);
    }

//    public void run() {
//        if (targetEditor == null)
//            return;
//
//        SafeRunner.run(new SafeRunnable() {
//            public void run() throws Exception {
//                targetEditor.doSaveAs(new NullProgressMonitor(),
//                        MindMapUI.FILE_EXT_TEMPLATE,
//                        DialogMessages.TemplateFilterName);
//                IEditorInput input = targetEditor.getEditorInput();
//                if (input != null) {
//                    File file = MME.getFile(input);
//                    if (file != null) {
//                        saveTemplateFromFile(file);
//                    }
//                }
//            }
//        });
//
//    }
//
//    private void saveTemplateFromFile(File file) {
//        MindMapTemplateManager.getInstance().importCustomTemplate(
//                file.getAbsolutePath());
//    }

    public void dispose() {
//        targetEditor = null;
//        if (window != null) {
//            window.getPartService().removePartListener(this);
//            window = null;
//        }
    }

//    public void partActivated(IWorkbenchPart part) {
//        if (part instanceof MindMapEditor) {
//            this.targetEditor = (MindMapEditor) part;
//        } else {
//            this.targetEditor = null;
//        }
//        setEnabled(this.targetEditor != null);
//    }
//
//    public void partBroughtToTop(IWorkbenchPart part) {
//    }
//
//    public void partClosed(IWorkbenchPart part) {
//        if (part == this.targetEditor) {
//            this.targetEditor = null;
//            setEnabled(this.targetEditor != null);
//        }
//    }
//
//    public void partDeactivated(IWorkbenchPart part) {
//    }
//
//    public void partOpened(IWorkbenchPart part) {
//    }

}