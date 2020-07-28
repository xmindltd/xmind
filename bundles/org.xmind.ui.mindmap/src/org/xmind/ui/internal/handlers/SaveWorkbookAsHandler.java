/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.ui.internal.handlers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.ui.commands.MindMapCommandConstants;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.wizards.ISaveContext;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class SaveWorkbookAsHandler extends AbstractHandler {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
     * ExecutionEvent)
     */
    @Override
    public Object execute(final ExecutionEvent event)
            throws ExecutionException {
        final IWorkbenchWindow window = HandlerUtil
                .getActiveWorkbenchWindowChecked(event);
        Object selection = HandlerUtil.getCurrentSelectionChecked(event);
        if (selection instanceof IStructuredSelection) {
            selection = ((IStructuredSelection) selection).getFirstElement();
        }
        if (!(selection instanceof IWorkbookRef))
            return null;

        final String preferredWizardId = event
                .getParameter(MindMapCommandConstants.SAVE_AS_WIZARD_ID_PARAM);
        final Set<String> excludedWizardIds = getExcludedWizardIds(event);

        final IWorkbookRef oldWorkbookRef = (IWorkbookRef) selection;
        final IWorkbookRef[] result = new IWorkbookRef[1];

        final ProgressMonitorDialog jobRunner = new ProgressMonitorDialog(
                window.getShell());
        jobRunner.setOpenOnRun(false);

        SafeRunner.run(new SafeRunnable() {
            @Override
            public void run() throws Exception {
                result[0] = org.xmind.ui.internal.e4handlers.SaveWorkbookAsHandler
                        .saveWorkbookAs(new ISaveContext() {

                    @Override
                    public Object getContextVariable(String key) {
                        Object variable = HandlerUtil.getVariable(event, key);
                        return variable == IEvaluationContext.UNDEFINED_VARIABLE
                                ? null : variable;
                    }

                    @Override
                    public <T> T getContextVariable(Class<T> key) {
                        return window.getService(key);
                    }
                }, oldWorkbookRef, jobRunner, new IFilter() {
                    @Override
                    public boolean select(Object wizardId) {
                        if (preferredWizardId != null) {
                            return preferredWizardId.equals(wizardId);
                        } else if (!excludedWizardIds.isEmpty()) {
                            return !excludedWizardIds.contains(wizardId);
                        }
                        return true;
                    }
                }, false);
            }
        });

        final IWorkbookRef newWorkbookRef = result[0];
        if (newWorkbookRef == null || newWorkbookRef.equals(oldWorkbookRef))
            return null;

        MessageDialog dialog = new MessageDialog(window.getShell(), MindMapMessages.SaveWorkbookAsHandler_doneDialog_title,
                null,
                MindMapMessages.SaveWorkbookAsHandler_doneDialog_message,
                MessageDialog.CONFIRM, new String[] {

                        MindMapMessages.SaveWorkbookAsHandler_doneDialog_okButton_text,

                        MindMapMessages.SaveWorkbookAsHandler_doneDialog_cancelButton_text

        }, 0);
        if (dialog.open() != MessageDialog.OK)
            return null;

        try {
            window.getActivePage()
                    .openEditor(
                            MindMapUI.getEditorInputFactory()
                                    .createEditorInput(newWorkbookRef),
                            MindMapUI.MINDMAP_EDITOR_ID, true);
        } catch (PartInitException e) {
            throw new ExecutionException(e.getMessage(), e);
        }

        return null;
    }

    private static Set<String> getExcludedWizardIds(ExecutionEvent event) {
        HashSet<String> set = new HashSet<String>();
        String param = event.getParameter(
                MindMapCommandConstants.SAVE_AS_EXCLUDED_WIZARD_IDS_PARAM);
        if (param != null) {
            set.addAll(Arrays.asList(param.split(","))); //$NON-NLS-1$
        }
        return set;
    }

}
