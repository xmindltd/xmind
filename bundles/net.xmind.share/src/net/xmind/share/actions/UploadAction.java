/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 *
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL),
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 *
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package net.xmind.share.actions;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.xmind.ui.mindmap.IMindMapViewer;

import net.xmind.share.Uploader;

/**
 * @author briansun
 *
 */
@Deprecated
public class UploadAction implements IEditorActionDelegate {

    private IEditorPart editor;

    private IMindMapViewer currentViewer;

    /**
     * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction,
     *      org.eclipse.ui.IEditorPart)
     */
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        editor = targetEditor;
        currentViewer = findCurrentViewer();
        action.setEnabled(currentViewer != null);
    }

    private IMindMapViewer findCurrentViewer() {
        if (editor == null)
            return null;
        return (IMindMapViewer) editor.getAdapter(IMindMapViewer.class);
//        if (editor instanceof IGraphicalEditor) {
//            IGraphicalEditorPage page = ((IGraphicalEditor) editor)
//                    .getActivePageInstance();
//            if (page != null) {
//                IGraphicalViewer viewer = page.getViewer();
//                if (viewer instanceof IMindMapViewer)
//                    return (IMindMapViewer) viewer;
//            }
//        }
//        return null;
    }

    /**
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        if (currentViewer == null)
            return;

        final Control control = currentViewer.getControl();
        if (control == null || control.isDisposed())
            return;

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                new Uploader(control.getShell(), currentViewer).upload();
            }
        });
    }

    /**
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        currentViewer = findCurrentViewer();
        action.setEnabled(currentViewer != null);
    }

}